package br.com.vansdialog;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import br.com.activity.R;
import br.com.vansintent.CustomIntentOutside;

public class CustomDialogAboutApp extends Dialog implements android.view.View.OnClickListener {
	private Context context;

	public CustomDialogAboutApp(Context context) throws NameNotFoundException {
		super(context);
		this.context = context;
		setCancelable(true);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		String firstInstallDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT).format(new Date(pInfo.firstInstallTime));
		String lastUpdate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT).format(new Date(pInfo.lastUpdateTime));
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setContentView(inflater.inflate(R.layout.about_app, null));

		findViewById(R.id.about_bt_icon_shopping_list).setOnClickListener(this);

		((TextView) findViewById(R.id.about_tv_version_code)).setText(context.getString(R.string.about_app_code) + " " + pInfo.versionCode);
		((TextView) findViewById(R.id.about_tv_version_app)).setText(context.getString(R.string.about_app_version) + " " + pInfo.versionName);		
		
		((TextView) findViewById(R.id.about_tv_version_android)).setText(context.getString(R.string.about_android_version) + " " + android.os.Build.VERSION.RELEASE + " SDK " + android.os.Build.VERSION.SDK_INT);
		((TextView) findViewById(R.id.about_tv_install_date)).setText(context.getString(R.string.about_app_install_date) + " " +  firstInstallDate);
		((TextView) findViewById(R.id.about_tv_update_date)).setText(context.getString(R.string.about_app_update_date) + " " +  lastUpdate);

		((TextView) findViewById(R.id.about_tv_contact)).setText(" " + context.getString(R.string.my_email));		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.about_bt_icon_shopping_list:
              CustomIntentOutside.UpdateApp(context);
			break;
		}

	}

}
