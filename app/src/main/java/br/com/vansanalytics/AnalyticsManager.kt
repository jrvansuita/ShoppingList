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

    companion object {
        @Volatile
        private var instance: AnalyticsManager? = null

        fun getInstance(): AnalyticsManager {
            return instance ?: synchronized(this) {
                instance ?: AnalyticsManager().also { instance = it }
            }
        }
    }
}
