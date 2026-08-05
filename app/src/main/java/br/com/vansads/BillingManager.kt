package br.com.vansads

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import br.com.vansanalytics.AnalyticsManager
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

/**
 * One time purchase that removes the ads.
 *
 * The product id must match a managed product in the Play Console.
 * The state is cached, so the ads stay off when the device is offline.
 */
object BillingManager {
    // TODO: Create this product in the Play Console before you release.
    //  Go to Monetise > Products > In-app products > Create product.
    //  Set the product ID to "remove_ads" and the type to a managed product.
    //  Set a price and activate it. The purchase flow fails while the product
    //  is missing or inactive, because queryProductDetailsAsync returns nothing.
    //  Add your account under Setup > License testing to buy it without a charge.
    const val PRODUCT_ID = "remove_ads"

    private const val PREFS_NAME = "billing"
    private const val KEY_ADS_REMOVED = "ads_removed"

    @Volatile
    var adsRemoved: Boolean = false
        private set

    private var client: BillingClient? = null
    private var onEntitlementChanged: (() -> Unit)? = null

    /** Set while a purchase this session started is still open. Null for a restore. */
    private var pendingSource: String? = null
    private var lastPriceMicros: Long = 0L
    private var lastCurrency: String = ""


    fun initialize(context: Context) {
        val app = context.applicationContext
        adsRemoved = prefs(app).getBoolean(KEY_ADS_REMOVED, false)
        AnalyticsManager.getInstance().setAdsRemovedUserProperty(adsRemoved)

        if (client != null) return

        client = BillingClient.newBuilder(app)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .setListener { result, purchases ->
                when (result.responseCode) {
                    BillingClient.BillingResponseCode.OK ->
                        applyPurchases(app, purchases)

                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        AnalyticsManager.getInstance().logRemoveAdsFailed(
                            pendingSource.orEmpty(), AnalyticsManager.REASON_CANCELLED
                        )
                        pendingSource = null
                    }

                    else -> {
                        AnalyticsManager.getInstance().logRemoveAdsFailed(
                            pendingSource.orEmpty(), AnalyticsManager.REASON_BILLING_ERROR
                        )
                        pendingSource = null
                    }
                }
            }
            .build()

        // Play is the source of truth. A refund must turn the ads back on.
        connect { queryPurchases(app) }
    }

    /**
     * Starts the Play purchase flow. [onDone] runs when the entitlement changes.
     *
     * [source] names the entry point, so the reports can compare the settings
     * entry with the house banner.
     */
    fun purchase(activity: Activity, source: String, onDone: () -> Unit) {
        onEntitlementChanged = onDone
        pendingSource = source
        AnalyticsManager.getInstance().logRemoveAdsClicked(source)

        connect {
            val billingClient = client ?: return@connect
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PRODUCT_ID)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                )
                .build()

            billingClient.queryProductDetailsAsync(params) { _, result ->
                val details = result.productDetailsList.firstOrNull()

                if (details == null) {
                    // The purchase sheet stays closed with no error, so record it.
                    Firebase.crashlytics.recordException(
                        Exception("No product details for $PRODUCT_ID. Check the Play Console.")
                    )
                    AnalyticsManager.getInstance()
                        .logRemoveAdsFailed(source, AnalyticsManager.REASON_NO_PRODUCT)
                    pendingSource = null
                    return@queryProductDetailsAsync
                }

                val offer = details.oneTimePurchaseOfferDetailsList?.firstOrNull()
                lastPriceMicros = offer?.priceAmountMicros ?: 0L
                lastCurrency = offer?.priceCurrencyCode.orEmpty()
                AnalyticsManager.getInstance()
                    .logRemoveAdsFlowStarted(source, lastPriceMicros, lastCurrency)

                val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)

                // Play splits a one time product into purchase options, and each one
                // carries an offer token. A product with no purchase option has none.
                details.oneTimePurchaseOfferDetailsList
                    ?.firstOrNull()
                    ?.offerToken
                    ?.let { productParams.setOfferToken(it) }

                billingClient.launchBillingFlow(
                    activity,
                    BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(listOf(productParams.build()))
                        .build()
                )
            }
        }
    }

    /**
     * Asks Play for the purchases again. A purchase made on another device,
     * or a connection that failed at startup, resolves here without a restart.
     */
    fun refresh(context: Context) {
        val app = context.applicationContext
        connect { queryPurchases(app) }
    }

    private fun connect(onReady: () -> Unit) {
        val billingClient = client ?: return
        if (billingClient.isReady) {
            onReady()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) onReady()
            }

            override fun onBillingServiceDisconnected() = Unit
        })
    }

    private fun queryPurchases(context: Context) {
        val billingClient = client ?: return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { _, purchases ->
            applyPurchases(context, purchases)
        }
    }

    private fun applyPurchases(context: Context, purchases: List<Purchase>?) {
        val owned = purchases.orEmpty().filter {
            it.products.contains(PRODUCT_ID) && it.purchaseState == Purchase.PurchaseState.PURCHASED
        }

        // Play refunds a purchase that stays unacknowledged for three days.
        owned.filterNot { it.isAcknowledged }.forEach { acknowledge(it) }

        setAdsRemoved(context, owned.isNotEmpty())
    }

    private fun acknowledge(purchase: Purchase) {
        client?.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        ) { }
    }

    private fun setAdsRemoved(context: Context, value: Boolean) {
        if (adsRemoved == value) return

        adsRemoved = value
        prefs(context).edit().putBoolean(KEY_ADS_REMOVED, value).apply()

        val analytics = AnalyticsManager.getInstance()
        analytics.setAdsRemovedUserProperty(value)

        when {
            // Only a flow that this session started counts as a sale. Play also
            // reports a purchase after a reinstall, and that is not revenue.
            value && pendingSource != null ->
                analytics.logRemoveAdsPurchased(PRODUCT_ID, lastPriceMicros, lastCurrency)

            value -> analytics.logRemoveAdsRestored(PRODUCT_ID)

            else -> analytics.logRemoveAdsRevoked()
        }
        pendingSource = null

        onEntitlementChanged?.invoke()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
