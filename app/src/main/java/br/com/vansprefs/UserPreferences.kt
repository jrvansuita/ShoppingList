package br.com.vansprefs

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.edit
import br.com.activity.R
import br.com.vansads.AdsManager
import br.com.vansanalytics.AnalyticsManager
import br.com.vansdialog.CustomDialogAboutApp

class UserPreferences : PreferenceActivity(), OnSharedPreferenceChangeListener,
    OnPreferenceClickListener {
    private var listOrdenationCheckedStyle: ListPreference? = null
    private var listOrdenationAlphabeticalStyle: ListPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        fistPrefs
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.layout.activity_user_preferences)

        listOrdenationCheckedStyle =
            findPreference(getString(R.string.user_preference_ordenation_checked_list)) as ListPreference
        listOrdenationAlphabeticalStyle =
            findPreference(getString(R.string.user_preference_ordenation_alphabetical_list)) as ListPreference

        (findPreference(getString(R.string.user_preference_about_app)) as Preference).onPreferenceClickListener =
            this

        AnalyticsManager.getInstance().logSettingsScreenView()
        addBannerAd()
    }

    private fun addBannerAd() {
        val root = listView.parent as android.view.ViewGroup
        val adContainer = android.widget.FrameLayout(this).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        root.addView(adContainer)
        AdsManager.loadAdBanner(adContainer)
    }

    private val fistPrefs: Unit
        get() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            prefs.edit(commit = true) {
                putBoolean(
                    getString(R.string.user_preference_show_check_box),
                    prefs.getBoolean(getString(R.string.user_preference_show_check_box), true)
                )
                putBoolean(
                    getString(R.string.user_preference_show_quantity),
                    prefs.getBoolean(getString(R.string.user_preference_show_quantity), true)
                )
                putBoolean(
                    getString(R.string.user_preference_show_unit_value),
                    prefs.getBoolean(getString(R.string.user_preference_show_unit_value), true)
                )

            }
        }

    override fun onBackPressed() {
        AdsManager.showInterstitialAd(this) {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        setSummaries()
    }

    override fun onPause() {
        super.onPause()
        // Unregister the listener whenever a key changes
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        setSummaries()
    }

    private fun setSummaries() {
        listOrdenationAlphabeticalStyle!!.summary =
            getString(R.string.default_option) + " " + listOrdenationAlphabeticalStyle!!.entry.toString()
        listOrdenationCheckedStyle!!.summary =
            getString(R.string.default_option) + " " + listOrdenationCheckedStyle!!.entry.toString()
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        try {
            if (preference.key == getString(R.string.user_preference_about_app)) {
                CustomDialogAboutApp(preference.context).show()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(preference.context, e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        AdsManager.showInterstitialAd(this) {
            finish()
        }
        return true
    }

    companion object {
        @JvmStatic
        fun getShowCheckBox(context: Context): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getBoolean(
                context.getString(R.string.user_preference_show_check_box), true
            )
        }

        @JvmStatic
        fun getShowQuantity(context: Context): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getBoolean(context.getString(R.string.user_preference_show_quantity), true)
        }

        @JvmStatic
        fun getShowUnitValue(context: Context): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getBoolean(
                context.getString(R.string.user_preference_show_unit_value), true
            )
        }

        fun getItemListCheckedOrdenation(context: Context): String {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getString(
                context.getString(R.string.user_preference_ordenation_checked_list), ""
            )!!
        }

        fun getItemListAlphabeticalOrdenation(context: Context): String {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getString(
                context.getString(R.string.user_preference_ordenation_alphabetical_list), ""
            )!!
        }
    }
}