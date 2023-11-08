package com.esport.logo.maker.unlimited.one_time_screens.on_boarding

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.marginEnd
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
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
import com.esport.logo.maker.unlimited.databinding.ActivityBoardingBinding
import com.esport.logo.maker.unlimited.main.ActivityMain
import com.esport.logo.maker.unlimited.one_time_screens.on_boarding.adapter.OnBoardingAdapter
import com.esport.logo.maker.unlimited.one_time_screens.on_boarding.utils.OnboardingPageTransformer
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
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class OnBoardingActivity : AppCompatActivity(), MaxAdListener, MaxAdViewAdListener {

    private lateinit var binding: ActivityBoardingBinding
    private lateinit var onboardingAdapter: OnBoardingAdapter
    private lateinit var adRequest: AdRequest
    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialAd: MaxInterstitialAd? = null
    private lateinit var adViewTop: MaxAdView
    private lateinit var adViewBottom: MaxAdView

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoardingBinding.inflate(layoutInflater)
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
        when (LogoMakerApp.BOARDING_ACTIVITY_BANNER_TOP) {
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
        when (LogoMakerApp.BOARDING_ACTIVITY_BANNER_BOTTOM) {
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
                binding.applovinAdView1.visibility = View.VISIBLE

                adViewBottom.loadAd()
            }
        }

        //loading interstitial ads
        setAd()
        interstitialAd!!.loadAd()

        //status bar transparent
        MainUtils.makeStatusBarTransparent(this@OnBoardingActivity)

        onboardingAdapter = OnBoardingAdapter(this@OnBoardingActivity)
        binding.viewPager.adapter = onboardingAdapter
        //page animation
        binding.viewPager.setPageTransformer(false, OnboardingPageTransformer())

        addDots(0)

        binding.viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int) {

            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onPageSelected(position: Int) {
                addDots(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        //button click continue
        binding.continueButton.setOnClickListener {

            if (binding.viewPager.currentItem < onboardingAdapter.count - 1) {
                binding.viewPager.setCurrentItem(binding.viewPager.currentItem + 1, true)
            }
            else {
                //setting first run to false because app has run already
                SharedPreference.isFirstRun = false

                if (LogoMakerApp.BOARDING_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "0"){

                    //No Ad
                    //starting the new activity
                    startActivity(Intent(this@OnBoardingActivity, ActivityMain::class.java))
                    finish()
                }
                else if (LogoMakerApp.BOARDING_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "1"){

                    //AdMob Ad
                    if (mInterstitialAd != null) {
                        mInterstitialAd!!.show(this)

                        mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()

                                setAd()

                                //starting the new activity
                                startActivity(Intent(this@OnBoardingActivity, ActivityMain::class.java))
                                finish()
                            }
                        }
                    }
                    else{
                        setAd()

                        //starting the new activity
                        startActivity(Intent(this@OnBoardingActivity, ActivityMain::class.java))
                        finish()
                    }
                }
                else if (LogoMakerApp.BOARDING_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "2"){

                    //AppLovin load
                    if(interstitialAd!!.isReady)
                        interstitialAd!!.showAd()
                    else{
                        //starting the new activity
                        startActivity(Intent(this@OnBoardingActivity, ActivityMain::class.java))
                        finish()
                    }
                }
                else{
                    //starting the new activity
                    startActivity(Intent(this@OnBoardingActivity, ActivityMain::class.java))
                    finish()
                }
            }
        }

        //button click skip
        binding.skipButton.setOnClickListener {

            //setting first run to false because app has run already
            SharedPreference.isFirstRun = false

            if (LogoMakerApp.BOARDING_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "0"){

                //No Ad
                //starting the new activity
                startActivity(Intent(this@OnBoardingActivity, ActivityMain::class.java))
                finish()
            }
            else if (LogoMakerApp.BOARDING_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "1"){

                //AdMOb
                if (mInterstitialAd != null && binding.viewPager.currentItem < onboardingAdapter.count - 1) {
                    mInterstitialAd!!.show(this)

                    mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){

                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()

                            setAd()

                            //starting the new activity
                            startActivity(Intent(this@OnBoardingActivity, ActivityMain::class.java))
                            finish()
                        }
                    }
                }
                else{
                    setAd()

                    //starting the new activity
                    startActivity(Intent(this@OnBoardingActivity, ActivityMain::class.java))
                    finish()
                }
            }
            else if (LogoMakerApp.BOARDING_ACTIVITY_BUTTON_CLICK_INTERSTITIAL == "2"){

                //AppLovin load
                if(interstitialAd!!.isReady && binding.viewPager.currentItem < onboardingAdapter.count - 1)
                    interstitialAd!!.showAd()
                else{
                    interstitialAd!!.loadAd()
                    //starting the new activity
                    startActivity(Intent(this@OnBoardingActivity, ActivityMain::class.java))
                    finish()
                }
            }
            else{
                //starting the new activity
                startActivity(Intent(this@OnBoardingActivity, ActivityMain::class.java))
                finish()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun addDots(position: Int) {
        val tvDots = arrayOfNulls<TextView>(5)
        val htmlString = "&#8226;"
        binding.layoutDots.removeAllViews()
        for (i in tvDots.indices) {
            tvDots[i] = TextView(this@OnBoardingActivity)
            tvDots[i]!!.text = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY)
            tvDots[i]!!.textSize = 35f
            tvDots[i]!!.setTextColor(ContextCompat.getColor(this@OnBoardingActivity, R.color.white))
            binding.layoutDots.addView(tvDots[i])
        }
        if (tvDots.isNotEmpty()) {
            tvDots[position]!!.setTextColor(
                ContextCompat.getColor(
                    this@OnBoardingActivity,
                    R.color.theme_color
                )
            )
        }
    }

    override fun onAdLoaded(p0: MaxAd?) {}
    override fun onAdDisplayed(p0: MaxAd?) {}
    override fun onAdHidden(p0: MaxAd?) {

        interstitialAd!!.loadAd()
        //starting the new activity
        startActivity(Intent(this@OnBoardingActivity, ActivityMain::class.java))
        finish()

    }
    override fun onAdClicked(p0: MaxAd?) {}
    override fun onAdLoadFailed(p0: String?, p1: MaxError?) {}
    override fun onAdDisplayFailed(p0: MaxAd?, p1: MaxError?) {}
    override fun onAdExpanded(p0: MaxAd?) {
    }
    override fun onAdCollapsed(p0: MaxAd?) {
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
        adViewBottom.setListener(this)
        //preparing the AdView
        adViewBottom.layoutParams = binding.applovinAdView2.layoutParams

        binding.applovinAdView1.addView(adViewBottom)
    }
}