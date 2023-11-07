package com.esport.logo.maker.unlimited.application

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.applovin.sdk.AppLovinSdk
import com.esport.logo.maker.unlimited.BuildConfig
import com.esport.logo.maker.unlimited.splash.SplashActivity
import com.esport.logo.maker.unlimited.utils.MainUtils
import com.esport.logo.maker.unlimited.utils.OnShowAdCompleteListener
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import dagger.hilt.android.HiltAndroidApp
import java.util.Date
import java.util.concurrent.Executor


@HiltAndroidApp
class LogoMakerApp : Application(),LifecycleObserver {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var appContext: Context

        //ids {Debug}
        var BANNER_AD_ADMOB_ID_DEBUG: String = ""
        var NATIVE_AD_ADMOB_ID_DEBUG: String = ""
        var INTERSTITIAL_AD_ADMOB_ID_DEBUG: String = ""
        var OPEN_AD_ADMOB_ID_DEBUG: String = ""

        //ids {Release}
        var BANNER_AD_ADMOB_ID_RELEASE: String = ""
        var NATIVE_AD_ADMOB_ID_RELEASE: String = ""
        var INTERSTITIAL_AD_ADMOB_ID_RELEASE: String = ""
        var OPEN_AD_ADMOB_ID_RELEASE: String = ""

        //networks banner and native
        var MAIN_SCREEN_BANNER_BOTTOM: String = ""
        var MAIN_SCREEN_BANNER_TOP: String = ""
        var MAIN_SCREEN_NATIVE_AD: String = ""
        var RECENTS_ACTIVITY_BANNER_TOP: String = ""
        var RECENTS_ACTIVITY_BANNER_BOTTOM: String = ""
        var PREVIEW_ACTIVITY_BANNER_TOP: String = ""
        var PREVIEW_ACTIVITY_BANNER_BOTTOM: String = ""
        var CREATE_LOGO_SCREEN_BANNER_TOP: String = ""
        var CREATE_LOGO_SCREEN_BANNER_BOTTOM: String = ""
        var EXIT_SCREEN_BANNER_TOP: String = ""
        var EXIT_SCREEN_BANNER_BOTTOM: String = ""
        var POLICY_ACTIVITY_BANNER_TOP: String = ""
        var POLICY_ACTIVITY_BANNER_BOTTOM: String = ""
        var PERMISSION_ACTIVITY_BANNER_TOP: String = ""
        var PERMISSION_ACTIVITY_BANNER_BOTTOM: String = ""
        var BOARDING_ACTIVITY_BANNER_TOP: String = ""
        var BOARDING_ACTIVITY_BANNER_BOTTOM: String = ""

