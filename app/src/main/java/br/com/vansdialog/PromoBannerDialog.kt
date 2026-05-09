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

object PromoBannerDialog {

    private const val PRO_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.neat.nest.shoppinglist"

    fun show(activity: Activity) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_promo_banner)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.findViewById<ImageView>(R.id.imgBanner).setOnClickListener {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRO_STORE_URL)))
        }
        dialog.findViewById<TextView>(R.id.btnCloseBanner).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
