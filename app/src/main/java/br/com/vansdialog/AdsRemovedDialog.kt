package br.com.vansdialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.TextView
import br.com.activity.R

/** Confirms that the one time purchase removed the ads. */
object AdsRemovedDialog {

    fun show(activity: Activity, onDismiss: () -> Unit = {}) {
        if (activity.isFinishing) return

        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_ads_removed)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        dialog.setOnDismissListener { onDismiss() }

        dialog.findViewById<TextView>(R.id.btnAdsRemovedOk).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
