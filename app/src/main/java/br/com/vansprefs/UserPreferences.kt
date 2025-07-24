package br.com.vansprefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;
import br.com.activity.R;
import br.com.vansdialog.CustomDialogAboutApp;

public class UserPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener {
	private ListPreference listOrdenationCheckedStyle;
	private ListPreference listOrdenationAlphabeticalStyle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getFistPrefs();
		getActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.activity_user_preferences);

		listOrdenationCheckedStyle = (ListPreference) findPreference(getString(R.string.user_preference_ordenation_checked_list));
		listOrdenationAlphabeticalStyle = (ListPreference) findPreference(getString(R.string.user_preference_ordenation_alphabetical_list));

		((Preference) findPreference(getString(R.string.user_preference_about_app))).setOnPreferenceClickListener(this);

	}

	private void getFistPrefs() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putBoolean(getString(R.string.user_preference_show_check_box), prefs.getBoolean(getString(R.string.user_preference_show_check_box), true));
		editor.putBoolean(getString(R.string.user_preference_show_quantity), prefs.getBoolean(getString(R.string.user_preference_show_quantity), true));
		editor.putBoolean(getString(R.string.user_preference_show_unit_value), prefs.getBoolean(getString(R.string.user_preference_show_unit_value), true));

		editor.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		setSummaries();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		setSummaries();

	}

	private void setSummaries() {
		listOrdenationAlphabeticalStyle.setSummary(getString(R.string.default_option) + " " + listOrdenationAlphabeticalStyle.getEntry().toString());
		listOrdenationCheckedStyle.setSummary(getString(R.string.default_option) + " " + listOrdenationCheckedStyle.getEntry().toString());
	}

	public static boolean getShowCheckBox(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(context.getString(R.string.user_preference_show_check_box), true);
	}

	public static boolean getShowQuantity(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(context.getString(R.string.user_preference_show_quantity), true);
	}

	public static boolean getShowUnitValue(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(context.getString(R.string.user_preference_show_unit_value), true);
	}

	public static String getItemListCheckedOrdenation(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(context.getString(R.string.user_preference_ordenation_checked_list), "");
	}

	public static String getItemListAlphabeticalOrdenation(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(context.getString(R.string.user_preference_ordenation_alphabetical_list), "");
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		try {
			if (preference.getKey().equals(getString(R.string.user_preference_about_app))) {
				new CustomDialogAboutApp(preference.getContext()).show();				
			}
		} catch (NameNotFoundException e) {
			Toast.makeText(preference.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return true;
	}
}