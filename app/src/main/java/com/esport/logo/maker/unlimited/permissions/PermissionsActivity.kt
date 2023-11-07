package com.esport.logo.maker.unlimited.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.sdk.AppLovinSdk
import com.esport.logo.maker.unlimited.BuildConfig
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.application.LogoMakerApp
import com.esport.logo.maker.unlimited.databinding.ActivityPermissionsBinding
import com.esport.logo.maker.unlimited.one_time_screens.on_boarding.OnBoardingActivity
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
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class PermissionsActivity : AppCompatActivity(),MaxAdListener, MaxAdViewAdListener {

    private lateinit var binding: ActivityPermissionsBinding
    private var REQUEST_CODE_STORAGE = 100
    private lateinit var adRequest: AdRequest
    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialAd: MaxInterstitialAd? = null
    private lateinit var adViewTop: MaxAdView
    private lateinit var adViewBottom: MaxAdView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initializing Google Ads
        MobileAds.initialize(this)
        AppLovinSdk.initializeSdk(this)

        //Initializing AppLovin Ads
        AppLovinSdk.getInstance(this).mediationProvider = "max"
        AppLovinSdk.initializeSdk(this)

        adRequest = AdRequest.Builder().build()

        //AppLovin
        interstitialAd = MaxInterstitialAd(resources.getString(R.string.interstitialAd), this)
        interstitialAd!!.setListener(this)

        //Creating banner for AppLovin
        BannerAdAppLovinTop()
        BannerAdAppLovinBottom()

        //top Ad
        when (LogoMakerApp.PERMISSION_ACTIVITY_BANNER_TOP) {
            "0" -> {

                //no ad will be loaded
                binding.adaptiveBanner2.visibility = View.INVISIBLE
                binding.applovinAdView2.visibility = View.INVISIBLE

            }
            "1" -> {

                //Admob Ad will be loaded
                binding.adaptiveBanner2.visibility = View.VISIBLE
                binding.applovinAdView2.visibility = View.INVISIBLE

                //google banner ad load
                //top
                Banner2Ads()
                //bottom
                Banner1Ads()

            }
            "2" -> {

                //load AppLovin Banner Ads
                binding.adaptiveBanner2.visibility = View.INVISIBLE
                binding.applovinAdView2.visibility = View.VISIBLE

                adViewTop.loadAd()
            }
        }

        //bottom Ad
        when (LogoMakerApp.PERMISSION_ACTIVITY_BANNER_BOTTOM) {
            "0" -> {

                //no ad will be loaded
                binding.adaptiveBanner1.visibility = View.INVISIBLE
                binding.applovinAdView1.visibility = View.INVISIBLE

            }
            "1" -> {

                //Admob Ad will be loaded
                binding.adaptiveBanner1.visibility = View.VISIBLE
                binding.applovinAdView1.visibility = View.INVISIBLE

                //google banner ad load
                //bottom
                Banner1Ads()
                //top
                Banner2Ads()
            }
            "2" -> {

                //load AppLovin Banner Ads
                binding.adaptiveBanner1.visibility = View.INVISIBLE
                binding.applovinAdView1.visibility = View.VISIBLE

                adViewBottom.loadAd()
            }
        }

        //loading interstitial ads
        setAd()
        interstitialAd!!.loadAd()

        MainUtils.statusBarColor(this@PermissionsActivity)

        // runtime permissions on button click
        binding.continueButton.setOnClickListener {

            //images
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {


                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    ) {

                    if (LogoMakerApp.PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "0"){

                        //No Ad
                        // Permissions have already been granted
                        startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                    }
                    else if (LogoMakerApp.PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "1"){

                        //AdMob
                        if (mInterstitialAd != null) {
                            mInterstitialAd!!.show(this)

                            mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){

                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()

                                    setAd()
                                    // Permissions have already been granted
                                    startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                                }
                            }
                        }
                        else{
                            setAd()
                            // Permissions have already been granted
                            startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                        }
                    }
                    else if (LogoMakerApp.PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "2"){

                        //AppLovin load
                        if(interstitialAd!!.isReady)
                            interstitialAd!!.showAd()
                        else{
                            // Permissions have already been granted
                            startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                        }
                    }
                    else{
                        // Permissions have already been granted
                        startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                    }

                }
                else {
                    // Request the permissions
                    ActivityCompat.requestPermissions(this, arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_CODE_STORAGE)

                }
            }
            else {
                if (MainUtils.hasPermissions(this)){

                    if (LogoMakerApp.PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "0"){

                        //No Ad
                        // Permissions have already been granted
                        startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                    }
                    else if (LogoMakerApp.PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "1"){

                        //AdMob
                        if (mInterstitialAd != null) {
                            mInterstitialAd!!.show(this)

                            mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){

                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()

                                    setAd()
                                    // Permissions have already been granted
                                    startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                                }
                            }
                        }
                        else{
                            setAd()
                            // Permissions have already been granted
                            startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                        }
                    }
                    else if (LogoMakerApp.PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "2"){

                        //AppLovin load
                        if(interstitialAd!!.isReady)
                            interstitialAd!!.showAd()
                        else{
                            // Permissions have already been granted
                            startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                        }
                    }
                    else{
                        // Permissions have already been granted
                        startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                    }
                }
                else{
                    // Request the permissions
                    MainUtils.getPermissions(this)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_STORAGE) {

            //Read external storage
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.i("TAG", "gallery permission allowed")

                //granted permission
                if (LogoMakerApp.PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "0"){

                    //No Ad
                    // Permissions have already been granted
                    startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                }
                else if (LogoMakerApp.PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "1"){

                    //AdMob
                    if (mInterstitialAd != null) {
                        mInterstitialAd!!.show(this)

                        mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()

                                setAd()
                                // Permissions have already been granted
                                startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                            }
                        }
                    }
                    else{
                        setAd()
                        // Permissions have already been granted
                        startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                    }
                }
                else if (LogoMakerApp.PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "2"){

                    //AppLovin load
                    if(interstitialAd!!.isReady)
                        interstitialAd!!.showAd()
                    else{
                        // Permissions have already been granted
                        startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                    }
                }
                else{
                    // Permissions have already been granted
                    startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                }

            }
            else{
                // Request the permissions
                ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE)
            }
        }
        else{
            //granted permission
            if (LogoMakerApp.PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "0"){

                //No Ad
                // Permissions have already been granted
                startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
            }
            else if (LogoMakerApp.PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "1"){

                //AdMob
                if (mInterstitialAd != null) {
                    mInterstitialAd!!.show(this)

                    mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){

                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()

                            setAd()
                            // Permissions have already been granted
                            startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                        }
                    }
                }
                else{
                    setAd()
                    // Permissions have already been granted
                    startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                }
            }
            else if (LogoMakerApp.PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "2"){

                //AppLovin load
                if(interstitialAd!!.isReady)
                    interstitialAd!!.showAd()
                else{
                    // Permissions have already been granted
                    startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
                }
            }
            else{
                // Permissions have already been granted
                startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
            }
        }
    }

    override fun onAdLoaded(p0: MaxAd?) {}
    override fun onAdDisplayed(p0: MaxAd?) {}
    override fun onAdHidden(p0: MaxAd?) {
        interstitialAd!!.loadAd()
        // Permissions have already been granted {end interstitial}
        startActivity(Intent(this@PermissionsActivity, OnBoardingActivity::class.java))
    }
    override fun onAdClicked(p0: MaxAd?) {}
    override fun onAdLoadFailed(p0: String?, p1: MaxError?) {}
    override fun onAdDisplayFailed(p0: MaxAd?, p1: MaxError?) {}
    override fun onAdExpanded(p0: MaxAd?) {}

    override fun onAdCollapsed(p0: MaxAd?) {}

    //Ads
    private fun Banner1Ads() {

        if (BuildConfig.DEBUG){

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this, resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner1.addView(adView)
            adView.loadAd(adRequest)
        }
        else{
            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this, resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_RELEASE
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner1.addView(adView)
            adView.loadAd(adRequest)
        }
    }

    private fun Banner2Ads() {

        if (BuildConfig.DEBUG){

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this, resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner2.addView(adView)
            adView.loadAd(adRequest)
        }
        else{
            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this, resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_RELEASE
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner2.addView(adView)
            adView.loadAd(adRequest)
        }
    }

    //Interstitial Ad
    private fun setAd() {
        if (BuildConfig.DEBUG){

            InterstitialAd.load(
                this,
                LogoMakerApp.INTERSTITIAL_AD_ADMOB_ID_DEBUG,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d("AdError", adError.toString())
                        mInterstitialAd = null
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d("AdError", "Ad was loaded.")
                        mInterstitialAd = interstitialAd
                    }
                })
        }
        else{

            InterstitialAd.load(
                this,
                LogoMakerApp.INTERSTITIAL_AD_ADMOB_ID_RELEASE,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d("AdError", adError.toString())
                        mInterstitialAd = null
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d("AdError", "Ad was loaded.")
                        mInterstitialAd = interstitialAd
                    }
                })
        }
    }

    private fun BannerAdAppLovinTop() {

        adViewTop = MaxAdView(resources.getString(R.string.bannerAd), this)
        adViewTop.setListener(this)
        //preparing the AdView
        adViewTop.layoutParams = binding.applovinAdView2.layoutParams

        binding.applovinAdView2.addView(adViewTop)
    }

    private fun BannerAdAppLovinBottom() {

        adViewBottom = MaxAdView(resources.getString(R.string.bannerAd), this)
        adViewBottom.setListener(this)
        //preparing the AdView
        adViewBottom.layoutParams = binding.applovinAdView2.layoutParams

        binding.applovinAdView1.addView(adViewBottom)
    }
}