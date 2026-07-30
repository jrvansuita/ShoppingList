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

    fun initialize(context: Context) {
        val app = context.applicationContext
        adsRemoved = prefs(app).getBoolean(KEY_ADS_REMOVED, false)

        if (client != null) return

        client = BillingClient.newBuilder(app)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .setListener { _, purchases -> applyPurchases(app, purchases) }
            .build()

        // Play is the source of truth. A refund must turn the ads back on.
        connect { queryPurchases(app) }
    }

    /** Starts the Play purchase flow. [onDone] runs when the entitlement changes. */
    fun purchase(activity: Activity, onDone: () -> Unit) {
        onEntitlementChanged = onDone

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
                val details = result.productDetailsList.firstOrNull() ?: return@queryProductDetailsAsync

                billingClient.launchBillingFlow(
                    activity,
                    BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(
                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(details)
                                    .build()
                            )
                        )
                        .build()
                )
            }
        }
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
        onEntitlementChanged?.invoke()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