        //networks interstitial and openAd
        var MAINSCREEN_CREATELOGO_BUTTON_INTERSTITIAL: String = ""
        var MAINSCREEN_EDITLOGO_BUTTON_INTERSTITIAL: String = ""
        var MAINSCREEN_RECENTLIST_ITEM_CLICK_INTERSTITIAL: String = ""
        var RECENTS_ACTIVITY_RECENTLIST_ITEM_CLICK_INTERSTITIAL: String = ""
        var RECENTS_ACTIVITY_YESTERDAYLIST_ITEM_CLICK_INTERSTITIAL: String = ""
        var RECENTS_ACTIVITY_DAYBEFORE_YESTERDAYLIST_ITEM_CLICK_INTERSTITIAL: String = ""
        var PREVIEW_ACTIVITY_BACK_BUTTON_PRESS_CLICK_BUTTON_INTERSTITIAL: String = ""
        var CREATE_LOGO_SCREEN_BACK_BUTTON_PRESS_INTERSTITIAL: String = ""
        var CREATE_LOGO_SCREEN_DOWNLOAD_BUTTON_PRESS_INTERSTITIAL: String = ""
        var POLICY_ACTIVITY_BUTTON_CLICK_INTERSTITIAL: String = ""
        var PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL: String = ""
        var BOARDING_ACTIVITY_BUTTON_CLICK_INTERSTITIAL: String = ""
        var SPLASH_OPENAD_AGREE_BUTTON: String = ""
    }

    private lateinit var currentActivity: Activity

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false
    private val LOG_TAG = "AppOpenAdManager"
    private var loadTime: Long = 0

    override fun onCreate() {
        super.onCreate()

        //Application context for all of the application
        appContext = this

        MobileAds.initialize(this)
        //Initializing AppLovin Ads
        AppLovinSdk.getInstance(this).mediationProvider = "max"
        AppLovinSdk.getInstance(this).initializeSdk {}

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        loadAd(appContext)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity is SplashActivity){
                    //Fetching ids
                    MainUtils.fetchIdsOfAds()
                }
            }
            override fun onActivityStarted(activity: Activity) {
                if (!isShowingAd) {
                    currentActivity = activity
                    loadAd(appContext)
                }
            }
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
        Log.d("here", "here")

        //Handler
        Handler().postDelayed({

            when(SPLASH_OPENAD_AGREE_BUTTON){

                "0"->{
                    //no ad
                }
                "1"->{
                    showAdIfAvailable(object:OnShowAdCompleteListener{
                        override fun onShowAdComplete() {

                            //nothing just show ad
                        }
                    })
                }
            }

        },500)
    }

    /** Request an ad. */
    fun loadAd(context: Context) {

        if (BuildConfig.DEBUG){
            // Do not load ad if there is an unused ad or one is already loading.
            if (isLoadingAd || isAdAvailable()) {
                return
            }

            isLoadingAd = true
            val request = AdRequest.Builder().build()

            AppOpenAd.load(context, OPEN_AD_ADMOB_ID_DEBUG,
                request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,

                object : AppOpenAd.AppOpenAdLoadCallback() {

                    override fun onAdLoaded(ad: AppOpenAd) {
                        // Called when an app open ad has loaded.
                        Log.d(LOG_TAG, "Ad was loaded.")
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Called when an app open ad has failed to load.
                        Log.d(LOG_TAG, loadAdError.message)
                        isLoadingAd = false;
                        loadAd(appContext)
                    }
                })
        }
        else{
            // Do not load ad if there is an unused ad or one is already loading.
            if (isLoadingAd || isAdAvailable()) {
                return
            }

            isLoadingAd = true
            val request = AdRequest.Builder().build()

            AppOpenAd.load(context, LogoMakerApp.OPEN_AD_ADMOB_ID_RELEASE,
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,

                object : AppOpenAd.AppOpenAdLoadCallback() {

                    override fun onAdLoaded(ad: AppOpenAd) {
                        // Called when an app open ad has loaded.
                        Log.d(LOG_TAG, "Ad was loaded.")
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Called when an app open ad has failed to load.
                        Log.d(LOG_TAG, loadAdError.message)
                        isLoadingAd = false;
                    }
                })
        }
    }
    /** Check if ad exists and can be shown. */
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }
    /** Shows the ad if one isn't already showing. */
    private fun showAdIfAvailable(onShowAdCompleteListener: OnShowAdCompleteListener) {
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            Log.d(LOG_TAG, "The app open ad is already showing.")
            return
        }

        // If the app open ad is not available yet, invoke the callback then load the ad.
        if (!isAdAvailable()) {
            Log.d(LOG_TAG, "The app open ad is not ready yet.")
            onShowAdCompleteListener.onShowAdComplete()
            loadAd(appContext)
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdDismissedFullScreenContent() {
                // Called when full screen content is dismissed.
                // Set the reference to null so isAdAvailable() returns false.
                Log.d(LOG_TAG, "Ad dismissed fullscreen content.")
                appOpenAd = null
                isShowingAd = false

                onShowAdCompleteListener.onShowAdComplete()
                loadAd(appContext)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when fullscreen content failed to show.
                // Set the reference to null so isAdAvailable() returns false.
                Log.d(LOG_TAG, adError.message)
                appOpenAd = null
                isShowingAd = false

                onShowAdCompleteListener.onShowAdComplete()
                loadAd(appContext)
            }

            override fun onAdShowedFullScreenContent() {
                // Called when fullscreen content is shown.
                Log.d(LOG_TAG, "Ad showed fullscreen content.")
            }
        }
        isShowingAd = true
        appOpenAd?.show(currentActivity)
    }
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }
}