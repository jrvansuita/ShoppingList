package br.com.vansdialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import br.com.activity.R
import br.com.vansanalytics.AnalyticsManager
import br.com.vansintent.CustomIntentOutside
import java.text.DateFormat
import java.util.Date

class CustomDialogAboutApp(private val context: Context) : Dialog(
    context
), View.OnClickListener {
    init {
        setCancelable(true)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val firstInstallDate =
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(
                Date(pInfo.firstInstallTime)
            )
        val lastUpdate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(
            Date(pInfo.lastUpdateTime)
        )

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        setContentView(inflater.inflate(R.layout.about_app, null))

        findViewById<View>(R.id.about_bt_icon_shopping_list).setOnClickListener(this)

        (findViewById<View>(R.id.about_tv_version_code) as TextView).text =
            context.getString(R.string.about_app_code) + " " + pInfo.versionCode
        (findViewById<View>(R.id.about_tv_version_app) as TextView).text =
            context.getString(R.string.about_app_version) + " " + pInfo.versionName

        (findViewById<View>(R.id.about_tv_version_android) as TextView).text =
            context.getString(R.string.about_android_version) + " " + Build.VERSION.RELEASE + " SDK " + Build.VERSION.SDK_INT
        (findViewById<View>(R.id.about_tv_install_date) as TextView).text =
            context.getString(R.string.about_app_install_date) + " " + firstInstallDate
        (findViewById<View>(R.id.about_tv_update_date) as TextView).text =
            context.getString(R.string.about_app_update_date) + " " + lastUpdate

        (findViewById<View>(R.id.about_tv_contact) as TextView).text =
            " " + context.getString(R.string.my_email)

        AnalyticsManager.getInstance().logAboutDialogView()

    }

    override fun onClick(v: View) {
        if (v.id == R.id.about_bt_icon_shopping_list) {
            CustomIntentOutside.UpdateApp(context)
        }
    }
}
