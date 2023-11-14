package com.esport.logo.maker.unlimited.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
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
    private lateinit var adViewTop: MaxAdView

    @SuppressLint("NewApi")
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

        //remove status bar
        MainUtils.makeStatusBarTransparent(this@SplashActivity)

//        LogoMakerApp.appOpenAdManager.loadAd(this)

        //runtime 1st check if the connection is available then the call will be sent if not then the button will be visible
        if (checkForInternet(this)) {
            //loading banner ads according to the firebase
            MainUtils.splash_banner_liveData.observe(this) { SPLASH_BANNER_BOTTOM->

                Handler().postDelayed(
                    {
                        binding.progressBarAnim.pauseAnimation()
                        binding.progressBarAnim.visibility = View.INVISIBLE
                        binding.continueButton.visibility = View.VISIBLE
                    },
                    7000
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
        }
        else {
            binding.continueButton.visibility = View.VISIBLE
            binding.progressBarAnim.visibility = View.GONE
        }

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
                        LogoMakerApp.appOpenAdManager.showAdIfAvailable(this@SplashActivity,object : OnShowAdCompleteListener {
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

                else ->
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
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        //this callback is for runtime check that whether the internet is connection is available or not!
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // network is available for use
            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                runOnUiThread {

                    //loading banner ads according to the firebase
                    MainUtils.splash_banner_liveData.observe(this@SplashActivity) { SPLASH_BANNER_BOTTOM->

                        Handler().postDelayed(
                            {
                                binding.progressBarAnim.pauseAnimation()
                                binding.progressBarAnim.visibility = View.INVISIBLE
                                binding.continueButton.visibility = View.VISIBLE
                            },
                            7000
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
                }
            }

            // lost network connection
            override fun onLost(network: Network) {
                super.onLost(network)

                runOnUiThread {
                    binding.progressBarAnim.pauseAnimation()
                    binding.progressBarAnim.visibility = View.INVISIBLE
                    binding.continueButton.visibility = View.VISIBLE
                }

            }
        }

        val connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
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
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_RELEASE
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
    @SuppressLint("NewApi")
    private fun checkForInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Returns a Network object corresponding to
        // the currently active default data network.
        val network = connectivityManager.activeNetwork ?: return false

        // Representation of the capabilities of an active network.
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            // Indicates this network uses a Wi-Fi transport,
            // or WiFi has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

            // Indicates this network uses a Cellular transport. or
            // Cellular has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            // else return false
            else -> false
        }
    }
}