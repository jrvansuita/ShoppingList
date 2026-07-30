package br.com.vansads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import br.com.activity.R
import br.com.vansdialog.AdsRemovedDialog
import com.google.android.gms.ads.AdSize

/**
 * The house banner that offers the ad removal purchase.
 *
 * It fills the same slot as the network banner, with the same size, so the
 * layout does not move when one replaces the other.
 */
object HouseAdBanner {

    /**
     * Puts the house banner into [adContainer] at the size of [adSize].
     *
     * Returns false when the container has no Activity behind it. The purchase
     * flow needs an Activity, so the caller must show the network banner then.
     */
    fun show(adContainer: ViewGroup, adSize: AdSize): Boolean {
        val activity = adContainer.context.findActivity() ?: return false

        val view = LayoutInflater.from(adContainer.context)
            .inflate(R.layout.view_house_ad, adContainer, false)

        view.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            adSize.getHeightInPixels(adContainer.context)
        )

        view.setOnClickListener {
            if (BillingManager.adsRemoved) return@setOnClickListener

            BillingManager.purchase(activity) {
                activity.runOnUiThread {
                    if (activity.isFinishing) return@runOnUiThread
                    AdsRemovedDialog.show(activity) { activity.recreate() }
                }
            }
        }

        adContainer.removeAllViews()
        adContainer.addView(view)
        return true
    }

    private fun Context.findActivity(): Activity? {
        var context: Context? = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }
}
