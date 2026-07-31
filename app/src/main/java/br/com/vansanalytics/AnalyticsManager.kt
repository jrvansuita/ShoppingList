package br.com.vansanalytics

import android.content.Context
import br.com.dao.ItemShoppingListDAO
import br.com.dao.ShoppingListDAO
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

class AnalyticsManager private constructor() {
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    fun logMainScreenView(context: Context) {
        val totalLists = ShoppingListDAO.count(context)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "main_screen")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "MainApp")
            param(FirebaseAnalytics.Param.ITEMS, totalLists.toLong())
            param("total_lists", totalLists.toLong())
        }
    }

    fun logAddItemScreenView(context: Context, listId: Int) {
        val totalItems = ItemShoppingListDAO.count(context, listId)
        val listTotal = ItemShoppingListDAO.getListTotal(context, listId)

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "add_item_screen")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "AddItemShoppingList")
            param(FirebaseAnalytics.Param.ITEMS, totalItems.toLong())
            param(FirebaseAnalytics.Param.VALUE, listTotal.toDouble())
            param("total_items", totalItems.toLong())
            param("list_total", listTotal.toDouble())
        }
    }

    fun logSettingsScreenView() {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "settings_screen")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "UserPreferences")
        }
    }

    fun logAboutDialogView() {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "about_dialog")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "AboutDialog")
        }
    }

    fun logAdSeen(isInterstitial: Boolean = false) {
        firebaseAnalytics.logEvent("ad_seen") {
            param("ad_type", "interstitial".takeIf { isInterstitial } ?: "banner")
        }
    }

    // The ad removal purchase.

    /**
     * Marks the account as paying or not paying.
     *
     * Every report can then split on this, so the numbers of a paying user
     * never mix with the numbers of a user who still sees the ads.
     */
    fun setAdsRemovedUserProperty(adsRemoved: Boolean) {
        firebaseAnalytics.setUserProperty(USER_PROPERTY_ADS_REMOVED, adsRemoved.toString())
    }

    /** The house banner took the banner slot. [SOURCE_HOUSE_BANNER] only. */
    fun logHouseAdSeen() {
        firebaseAnalytics.logEvent("house_ad_seen") {
            param("ad_type", "house_banner")
        }
    }

    /** The user asked to buy. [source] tells which entry point sent them. */
    fun logRemoveAdsClicked(source: String) {
        firebaseAnalytics.logEvent("remove_ads_clicked") {
            param("source", source)
        }
    }

    /** The Play sheet opened. Together with the click, this gives a drop rate. */
    fun logRemoveAdsFlowStarted(source: String, priceMicros: Long, currency: String) {
        firebaseAnalytics.logEvent("remove_ads_flow_started") {
            param("source", source)
            param(FirebaseAnalytics.Param.VALUE, priceMicros / 1_000_000.0)
            param(FirebaseAnalytics.Param.CURRENCY, currency)
        }
    }

    /** A purchase that this session paid for. Not a restore. */
    fun logRemoveAdsPurchased(productId: String, priceMicros: Long, currency: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE) {
            param(FirebaseAnalytics.Param.ITEM_ID, productId)
            param(FirebaseAnalytics.Param.VALUE, priceMicros / 1_000_000.0)
            param(FirebaseAnalytics.Param.CURRENCY, currency)
        }
    }

    /**
     * Play reported a purchase this session did not pay for.
     *
     * This is a reinstall or a second device. Keep it out of the revenue
     * numbers, or every launch of a paying user counts as a sale.
     */
    fun logRemoveAdsRestored(productId: String) {
        firebaseAnalytics.logEvent("remove_ads_restored") {
            param(FirebaseAnalytics.Param.ITEM_ID, productId)
        }
    }

    /** The purchase did not complete. [reason] separates a refusal from a fault. */
    fun logRemoveAdsFailed(source: String, reason: String) {
        firebaseAnalytics.logEvent("remove_ads_failed") {
            param("source", source)
            param("reason", reason)
        }
    }

    /** Play lost the entitlement, which means a refund. */
    fun logRemoveAdsRevoked() {
        firebaseAnalytics.logEvent("remove_ads_revoked") {}
    }

    // The Pro promotion.

    fun logPromoBannerSeen(fromSettings: Boolean) {
        firebaseAnalytics.logEvent("promo_banner_seen") {
            param("source", if (fromSettings) SOURCE_SETTINGS else "auto")
        }
    }

    fun logPromoBannerClicked() {
        firebaseAnalytics.logEvent("promo_banner_clicked") {}
    }

    fun logPromoBannerClosed() {
        firebaseAnalytics.logEvent("promo_banner_closed") {}
    }

    // The XML import.

    fun logListImported(wasSuccessful: Boolean, importedItems: Int) {
        firebaseAnalytics.logEvent("list_imported") {
            param("was_successful", wasSuccessful.toString())
            param(FirebaseAnalytics.Param.ITEMS, importedItems.toLong())
            param("imported_items", importedItems.toLong())
        }
    }

    companion object {
        const val SOURCE_SETTINGS: String = "settings"
        const val SOURCE_HOUSE_BANNER: String = "house_banner"

        const val REASON_NO_PRODUCT: String = "no_product_details"
        const val REASON_CANCELLED: String = "user_cancelled"
        const val REASON_BILLING_ERROR: String = "billing_error"

        private const val USER_PROPERTY_ADS_REMOVED = "ads_removed"

        @Volatile
        private var instance: AnalyticsManager? = null

        fun getInstance(): AnalyticsManager {
            return instance ?: synchronized(this) {
                instance ?: AnalyticsManager().also { instance = it }
            }
        }
    }
}
