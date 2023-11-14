package com.esport.logo.maker.unlimited.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.esport.logo.maker.unlimited.BuildConfig
import com.esport.logo.maker.unlimited.application.LogoMakerApp
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

class AppOpenAdManager {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false

    /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
    private var loadTime: Long = 0

    fun loadAd(context: Context) {

        Log.i( "loadAdId: ",LogoMakerApp.OPEN_AD_ADMOB_ID_RELEASE)

        if (BuildConfig.DEBUG){

            MainUtils.openAd_id_debug_liveData.observeForever{OPEN_AD_ADMOB_ID_DEBUG ->

                // Do not load ad if there is an unused ad or one is already loading.
                if (OPEN_AD_ADMOB_ID_DEBUG.isNotEmpty()){

                    if (isLoadingAd || isAdAvailable()) {
                        return@observeForever
                    }

                    isLoadingAd = true
                    val request = AdRequest.Builder().build()
                    AppOpenAd.load(
                        context,
                        OPEN_AD_ADMOB_ID_DEBUG,
                        request,
                        object : AppOpenAd.AppOpenAdLoadCallback() {

                            override fun onAdLoaded(ad: AppOpenAd) {
                                appOpenAd = ad
                                isLoadingAd = false
                                loadTime = Date().time
                                Log.i(LOG_TAG, "onAdLoaded.")
//                            Toast.makeText(context, "onAdLoaded", Toast.LENGTH_SHORT).show()
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                isLoadingAd = false
                                Log.e(LOG_TAG, "onAdFailedToLoad: " + loadAdError.message)
//                            Toast.makeText(context, "onAdFailedToLoad: ${loadAdError.message}", Toast.LENGTH_LONG).show()
                            }
                        })
                }
                else{
                    if (isLoadingAd || isAdAvailable()) {
                        return@observeForever
                    }

                    isLoadingAd = true
                    val request = AdRequest.Builder().build()
                    AppOpenAd.load(
                        context,
                        "ca-app-pub-3940256099942544/3419835294",
                        request,
                        object : AppOpenAd.AppOpenAdLoadCallback() {

                            override fun onAdLoaded(ad: AppOpenAd) {
                                appOpenAd = ad
                                isLoadingAd = false
                                loadTime = Date().time
                                Log.i(LOG_TAG, "onAdLoaded.")
//                            Toast.makeText(context, "onAdLoaded", Toast.LENGTH_SHORT).show()
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                isLoadingAd = false
                                Log.e(LOG_TAG, "onAdFailedToLoad: " + loadAdError.message)
//                            Toast.makeText(context, "onAdFailedToLoad: ${loadAdError.message}", Toast.LENGTH_LONG).show()
                            }
                        })
                }

            }
        }
        else{

            MainUtils.openAd_id_release_liveData.observeForever{ OPEN_AD_ADMOB_ID_RELEASE->

                if (OPEN_AD_ADMOB_ID_RELEASE.isNotEmpty()){

                    // Do not load ad if there is an unused ad or one is already loading.
                    if (isLoadingAd || isAdAvailable()) {
                        return@observeForever
                    }

                    isLoadingAd = true
                    val request = AdRequest.Builder().build()
                    AppOpenAd.load(
                        context,
                        OPEN_AD_ADMOB_ID_RELEASE,
                        request,
                        object : AppOpenAd.AppOpenAdLoadCallback() {

                            override fun onAdLoaded(ad: AppOpenAd) {
                                appOpenAd = ad
                                isLoadingAd = false
                                loadTime = Date().time
                                Log.i(LOG_TAG, "onAdLoaded.")
//                            Toast.makeText(context, "onAdLoaded", Toast.LENGTH_SHORT).show()
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                isLoadingAd = false
                                Log.e(LOG_TAG, "onAdFailedToLoad: " + loadAdError.message)
//                            Toast.makeText(context, "onAdFailedToLoad: ${loadAdError.message}", Toast.LENGTH_LONG).show()
                            }
                        })
                }
                else{

                    // Do not load ad if there is an unused ad or one is already loading.
                    if (isLoadingAd || isAdAvailable()) {
                        return@observeForever
                    }

                    isLoadingAd = true
                    val request = AdRequest.Builder().build()
                    AppOpenAd.load(
                        context,
                        "ca-app-pub-5809026421762394/1749576267",
                        request,
                        object : AppOpenAd.AppOpenAdLoadCallback() {

                            override fun onAdLoaded(ad: AppOpenAd) {
                                appOpenAd = ad
                                isLoadingAd = false
                                loadTime = Date().time
                                Log.i(LOG_TAG, "onAdLoaded.")
//                            Toast.makeText(context, "onAdLoaded", Toast.LENGTH_SHORT).show()
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                isLoadingAd = false
                                Log.e(LOG_TAG, "onAdFailedToLoad: " + loadAdError.message)
//                            Toast.makeText(context, "onAdFailedToLoad: ${loadAdError.message}", Toast.LENGTH_LONG).show()
                            }
                        })
                }
            }
        }
    }

    /** Check if ad was loaded more than n hours ago. */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Check if ad exists and can be shown. */
    private fun isAdAvailable(): Boolean {

        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }
    fun showAdIfAvailable(activity: Activity) {
        showAdIfAvailable(
            activity,
            object : OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                    // Empty because the user will go back to the activity that shows the ad.
                }
            }
        )
    }
    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener) {
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            Log.d(LOG_TAG, "The app open ad is already showing.")
            return
        }

        // If the app open ad is not available yet, invoke the callback.
        if (!isAdAvailable()) {
            Log.d(LOG_TAG, "The app open ad is not ready yet.")
            onShowAdCompleteListener.onShowAdComplete()
            loadAd(activity)
            return
        }

        Log.d(LOG_TAG, "Will show ad.")

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                /** Called when full screen content is dismissed. */
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false
                    Log.d(LOG_TAG, "onAdDismissedFullScreenContent.")
//                    Toast.makeText(activity, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT).show()

                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                /** Called when fullscreen content failed to show. */
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                    Log.d(LOG_TAG, "onAdFailedToShowFullScreenContent: " + adError.message)
//                    Toast.makeText(activity, "onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT).show()

                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                /** Called when fullscreen content is shown. */
                override fun onAdShowedFullScreenContent() {
                    Log.d(LOG_TAG, "onAdShowedFullScreenContent.")
//                    Toast.makeText(activity, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT).show()
                }
            }
        isShowingAd = true
        appOpenAd?.show(activity)
    }
}