package br.com.vansads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.ViewGroup
import br.com.activity.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdsManager {
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7819301718588435/7682968684"
    private const val INTERSTITIAL_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    const val BANNER_AD_UNIT_ID = "ca-app-pub-7819301718588435/4430953538"
    const val BANNER_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    private var interstitialAd: InterstitialAd? = null
    private var isInitialized = false
    private var isShowingAd = false

    fun initialize(application: Application) {
        if (isInitialized) return

        MobileAds.initialize(application) {
            isInitialized = true
            loadInterstitialAd(application)
        }
    }

    private fun loadInterstitialAd(context: Context) {
        val adUnitId =
            if (BuildConfig.DEBUG) INTERSTITIAL_TEST_AD_UNIT_ID else INTERSTITIAL_AD_UNIT_ID
        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            })
    }

    fun showInterstitialAd(activity: Activity, onAdFinished: () -> Unit) {
        if (isShowingAd || interstitialAd == null) {
            onAdFinished()
            return
        }

        isShowingAd = true

        interstitialAd?.apply {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    interstitialAd = null // Discard the shown ad
                }

                override fun onAdDismissedFullScreenContent() {
                    isShowingAd = false
                    onAdFinished()
                    loadInterstitialAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    isShowingAd = false
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
        adView.loadAd(adRequest)
    }
}
