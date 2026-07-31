package br.com.vansdialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import br.com.activity.R
import br.com.vansads.BillingManager

object PromoBannerDialog {

    private const val PRO_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.neat.nest.shoppinglist"
    private const val PREFS_NAME = "promo_banner"
    private const val KEY_CLOSED_AT = "closed_at"
    private const val FIVE_DAYS_MS = 5 * 24 * 60 * 60 * 1000L

    fun show(activity: Activity, force: Boolean = false) {
        // A user who paid to remove the ads never sees the Pro promotion.
        if (BillingManager.adsRemoved) return
        if (!force && !shouldShow(activity)) return

        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_promo_banner)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        dialog.findViewById<ImageView>(R.id.imgBanner).setOnClickListener {
            saveClosedAt(activity)
            dialog.dismiss()
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRO_STORE_URL)))
        }
        dialog.findViewById<TextView>(R.id.btnCloseBanner).setOnClickListener {
            saveClosedAt(activity)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun shouldShow(activity: Activity): Boolean {
        val closedAt = activity
            .getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)
            .getLong(KEY_CLOSED_AT, 0L)
        return closedAt == 0L || System.currentTimeMillis() - closedAt >= FIVE_DAYS_MS
    }

    private fun saveClosedAt(activity: Activity) {
        activity.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)
            .edit()
            .putLong(KEY_CLOSED_AT, System.currentTimeMillis())
            .apply()
    }
}
