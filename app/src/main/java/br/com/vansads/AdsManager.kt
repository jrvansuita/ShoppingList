package br.com.vansads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.ViewGroup
import br.com.activity.BuildConfig
import br.com.vansanalytics.AnalyticsManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

object AdsManager {
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7819301718588435/7682968684"
    private const val INTERSTITIAL_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private const val BANNER_AD_UNIT_ID = "ca-app-pub-7819301718588435/4430953538"
    private const val BANNER_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    private var interstitialAd: InterstitialAd? = null
    private var isInitialized = false
    private var isShowingAd = false

    fun initialize(application: Application) {
        if (!BuildConfig.ADS_ENABLED) return
        if (isInitialized) return

        MobileAds.initialize(application) {
            isInitialized = true
            loadInterstitialAd(application)
        }
    }

    private fun canShowInterstitialAd(): Boolean {
        return BuildConfig.ADS_ENABLED && !isShowingAd && interstitialAd != null
    }

    private fun loadInterstitialAd(context: Context) {
        if (!BuildConfig.ADS_ENABLED) {
            return
        }

        val adUnitId =
            if (BuildConfig.DEBUG) INTERSTITIAL_TEST_AD_UNIT_ID else INTERSTITIAL_AD_UNIT_ID
        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    AnalyticsManager.getInstance().logAdLoaded(true)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Firebase.crashlytics.recordException(
                        Exception("Failed to load interstitial ad: ${error.message}")
                    )
                    interstitialAd = null
                }
            })
    }

    fun showInterstitialAd(activity: Activity, onAdFinished: () -> Unit) {
        if (!canShowInterstitialAd()) {
            onAdFinished()
            return
        }

        isShowingAd = true

        interstitialAd?.apply {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    interstitialAd = null
                    AnalyticsManager.getInstance().logAdSeen(true)
                }

                override fun onAdClicked() {
                    AnalyticsManager.getInstance().logAdOpen(true)
                }

                override fun onAdDismissedFullScreenContent() {
                    isShowingAd = false
                    onAdFinished()
                    loadInterstitialAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    isShowingAd = false
                    Firebase.crashlytics.recordException(
                        Exception("Failed to show interstitial ad: ${adError.message}")
                    )
                    onAdFinished()
                    loadInterstitialAd(activity)
                }
            }

            show(activity)
        } ?: onAdFinished()
    }


    fun loadAdBanner(
        adContainer: ViewGroup,
        isDebug: Boolean = BuildConfig.DEBUG
    ) {
        if (!BuildConfig.ADS_ENABLED) {
            adContainer.removeAllViews()
            return
        }

        val adView = AdView(adContainer.context)

        val adUnitId = if (isDebug) {
            BANNER_TEST_AD_UNIT_ID
        } else {
            BANNER_AD_UNIT_ID
        }

        adView.adUnitId = adUnitId
        adView.setAdSize(
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                adContainer.context,
                360
            )
        )

        adContainer.removeAllViews()
        adContainer.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.adListener = object : com.google.android.gms.ads.AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Firebase.crashlytics.recordException(
                    Exception("Failed to load banner ad: ${adError.message}")
                )
            }

            override fun onAdLoaded() {
                AnalyticsManager.getInstance().logAdSeen()
            }

            override fun onAdOpened() {
                AnalyticsManager.getInstance().logAdOpen()
            }
        }
        adView.loadAd(adRequest)
    }
}
