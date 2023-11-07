package com.esport.logo.maker.unlimited.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinSdk
import com.esport.logo.maker.unlimited.BuildConfig
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.application.LogoMakerApp
import com.esport.logo.maker.unlimited.databinding.ActivitySplashBinding
import com.esport.logo.maker.unlimited.main.ActivityMain
import com.esport.logo.maker.unlimited.policy.PolicyActivity
import com.esport.logo.maker.unlimited.preferences.SharedPreference
import com.esport.logo.maker.unlimited.utils.MainUtils
import com.esport.logo.maker.unlimited.utils.OnShowAdCompleteListener
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date


@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity(), MaxAdListener, MaxAdViewAdListener, LifecycleObserver {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var adRequest: AdRequest
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false
    private val LOG_TAG = "AppOpenAdManager"
    private var loadTime: Long = 0
    private lateinit var adViewTop: MaxAdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initializing Google Ads
        MobileAds.initialize(this)

        //Initializing AppLovin Ads
        AppLovinSdk.getInstance(this).mediationProvider = "max"
        AppLovinSdk.getInstance(this).initializeSdk {}

        adRequest = AdRequest.Builder().build()

        binding.progressBarAnim.playAnimation()
        binding.progressBarAnim.visibility = View.VISIBLE
        binding.continueButton.visibility = View.INVISIBLE

        //Fetching ids
        MainUtils.fetchIdsOfAds()

        //creating banner Ad
        BannerAdAppLovin()

        //openAd
        loadAd(this)

        //loading banner ads according to the firebase
        MainUtils.splash_banner_liveData.observe(this) { SPLASH_BANNER_BOTTOM->

            Handler().postDelayed(
                {
                    binding.progressBarAnim.pauseAnimation()
                    binding.progressBarAnim.visibility = View.INVISIBLE
                    binding.continueButton.visibility = View.VISIBLE
                },
                5000
            ) // 7000 milliseconds = 7 seconds

            if (SPLASH_BANNER_BOTTOM.isNotEmpty()){

                //showing the banner Ad on Top of the splash
                when (SPLASH_BANNER_BOTTOM) {
                    "0" -> {

                        //no ad will be loaded
                        binding.adaptiveBanner2.visibility = View.GONE
                        binding.applovinAdView2.visibility = View.GONE

                    }
                    "1" -> {

                        //Admob Ad will be loaded
                        binding.adaptiveBanner2.visibility = View.VISIBLE
                        binding.applovinAdView2.visibility = View.INVISIBLE

                        //google banner ad load
                        //bottom
                        Banner2Ads()

                    }
                    "2" -> {

                        //load AppLovin Banner Ads
                        binding.adaptiveBanner2.visibility = View.INVISIBLE
                        binding.applovinAdView2.visibility = View.VISIBLE

                        adViewTop.loadAd()
                    }
                }
            }
            else{
                // Use a Handler to schedule making the button visible after 7 seconds to wait to load the splash Ad
                binding.progressBarAnim.pauseAnimation()
                binding.progressBarAnim.visibility = View.INVISIBLE
                binding.continueButton.visibility = View.VISIBLE
            }
        }

        //remove status bar
        MainUtils.makeStatusBarTransparent(this@SplashActivity)

        //listener for splash button to continue the flow of the app!
        binding.continueButton.setOnClickListener {

            when(LogoMakerApp.SPLASH_OPENAD_AGREE_BUTTON){

                "0"->{
                        //no load Admob OpenAd
                        if (SharedPreference.isFirstRun) {

                            //changed preference value here or in the boarding screens

                            //call on Boarding screens
                            startActivity(Intent(this@SplashActivity, PolicyActivity::class.java))
                            finish()
                        }
                        else {
                            //call main Activity
                            startActivity(Intent(this@SplashActivity, ActivityMain::class.java))
                            finish()
                        }
                    }
                "1"->{
                        //load Admob OpenAd
                        showAdIfAvailable(object : OnShowAdCompleteListener {
                            override fun onShowAdComplete() {

                                if (SharedPreference.isFirstRun) {

                                    //changed preference value here or in the boarding screens

                                    //call on Boarding screens
                                    startActivity(Intent(this@SplashActivity, PolicyActivity::class.java))
                                    finish()
                                } else {
                                    //call main Activity
                                    startActivity(Intent(this@SplashActivity, ActivityMain::class.java))
                                    finish()
                                }
                            }

                        })
                    }
            }
        }
    }

    private fun BannerAdAppLovin() {

        adViewTop = MaxAdView(resources.getString(R.string.bannerAd), this)
        adViewTop.setListener(this)
        //preparing the AdView
        adViewTop.layoutParams = binding.applovinAdView2.layoutParams

        binding.applovinAdView2.addView(adViewTop)
    }

    private fun Banner2Ads() {

        if (BuildConfig.DEBUG) {

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this, resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner2.addView(adView)
            adView.loadAd(adRequest)
        }
        else {

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this, resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner2.addView(adView)
            adView.loadAd(adRequest)
        }
    }

    //AppLovin Ad loading functions
    override fun onAdLoaded(p0: MaxAd?) {}
    override fun onAdDisplayed(p0: MaxAd?) {}
    override fun onAdHidden(p0: MaxAd?) {}
    override fun onAdClicked(p0: MaxAd?) {}
    override fun onAdLoadFailed(p0: String?, p1: MaxError?) {

        Log.i("onAdLoadFailedLovin: ", p1.toString())
        Log.i("onAdLoadFailedLovin: ", "p1.toString()")
    }
    override fun onAdDisplayFailed(p0: MaxAd?, p1: MaxError?) {
        Log.i("onAdDisplayFailLovin: ", p1.toString())
        Log.i("onAdDisplayFailLovin: ", "p1.toString()")
    }
    override fun onAdExpanded(p0: MaxAd?) {}
    override fun onAdCollapsed(p0: MaxAd?) {}

    override fun onStart() {
        super.onStart()

        loadAd(this)
    }

    /** Request an ad. */
    fun loadAd(context: Context) {

        if (BuildConfig.DEBUG) {
            // Do not load ad if there is an unused ad or one is already loading.
            if (isLoadingAd || isAdAvailable()) {
                return
            }

            isLoadingAd = true
            val request = AdRequest.Builder().build()

            AppOpenAd.load(context, LogoMakerApp.OPEN_AD_ADMOB_ID_DEBUG,
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
                        loadAd(this@SplashActivity)
                    }
                })
        }
        else {
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
            loadAd(this@SplashActivity)
            return
        }

        try {

            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    // Called when full screen content is dismissed.
                    // Set the reference to null so isAdAvailable() returns false.
                    Log.d(LOG_TAG, "Ad dismissed fullscreen content.")
                    appOpenAd = null
                    isShowingAd = false

                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(this@SplashActivity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Called when fullscreen content failed to show.
                    // Set the reference to null so isAdAvailable() returns false.
                    Log.d(LOG_TAG, adError.message)
                    appOpenAd = null
                    isShowingAd = false

                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(this@SplashActivity)
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when fullscreen content is shown.
                    Log.d(LOG_TAG, "Ad showed fullscreen content.")
                }
            }
            isShowingAd = true
            appOpenAd?.show(this@SplashActivity)
        }
        catch (exception:Exception){

            //checking the exception that is thrown if the Ad is not shown because here this Ad will be always visible
            Log.d( "showAdIfAvailable: ",exception.message.toString())
        }
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }
}