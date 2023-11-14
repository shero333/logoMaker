package com.esport.logo.maker.unlimited.application

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.applovin.sdk.AppLovinSdk
import com.esport.logo.maker.unlimited.BuildConfig
import com.esport.logo.maker.unlimited.splash.SplashActivity
import com.esport.logo.maker.unlimited.splash.SplashStart
import com.esport.logo.maker.unlimited.utils.AppOpenAdManager
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
class LogoMakerApp : Application(), LifecycleObserver {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var appContext: Context
        lateinit var appOpenAdManager:AppOpenAdManager

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
        var PREVIEW_ACTIVITY_BACK_BUTTON_PRESS_CLICK_BUTTON_INTERSTITIAL: String = ""
        var CREATE_LOGO_SCREEN_BACK_BUTTON_PRESS_INTERSTITIAL: String = ""
        var CREATE_LOGO_SCREEN_DOWNLOAD_BUTTON_PRESS_INTERSTITIAL: String = ""
        var POLICY_ACTIVITY_BUTTON_CLICK_INTERSTITIAL: String = ""
        var PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL: String = ""
        var BOARDING_ACTIVITY_BUTTON_CLICK_INTERSTITIAL: String = ""
        var SPLASH_OPENAD_AGREE_BUTTON: String = ""
    }

    private lateinit var currentActivity: Activity

    override fun onCreate() {
        super.onCreate()

        //Application context for all of the application
        appContext = this

        MobileAds.initialize(this)
        //Initializing AppLovin Ads
        AppLovinSdk.getInstance(this).mediationProvider = "max"
        AppLovinSdk.getInstance(this).initializeSdk {}

        appOpenAdManager = AppOpenAdManager()

        Handler().postDelayed(
            {
                ProcessLifecycleOwner.get().lifecycle.addObserver(this)
                appOpenAdManager.loadAd(this)
            },
            4500
        ) // 4500 milliseconds = 4.5 seconds because of delay due to firebase

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity is SplashActivity){
                    MainUtils.fetchIdsOfAds()
                }
            }

            override fun onActivityStarted(activity: Activity) {
                if (!appOpenAdManager.isShowingAd) {
                    currentActivity = activity
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

        when (SPLASH_OPENAD_AGREE_BUTTON) {

            "0" -> {
                //no ad
            }

            "1" -> {
                appOpenAdManager.showAdIfAvailable(currentActivity,object : OnShowAdCompleteListener {
                    override fun onShowAdComplete() {

                        //nothing just show ad
                    }
                })
            }
        }
    }

}