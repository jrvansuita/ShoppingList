package br.com.vansads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.view.accessibility.AccessibilityNodeInfo
import br.com.activity.BuildConfig
import br.com.vansanalytics.AnalyticsManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
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
import kotlin.random.Random

object AdsManager {
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7819301718588435/6992811341"
    private const val INTERSTITIAL_AD_UNIT_ID2 = "ca-app-pub-7819301718588435/4731078697"
    private const val INTERSTITIAL_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private const val BANNER_AD_UNIT_ID = "ca-app-pub-7819301718588435/5949601500"
    private const val BANNER_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    private var interstitialAd: InterstitialAd? = null
    private var interstitialAdLoadedTime: Long = 0L
    private val INTERSTITIAL_AD_EXPIRATION_MS = 60 * 60 * 1000L // 1 hour

    private var isInitialized = false
    private var isShowingAd = false

    /** How often the house banner takes the banner slot, instead of the network banner. */
    private const val HOUSE_AD_CHANCE = 0.3f

    /** Ads are off when the build disables them, or when the user paid to remove them. */
    private fun adsEnabled() = BuildConfig.ADS_ENABLED && !BillingManager.adsRemoved

    /** The house banner uses this too, so both banners have the same size. */
    private fun bannerAdSize(context: Context): AdSize =
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, 360)

    fun initialize(application: Application) {
        if (!adsEnabled()) return
        if (isInitialized) return

        MobileAds.initialize(application) {
            isInitialized = true
            loadInterstitialAd(application)
        }
    }

    private fun canShowInterstitialAd(): Boolean {
        return adsEnabled() && !isShowingAd && interstitialAd != null
    }

    private fun loadInterstitialAd(context: Context) {
        if (!adsEnabled()) return
        val adUnitId = getInterstitialAdUnitId()
        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    interstitialAdLoadedTime = System.currentTimeMillis()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Firebase.crashlytics.recordException(
                        Exception("Failed to load interstitial ad($adUnitId): ${error.message}")
                    )
                    interstitialAd = null
                    interstitialAdLoadedTime = 0L
                }
            })
    }

    private fun isInterstitialAdExpired(): Boolean {
        return interstitialAd != null &&
                (System.currentTimeMillis() - interstitialAdLoadedTime > INTERSTITIAL_AD_EXPIRATION_MS)
    }

    fun showInterstitialAd(activity: Activity, onAdFinished: () -> Unit) {
        if (!canShowInterstitialAd() || isInterstitialAdExpired()) {
            interstitialAd = null
            interstitialAdLoadedTime = 0L
            loadInterstitialAd(activity)
            onAdFinished()
            return
        }
        isShowingAd = true
        interstitialAd?.apply {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdImpression() {
                    interstitialAd = null
                    interstitialAdLoadedTime = 0L
                    AnalyticsManager.getInstance().logAdSeen(true)
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
        }
    }

    fun refreshInterstitialAdCache(context: Context) {
        interstitialAd = null
        interstitialAdLoadedTime = 0L
        loadInterstitialAd(context)
    }

    /**
     * Loads a banner into [adContainer].
     *
     * When [allowHouseAd] is true, the house banner that offers the ad removal
     * purchase takes the slot part of the time, in place of the network banner.
     */
    fun loadAdBanner(
        adContainer: ViewGroup,
        allowHouseAd: Boolean = false,
    ) {
        if (!adsEnabled()) {
            releaseAdBanner(adContainer)
            return
        }

        releaseAdBanner(adContainer)

        if (allowHouseAd && Random.nextFloat() < HOUSE_AD_CHANCE) {
            if (HouseAdBanner.show(adContainer, bannerAdSize(adContainer.context))) return
            // The house banner needs an Activity to start the purchase. Fall back.
        }

        // The slot takes the full width, and the ad centers inside it. Both
        // banners then share the same geometry on every screen.
        adContainer.layoutParams = adContainer.layoutParams?.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }

        // The ad runs in a WebView. When that WebView holds the window focus and
        // then leaves the tree, the next draw pass maps the focus rectangle
        // through a parent that is already null, and the app crashes. Keep the
        // focus out of the ad, so there is nothing to map.
        adContainer.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

        // Accessibility focus is a separate thing, and the setting above does not
        // cover it. A screen reader can hold accessibility focus inside the ad.
        // When the window regains focus, the platform maps that node through its
        // parent. If the ad left the tree first, the parent is null and the app
        // dies with no frame of ours in the report. Keep the ad out of the
        // accessibility tree, so there is no node to hold.
        adContainer.importantForAccessibility =
            View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS

        val adView = AdView(adContainer.context)

        val adUnitId = if (BuildConfig.DEBUG) {
            BANNER_TEST_AD_UNIT_ID
        } else {
            BANNER_AD_UNIT_ID
        }

        adView.adUnitId = adUnitId
        adView.setAdSize(bannerAdSize(adContainer.context))

        adContainer.removeAllViews()
        adContainer.addView(
            adView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL
            )
        )

        val adRequest = AdRequest.Builder().build()
        adView.adListener = object : AdListener() {
            override fun onAdImpression() {
                AnalyticsManager.getInstance().logAdSeen()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Firebase.crashlytics.recordException(
                    Exception("Failed to load banner ad: ${adError.message}")
                )
            }
        }
        adView.loadAd(adRequest)
    }

    /**
     * Empties the banner slot when the ads went off after the screen loaded
     * its banner. A purchase on one screen then clears the banner on the
     * screens behind it, as soon as each one comes back.
     *
     * Call this from onResume of every screen that shows a banner.
     */
    fun refreshAdBanner(adContainer: ViewGroup) {
        if (!adsEnabled()) releaseAdBanner(adContainer)
    }

    /**
     * Empties the banner slot in [adContainer].
     *
     * Clears the focus and destroys the [AdView] before the view leaves the
     * tree. A detached ad view that still holds the focus crashes the next draw
     * pass in ViewRootImpl.scrollToRectOrFocus. Destroying it also releases the
     * WebView that the ad runs in.
     *
     * Call this from onDestroy of every screen that shows a banner.
     */
    fun releaseAdBanner(adContainer: ViewGroup) {
        if (adContainer.childCount == 0) return

        // Take the focus back before the children leave the tree.
        adContainer.clearFocus()

        for (index in 0 until adContainer.childCount) {
            val child = adContainer.getChildAt(index)
            child.clearFocus()
            child.performAccessibilityAction(
                AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS,
                null
            )
            if (child is AdView) {
                child.visibility = View.GONE
                child.destroy()
            }
        }

        adContainer.removeAllViews()

        // Give both settings back. The house banner is a real control, so it
        // must stay reachable by touch and by a screen reader.
        adContainer.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        adContainer.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
    }

    private fun getInterstitialAdUnitId(): String {
        if (BuildConfig.DEBUG) return INTERSTITIAL_TEST_AD_UNIT_ID

        val ids = listOf(INTERSTITIAL_AD_UNIT_ID, INTERSTITIAL_AD_UNIT_ID2)
        return ids.random()
    }
}
