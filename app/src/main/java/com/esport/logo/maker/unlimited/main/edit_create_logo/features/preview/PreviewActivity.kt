package com.esport.logo.maker.unlimited.main.edit_create_logo.features.preview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.esport.logo.maker.unlimited.databinding.ActivityPreviewBinding
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
import java.io.File
import java.util.Date

class PreviewActivity : AppCompatActivity(), MaxAdListener, MaxAdViewAdListener {

    private lateinit var binding: ActivityPreviewBinding
    private lateinit var adRequest: AdRequest
    private var mInterstitialAd: InterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false
    private val LOG_TAG = "AppOpenAdManager"
    private var loadTime: Long = 0
    private var restarted = false
    private var interstitialAd: MaxInterstitialAd? = null
    private lateinit var adViewTop: MaxAdView
    private lateinit var adViewBottom: MaxAdView

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPreviewBinding.inflate(layoutInflater)
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
        when (LogoMakerApp.PREVIEW_ACTIVITY_BANNER_TOP) {
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
                Banner1Ads()
                //top
                Banner2Ads()

            }
            "2" -> {

                //load AppLovin Banner Ads
                binding.adaptiveBanner2.visibility = View.INVISIBLE
                binding.applovinAdView2.visibility = View.VISIBLE

                adViewTop.loadAd()
            }
        }

        //bottom Ad
        when (LogoMakerApp.PREVIEW_ACTIVITY_BANNER_BOTTOM) {
            "0" -> {

                //no ad will be loaded
                binding.adaptiveBanner1.visibility = View.GONE
                binding.applovinAdView1.visibility = View.GONE

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
                binding.adaptiveBanner2.visibility = View.INVISIBLE
                binding.applovinAdView1.visibility = View.VISIBLE
                binding.applovinAdView2.visibility = View.INVISIBLE

                adViewBottom.loadAd()
            }
        }

        //loading interstitial ads
        setAd()
        interstitialAd!!.loadAd()

        MainUtils.statusBarColor(this@PreviewActivity)

        //setting the preview image
        if (intent != null){

            val image = intent.getSerializableExtra("imagePreview") as File
            binding.logoPreview.setImageBitmap(BitmapFactory.decodeFile(image.absolutePath))
        }

        //on click listener
        binding.backButton.setOnClickListener {

            if (LogoMakerApp.PREVIEW_ACTIVITY_BACK_BUTTON_PRESS_CLICK_BUTTON_INTERSTITIAL == "0"){

                //no ad
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            else if (LogoMakerApp.PREVIEW_ACTIVITY_BACK_BUTTON_PRESS_CLICK_BUTTON_INTERSTITIAL == "1"){

                //Admob interstitial Ad
                if (mInterstitialAd != null) {
                    mInterstitialAd!!.show(this)

                    mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){

                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()

                            setAd()

                            val resultIntent = Intent()
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }
                    }
                }
                else{
                    setAd()

                    val resultIntent = Intent()
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
            else if (LogoMakerApp.PREVIEW_ACTIVITY_BACK_BUTTON_PRESS_CLICK_BUTTON_INTERSTITIAL == "2"){

                //AppLovin interstitial Ad
                if(interstitialAd!!.isReady)
                    interstitialAd!!.showAd()
                else{

                    val resultIntent = Intent()
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }

            }
            else{
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        //facebook
        binding.facebookButton.setOnClickListener {

            try {

                // Try to open the Facebook app
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/{page_id}"))
                startActivity(intent)
            } catch (e: Exception) {

                // If the Facebook app is not installed, open Facebook in a web browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/{page_id}"))
                startActivity(intent)
            }
        }

        //instagram
        binding.instagramButton.setOnClickListener {

            try {

                // Try to open the Instagram app
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/{username}"))
                intent.setPackage("com.instagram.android")
                startActivity(intent)
            } catch (e: java.lang.Exception) {

                // If the Instagram app is not installed, open Instagram in a web browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/{username}"))
                startActivity(intent)
            }
        }

        //whatsApp
        binding.whatsappButton.setOnClickListener {

            try {

                // Create an Intent to open WhatsApp
                val intent = Intent(Intent.ACTION_MAIN)
                intent.component = ComponentName("com.whatsapp", "com.whatsapp.HomeActivity")
                startActivity(intent)
            } catch (e: java.lang.Exception) {

                // If WhatsApp is not installed, you can inform the user or handle it in a way that makes sense for your app.
                Toast.makeText(this@PreviewActivity, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show()
            }
        }

        //snapChat
        binding.snapchatButton.setOnClickListener {

            try {

                // Try to open the Snapchat app
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.snapchat.com/add/{username}"))
                startActivity(intent)
            } catch (e: java.lang.Exception) {

                // If the Snapchat app is not installed, you can inform the user or handle it in a way that makes sense for your app.
                Toast.makeText(this@PreviewActivity, "Snapchat is not installed.", Toast.LENGTH_SHORT).show()
            }
        }

        //more Apps
        binding.moreButton.setOnClickListener {

            // Create an intent to share some content (e.g., a text message)
            // Create an intent to share some content (e.g., a text message)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Your message to share")

            // Create a chooser dialog

            // Create a chooser dialog
            val chooser = Intent.createChooser(shareIntent, "Choose an app")

            // Verify that the intent will resolve to at least one app

            // Verify that the intent will resolve to at least one app
            if (shareIntent.resolveActivity(this@PreviewActivity.packageManager) != null) {
                startActivity(chooser)
            } else {
                // Handle the case where no apps can handle the intent
                Toast.makeText(
                    this@PreviewActivity,
                    "No apps available to handle this action.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        onBackPressedDispatcher.addCallback(object :OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

                if (LogoMakerApp.PREVIEW_ACTIVITY_BACK_BUTTON_PRESS_CLICK_BUTTON_INTERSTITIAL == "0"){

                    //no ad
                    val resultIntent = Intent()
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
                else if (LogoMakerApp.PREVIEW_ACTIVITY_BACK_BUTTON_PRESS_CLICK_BUTTON_INTERSTITIAL == "1"){

                    //Admob interstitial Ad
                    if (mInterstitialAd != null) {
                        mInterstitialAd!!.show(this@PreviewActivity)

                        mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()

                                setAd()

                                val resultIntent = Intent()
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            }
                        }
                    }
                    else{
                        setAd()

                        val resultIntent = Intent()
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                }
                else if (LogoMakerApp.PREVIEW_ACTIVITY_BACK_BUTTON_PRESS_CLICK_BUTTON_INTERSTITIAL == "2"){

                    //AppLovin interstitial Ad
                    if(interstitialAd!!.isReady)
                        interstitialAd!!.showAd()
                    else{

                        val resultIntent = Intent()
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }

                }
                else{
                    val resultIntent = Intent()
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }

        })
    }

    //Ads
    private fun Banner1Ads() {

        if (BuildConfig.DEBUG){

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this,resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner1.addView(adView)
            adView.loadAd(adRequest)
        }
        else{
            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this,resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_RELEASE
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner1.addView(adView)
            adView.loadAd(adRequest)
        }

    }
    private fun Banner2Ads() {

        if (BuildConfig.DEBUG){

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this,resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner2.addView(adView)
            adView.loadAd(adRequest)
        }
        else{

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this,resources)
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
        adViewBottom.setListener(/* p0 = */ this)
        //preparing the AdView
        adViewBottom.layoutParams = binding.applovinAdView2.layoutParams

        binding.applovinAdView1.addView(adViewBottom)
    }

    override fun onAdLoaded(p0: MaxAd?) {
        TODO("Not yet implemented")
    }

    override fun onAdDisplayed(p0: MaxAd?) {
        TODO("Not yet implemented")
    }

    override fun onAdHidden(p0: MaxAd?) {

        //loading next ad
        interstitialAd!!.loadAd()

        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onAdClicked(p0: MaxAd?) {
        TODO("Not yet implemented")
    }

    override fun onAdLoadFailed(p0: String?, p1: MaxError?) {
        TODO("Not yet implemented")
    }

    override fun onAdDisplayFailed(p0: MaxAd?, p1: MaxError?) {
        TODO("Not yet implemented")
    }

    override fun onAdExpanded(p0: MaxAd?) {
        TODO("Not yet implemented")
    }

    override fun onAdCollapsed(p0: MaxAd?) {
        TODO("Not yet implemented")
    }
}