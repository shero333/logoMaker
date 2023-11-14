package com.esport.logo.maker.unlimited.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdRevenueListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.applovin.sdk.AppLovinSdk
import com.esport.logo.maker.unlimited.BuildConfig
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.application.LogoMakerApp
import com.esport.logo.maker.unlimited.databinding.ActivityMainBinding
import com.esport.logo.maker.unlimited.main.adapterRecentMainList.RecentMainListAdapter
import com.esport.logo.maker.unlimited.main.edit_create_logo.CreateOrEditTemplateActivity
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.SavedLogo
import com.esport.logo.maker.unlimited.main.exit_fragment.ExitFragment
import com.esport.logo.maker.unlimited.main.recent_work.RecentListActivity
import com.esport.logo.maker.unlimited.preferences.SharedPreference
import com.esport.logo.maker.unlimited.utils.MainUtils
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.errorprone.annotations.Keep
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Type

@AndroidEntryPoint
class ActivityMain : AppCompatActivity(), RecentMainListAdapter.RecentItemClicked, MaxAdListener,
    MaxAdViewAdListener, MaxAdRevenueListener {

    private lateinit var nativeAdContainerView: ViewGroup
    private var nativeAdLoader: MaxNativeAdLoader? = null
    private var loadedNativeAd: MaxAd? = null
    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var adapterRecentList: RecentMainListAdapter
    private lateinit var adRequest: AdRequest
    private lateinit var template: TemplateView
    private lateinit var adloader: AdLoader
    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialAd: MaxInterstitialAd? = null
    private var templateClicked = false
    private var itemRecentsClicked = false
    private lateinit var adViewTop: MaxAdView
    private lateinit var adViewBottom: MaxAdView
    private lateinit var exitFragment: ExitFragment
    private val MAIL_OR_PLAY_STORE_INTENT_REQ_CODE = 200

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingMain = ActivityMainBinding.inflate(layoutInflater)

        setContentView(bindingMain.root)

        //Initializing Google Ads
        MobileAds.initialize(this)
        AppLovinSdk.initializeSdk(this)

        //Initializing AppLovin Ads
        AppLovinSdk.getInstance(this).mediationProvider = "max"
        AppLovinSdk.getInstance(this).initializeSdk {
        }

        adRequest = AdRequest.Builder().build()

        template = bindingMain.adTemplate

        //AppLovin
        interstitialAd = MaxInterstitialAd(resources.getString(R.string.interstitialAd), this)
        interstitialAd!!.setListener(this)

        //Creating banner for AppLovin
        BannerAdAppLovinTop()
        BannerAdAppLovinBottom()

        //instance to kill and call the fragment from this activity
        exitFragment = ExitFragment()

        //top Ad
        when (LogoMakerApp.MAIN_SCREEN_BANNER_TOP) {
            "0" -> {

                //no ad will be loaded
                bindingMain.adaptiveBanner2.visibility = View.GONE
                bindingMain.applovinAdView2.visibility = View.GONE

                val params = bindingMain.logoText.layoutParams as ConstraintLayout.LayoutParams

                // Set margin top and margin end in DP (change these values as needed)
                val marginTopDp = 35 // Top margin in DP
                val marginStartDp = 20 // End (right) margin in DP

                // Convert DP to pixels
                val density = resources.displayMetrics.density
                val marginTopPx = (marginTopDp * density).toInt()
                val marginStartPx = (marginStartDp * density).toInt()

                // Set the margins
                params.topMargin = marginTopPx
                params.marginStart = marginStartPx

                bindingMain.logoText.layoutParams = params
            }

            "1" -> {

                //Admob Ad will be loaded
                bindingMain.adaptiveBanner2.visibility = View.VISIBLE
                bindingMain.applovinAdView2.visibility = View.INVISIBLE

                //google banner ad load
                //bottom
                Banner1Ads()
                //top
                Banner2Ads()

            }

            "2" -> {

                //load AppLovin Banner Ads
                bindingMain.adaptiveBanner2.visibility = View.INVISIBLE
                bindingMain.applovinAdView2.visibility = View.VISIBLE

                adViewTop.loadAd()
            }
        }

        //bottom Ad
        when (LogoMakerApp.MAIN_SCREEN_BANNER_BOTTOM) {
            "0" -> {

                //no ad will be loaded
                bindingMain.adaptiveBanner1.visibility = View.GONE
                bindingMain.applovinAdView1.visibility = View.GONE

            }

            "1" -> {

                //Admob Ad will be loaded
                bindingMain.adaptiveBanner1.visibility = View.VISIBLE
                bindingMain.applovinAdView1.visibility = View.INVISIBLE

                //google banner ad load
                //bottom
                Banner1Ads()
                //top
                Banner2Ads()

            }

            "2" -> {

                //load AppLovin Banner Ads
                bindingMain.adaptiveBanner1.visibility = View.INVISIBLE
                bindingMain.applovinAdView1.visibility = View.VISIBLE

                adViewBottom.loadAd()
            }
        }

        setAd()
        interstitialAd!!.loadAd()

        //loading native ad
        when (LogoMakerApp.MAIN_SCREEN_NATIVE_AD) {
            "0" -> {
                //no ad
                bindingMain.adTemplate.visibility = View.GONE
                bindingMain.appLovinNativeAdView.visibility = View.GONE
            }

            "1" -> {

                bindingMain.adTemplate.visibility = View.VISIBLE
                bindingMain.appLovinNativeAdView.visibility = View.GONE

                if (BuildConfig.DEBUG) {
                    //native ads show AdMob
                    adloader = AdLoader.Builder(this, LogoMakerApp.NATIVE_AD_ADMOB_ID_DEBUG)
                        .forNativeAd { nativeAd: NativeAd? ->

                            template.setStyles(NativeTemplateStyle.Builder().build())
                            template.setNativeAd(nativeAd)
                        }
                        .build()

                    adloader.loadAd(AdRequest.Builder().build())

                } else {
                    //native ads show AdMob
                    adloader = AdLoader.Builder(this, LogoMakerApp.NATIVE_AD_ADMOB_ID_RELEASE)
                        .forNativeAd { nativeAd: NativeAd? ->
                            template.setStyles(NativeTemplateStyle.Builder().build())
                            template.setNativeAd(nativeAd)
                        }
                        .build()

                    adloader.loadAd(AdRequest.Builder().build())
                }

            }

            "2" -> {
                bindingMain.adTemplate.visibility = View.INVISIBLE
                bindingMain.appLovinNativeAdView.visibility = View.VISIBLE
                //native ads show AppLovin
                createNativeAdLoader()
            }
        }

        adapterRecentList = RecentMainListAdapter(this)

        //setting status bar color
        MainUtils.statusBarColor(this@ActivityMain)

        //towards create logo flow
        bindingMain.createButton.setOnClickListener {

            templateClicked = false

            if (LogoMakerApp.MAINSCREEN_CREATELOGO_BUTTON_INTERSTITIAL == "0") {

                setAd()
                //create logo flow
                val intent = Intent(this@ActivityMain, CreateOrEditTemplateActivity::class.java)
                intent.putExtra("logosList", "createLogo")
                startActivity(intent)
            } else if (LogoMakerApp.MAINSCREEN_CREATELOGO_BUTTON_INTERSTITIAL == "1") {

                //Admob
                if (mInterstitialAd != null) {
                    mInterstitialAd!!.show(this)

                    mInterstitialAd!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()

                                setAd()
                                //create logo flow
                                val intent = Intent(
                                    this@ActivityMain,
                                    CreateOrEditTemplateActivity::class.java
                                )
                                intent.putExtra("logosList", "createLogo")
                                startActivity(intent)
                            }
                        }
                } else {
                    setAd()
                    //create logo flow
                    val intent = Intent(this@ActivityMain, CreateOrEditTemplateActivity::class.java)
                    intent.putExtra("logosList", "createLogo")
                    startActivity(intent)
                }
            } else if (LogoMakerApp.MAINSCREEN_CREATELOGO_BUTTON_INTERSTITIAL == "2") {

                //Applovin
                if (interstitialAd!!.isReady)
                    interstitialAd!!.showAd()
                else {
                    //loading next ad
                    interstitialAd!!.loadAd()
                    //create logo flow
                    val intent = Intent(this@ActivityMain, CreateOrEditTemplateActivity::class.java)
                    intent.putExtra("logosList", "createLogo")
                    startActivity(intent)
                }
            } else {
                //create logo flow
                val intent = Intent(this@ActivityMain, CreateOrEditTemplateActivity::class.java)
                intent.putExtra("logosList", "createLogo")
                startActivity(intent)
            }
        }

        //towards create logo flow
        bindingMain.templatesButton.setOnClickListener {

            templateClicked = true

            if (LogoMakerApp.MAINSCREEN_CREATELOGO_BUTTON_INTERSTITIAL == "0") {

                //template of logo flow
                val intent = Intent(this@ActivityMain, CreateOrEditTemplateActivity::class.java)
                intent.putExtra("logosList", "editTemplate")
                startActivity(intent)
            } else if (LogoMakerApp.MAINSCREEN_CREATELOGO_BUTTON_INTERSTITIAL == "1") {

                //load AdMob ad
                if (mInterstitialAd != null) {
                    mInterstitialAd!!.show(this)

                    mInterstitialAd!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()

                                setAd()
                                //template of logo flow
                                val intent = Intent(
                                    this@ActivityMain,
                                    CreateOrEditTemplateActivity::class.java
                                )
                                intent.putExtra("logosList", "editTemplate")
                                startActivity(intent)
                            }
                        }
                } else {
                    setAd()
                    //template of logo flow
                    val intent = Intent(this@ActivityMain, CreateOrEditTemplateActivity::class.java)
                    intent.putExtra("logosList", "editTemplate")
                    startActivity(intent)
                }
            } else if (LogoMakerApp.MAINSCREEN_CREATELOGO_BUTTON_INTERSTITIAL == "2") {

                //Applovin Ad
                if (interstitialAd!!.isReady)
                    interstitialAd!!.showAd()
                else {
                    interstitialAd!!.loadAd()
                    //template of logo flow
                    val intent = Intent(this@ActivityMain, CreateOrEditTemplateActivity::class.java)
                    intent.putExtra("logosList", "editTemplate")
                    startActivity(intent)
                }
            } else {
                //template of logo flow
                val intent = Intent(this@ActivityMain, CreateOrEditTemplateActivity::class.java)
                intent.putExtra("logosList", "editTemplate")
                startActivity(intent)
            }

        }

        //recycler view
        bindingMain.recentsList.layoutManager =
            GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        bindingMain.recentsList.setHasFixedSize(true)
        bindingMain.recentsList.adapter = adapterRecentList

        //list of recent images set the recyclerview here
        // Get the current time
        val now = Calendar.getInstance()
        //now find the recent entries from the list
        // Calculate the milliseconds in a full day
        val millisecondsInADay = 24 * 60 * 60 * 1000L

        // Calculate milliseconds in the upcoming 24 hours
        val todayMillis = millisecondsInADay - (now.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000L
                + now.get(Calendar.MINUTE) * 60 * 1000L
                + now.get(Calendar.SECOND) * 1000L
                + now.get(Calendar.MILLISECOND))

        // Calculate milliseconds in the past 24 hours
        val yesterdayMillis = now.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000L
        +now.get(Calendar.MINUTE) * 60 * 1000L
        +now.get(Calendar.SECOND) * 1000L
        now.get(Calendar.MILLISECOND)

        //preparing lists for the recycler views
        if (SharedPreference.downloadedLogos!!.isNotEmpty()) {

            //visibility settlement
            if (getTheListFromRecentInPreference().isNotEmpty()) {

                val recentLogos = ArrayList<SavedLogo>()

                for (logo in getTheListFromRecentInPreference()) {
                    //from now and upcoming 24 hours {Today}
                    if (logo.timeNDate < System.currentTimeMillis() && logo.timeNDate > yesterdayMillis ||
                        logo.timeNDate <= todayMillis && logo.timeNDate >= System.currentTimeMillis()
                    ) {

                        recentLogos.add(logo)
                    }
                }

                //set the recyclerview of the recent logos
                //make the recyclerview visible and set the adapter
                bindingMain.recentsListCard.visibility = View.VISIBLE
                bindingMain.headingListRecent.visibility = View.VISIBLE
                adapterRecentList.setRecentLogosList(recentLogos)
            } else {

                bindingMain.headingListRecent.visibility = View.GONE
                bindingMain.recentsListCard.visibility = View.GONE
            }
        } else {
            bindingMain.headingListRecent.visibility = View.GONE
            bindingMain.recentsListCard.visibility = View.GONE
        }

        //call the fragment exit on back press
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                bindingMain.createButton.visibility = View.GONE
                bindingMain.templatesButton.visibility = View.GONE
                bindingMain.recentsListCard.visibility = View.GONE
                bindingMain.headingListRecent.visibility = View.GONE

                //calling the exit fragment
                MainUtils.replaceFragment(
                    exitFragment,
                    supportFragmentManager,
                    R.id.container_fragment_exit_activity_main
                )

            }
        })

        //runtime 1st check if the connection is available then the call will be sent if not then the button will be visible
        if (checkForInternet(this)) {
            bindingMain.adTemplate.visibility = View.VISIBLE
            bindingMain.appLovinNativeAdView.visibility = View.VISIBLE
        }
        else {
            bindingMain.adTemplate.visibility = View.GONE
            bindingMain.appLovinNativeAdView.visibility = View.GONE
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
                    bindingMain.adTemplate.visibility = View.VISIBLE
                    bindingMain.appLovinNativeAdView.visibility = View.VISIBLE
                }
            }

            // lost network connection
            override fun onLost(network: Network) {
                super.onLost(network)

                runOnUiThread {
                    //loading banner ads according to the firebase
                    bindingMain.adTemplate.visibility = View.GONE
                    bindingMain.appLovinNativeAdView.visibility = View.GONE
                }
            }
        }

        val connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()

        //recycler view
        bindingMain.recentsList.layoutManager =
            GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        bindingMain.recentsList.setHasFixedSize(true)
        bindingMain.recentsList.adapter = adapterRecentList

        //list of recent images set the recyclerview here
        // Get the current time
        val now = Calendar.getInstance()
        //now find the recent entries from the list
        // Calculate the milliseconds in a full day
        val millisecondsInADay = 24 * 60 * 60 * 1000L

        // Calculate milliseconds in the upcoming 24 hours
        val todayMillis = millisecondsInADay - (now.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000L
                + now.get(Calendar.MINUTE) * 60 * 1000L
                + now.get(Calendar.SECOND) * 1000L
                + now.get(Calendar.MILLISECOND))

        // Calculate milliseconds in the past 24 hours
        val yesterdayMillis = now.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000L
        +now.get(Calendar.MINUTE) * 60 * 1000L
        +now.get(Calendar.SECOND) * 1000L
        now.get(Calendar.MILLISECOND)


        //preparing lists for the recycler views
        if (SharedPreference.downloadedLogos!!.isNotEmpty()) {

            if (getTheListFromRecentInPreference().isNotEmpty()) {

                val recentLogos = ArrayList<SavedLogo>()

                for (logo in getTheListFromRecentInPreference()) {
                    //from now and upcoming 24 hours {Today}
                    if (logo.timeNDate < System.currentTimeMillis() && logo.timeNDate > yesterdayMillis ||
                        logo.timeNDate <= todayMillis && logo.timeNDate >= System.currentTimeMillis()
                    ) {

                        recentLogos.add(logo)
                    }
                }

                //set the recyclerview of the recent logos
                //make the recyclerview visible and set the adapter
                bindingMain.recentsListCard.visibility = View.VISIBLE
                bindingMain.headingListRecent.visibility = View.VISIBLE
                adapterRecentList.setRecentLogosList(recentLogos)
            } else {
                bindingMain.recentsListCard.visibility = View.GONE
                bindingMain.headingListRecent.visibility = View.GONE
            }
        } else {
            bindingMain.recentsListCard.visibility = View.GONE
            bindingMain.headingListRecent.visibility = View.GONE
        }

        //top Ad
        when (LogoMakerApp.MAIN_SCREEN_BANNER_TOP) {
            "0" -> {

                //no ad will be loaded
                bindingMain.adaptiveBanner2.visibility = View.GONE
                bindingMain.applovinAdView2.visibility = View.GONE

                val params = bindingMain.logoText.layoutParams as ConstraintLayout.LayoutParams

                // Set margin top and margin end in DP (change these values as needed)
                val marginTopDp = 35 // Top margin in DP
                val marginStartDp = 20 // End (right) margin in DP

                // Convert DP to pixels
                val density = resources.displayMetrics.density
                val marginTopPx = (marginTopDp * density).toInt()
                val marginStartPx = (marginStartDp * density).toInt()

                // Set the margins
                params.topMargin = marginTopPx
                params.marginStart = marginStartPx

                bindingMain.logoText.layoutParams = params
            }

            "1" -> {

                //Admob Ad will be loaded
                bindingMain.adaptiveBanner2.visibility = View.VISIBLE
                bindingMain.applovinAdView2.visibility = View.INVISIBLE

                //google banner ad load
                //bottom
                Banner1Ads()
                //top
                Banner2Ads()

            }

            "2" -> {

                //load AppLovin Banner Ads
                bindingMain.adaptiveBanner2.visibility = View.INVISIBLE
                bindingMain.applovinAdView2.visibility = View.VISIBLE

                adViewTop.loadAd()
            }
        }

        //bottom Ad
        when (LogoMakerApp.MAIN_SCREEN_BANNER_BOTTOM) {
            "0" -> {

                //no ad will be loaded
                bindingMain.adaptiveBanner1.visibility = View.GONE
                bindingMain.applovinAdView1.visibility = View.GONE

            }

            "1" -> {

                //Admob Ad will be loaded
                bindingMain.adaptiveBanner1.visibility = View.VISIBLE
                bindingMain.applovinAdView1.visibility = View.INVISIBLE

                //google banner ad load
                //bottom
                Banner1Ads()
                //top
                Banner2Ads()

            }

            "2" -> {

                //load AppLovin Banner Ads
                bindingMain.adaptiveBanner1.visibility = View.INVISIBLE
                bindingMain.applovinAdView1.visibility = View.VISIBLE

                adViewBottom.loadAd()
            }
        }

        setAd()
        interstitialAd!!.loadAd()

        //loading native ad
        when (LogoMakerApp.MAIN_SCREEN_NATIVE_AD) {
            "0" -> {
                //no ad will be loaded
                bindingMain.adaptiveBanner1.visibility = View.GONE
                bindingMain.adaptiveBanner2.visibility = View.GONE
                bindingMain.applovinAdView1.visibility = View.GONE
                bindingMain.applovinAdView2.visibility = View.GONE
            }

            "1" -> {

                bindingMain.adTemplate.visibility = View.VISIBLE
                bindingMain.appLovinNativeAdView.visibility = View.INVISIBLE

                if (BuildConfig.DEBUG) {
                    //native ads show AdMob
                    adloader = AdLoader.Builder(this, LogoMakerApp.NATIVE_AD_ADMOB_ID_DEBUG)
                        .forNativeAd { nativeAd: NativeAd? ->
                            template.setStyles(NativeTemplateStyle.Builder().build())
                            template.setNativeAd(nativeAd)
                        }
                        .build()

                    adloader.loadAd(AdRequest.Builder().build())
                } else {
                    //native ads show AdMob
                    adloader = AdLoader.Builder(this, LogoMakerApp.NATIVE_AD_ADMOB_ID_RELEASE)
                        .forNativeAd { nativeAd: NativeAd? ->
                            template.setStyles(NativeTemplateStyle.Builder().build())
                            template.setNativeAd(nativeAd)
                        }
                        .build()

                    adloader.loadAd(AdRequest.Builder().build())
                }

            }

            "2" -> {
                bindingMain.adTemplate.visibility = View.INVISIBLE
                bindingMain.appLovinNativeAdView.visibility = View.VISIBLE
                //native ads show AppLovin
                createNativeAdLoader()
            }
        }

        //preparing lists for the recycler views
        if (SharedPreference.downloadedLogos!!.isNotEmpty()) {

            if (getTheListFromRecentInPreference().isNotEmpty()) {

                val recentLogos = ArrayList<SavedLogo>()

                for (logo in getTheListFromRecentInPreference()) {
                    //from now and upcoming 24 hours {Today}
                    if (logo.timeNDate < System.currentTimeMillis() && logo.timeNDate > yesterdayMillis ||
                        logo.timeNDate <= todayMillis && logo.timeNDate >= System.currentTimeMillis()
                    ) {

                        recentLogos.add(logo)
                    }
                }

                //set the recyclerview of the recent logos
                //make the recyclerview visible and set the adapter
                bindingMain.recentsListCard.visibility = View.VISIBLE
                bindingMain.headingListRecent.visibility = View.VISIBLE
                adapterRecentList.setRecentLogosList(recentLogos)
            } else {
                bindingMain.recentsListCard.visibility = View.GONE
                bindingMain.headingListRecent.visibility = View.GONE
            }
        } else {
            bindingMain.recentsListCard.visibility = View.GONE
            bindingMain.headingListRecent.visibility = View.GONE
        }
    }

    private fun getTheListFromRecentInPreference(): ArrayList<SavedLogo> {

        //get the array string from the preference
        val recentString = SharedPreference.downloadedLogos
        var savedLogos = ArrayList<SavedLogo>()

        if (recentString != null && recentString.isNotEmpty()) {
            val typeData: Type = object : TypeToken<ArrayList<SavedLogo>>() {}.type

            savedLogos = Gson().fromJson(recentString, typeData)
            return savedLogos
        }

        return savedLogos
    }

    override fun recentItemClicked() {

        itemRecentsClicked = true

        if (LogoMakerApp.MAINSCREEN_RECENTLIST_ITEM_CLICK_INTERSTITIAL == "0") {
            //no ad
            //navigate to the activity
            startActivity(Intent(this@ActivityMain, RecentListActivity::class.java))
        }
        else if (LogoMakerApp.MAINSCREEN_RECENTLIST_ITEM_CLICK_INTERSTITIAL == "1") {

            //Loading Ad Mob interstitial
            if (mInterstitialAd != null) {
                mInterstitialAd!!.show(this)

                mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()

                        setAd()
                        //navigate to the activity
                        startActivity(Intent(this@ActivityMain, RecentListActivity::class.java))
                    }
                }
            } else {
                setAd()
                //navigate to the activity
                startActivity(Intent(this@ActivityMain, RecentListActivity::class.java))
            }
        } else if (LogoMakerApp.MAINSCREEN_RECENTLIST_ITEM_CLICK_INTERSTITIAL == "2") {

            //loading Ad AppLovin interstitial
            //Applovin Ad
            if (interstitialAd!!.isReady)
                interstitialAd!!.showAd()
            else {
                //navigate to the activity
                startActivity(Intent(this@ActivityMain, RecentListActivity::class.java))
            }
        }

    }

    fun showButtons() {
        // Set the visibility of your buttons to VISIBLE
        bindingMain.createButton.visibility = View.VISIBLE
        bindingMain.templatesButton.visibility = View.VISIBLE

        if (getTheListFromRecentInPreference().isNotEmpty()) {
            bindingMain.recentsListCard.visibility = View.VISIBLE
            bindingMain.headingListRecent.visibility = View.VISIBLE
        } else {
            bindingMain.recentsListCard.visibility = View.GONE
            bindingMain.headingListRecent.visibility = View.GONE
        }
    }

    //Ads
    private fun Banner1Ads() {
        if (BuildConfig.DEBUG) {

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this, resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG
            adView.setAdSize(adaptiveAdSize)
            bindingMain.adaptiveBanner1.addView(adView)
            adView.loadAd(adRequest)
        } else {

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this, resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_RELEASE
            adView.setAdSize(adaptiveAdSize)
            bindingMain.adaptiveBanner1.addView(adView)
            adView.loadAd(adRequest)
        }
    }

    private fun Banner2Ads() {

        if (BuildConfig.DEBUG) {

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this, resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG
            adView.setAdSize(adaptiveAdSize)
            bindingMain.adaptiveBanner2.addView(adView)
            adView.loadAd(adRequest)
        } else {

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this, resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_RELEASE
            adView.setAdSize(adaptiveAdSize)
            bindingMain.adaptiveBanner2.addView(adView)
            adView.loadAd(adRequest)
        }
    }

    //Interstitial Ad
    private fun setAd() {
        if (BuildConfig.DEBUG) {

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
        } else {

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
        adViewTop.layoutParams = bindingMain.applovinAdView2.layoutParams

        bindingMain.applovinAdView2.addView(adViewTop)
    }

    private fun BannerAdAppLovinBottom() {

        adViewBottom = MaxAdView(resources.getString(R.string.bannerAd), this)
        adViewBottom.setListener(/* p0 = */ this)
        //preparing the AdView
        adViewBottom.layoutParams = bindingMain.applovinAdView2.layoutParams

        bindingMain.applovinAdView1.addView(adViewBottom)
    }

    override fun onAdLoaded(p0: MaxAd?) {}
    override fun onAdDisplayed(p0: MaxAd?) {}
    override fun onAdHidden(p0: MaxAd?) {

        //loading next ad
        interstitialAd!!.loadAd()

        if (templateClicked) {

            templateClicked = false

            //template of logo flow
            val intent = Intent(this@ActivityMain, CreateOrEditTemplateActivity::class.java)
            intent.putExtra("logosList", "editTemplate")
            startActivity(intent)
        } else if (itemRecentsClicked) {

            itemRecentsClicked = false
            //navigate to the activity
            startActivity(Intent(this@ActivityMain, RecentListActivity::class.java))
        } else {
            //create logo flow
            val intent = Intent(this@ActivityMain, CreateOrEditTemplateActivity::class.java)
            intent.putExtra("logosList", "createLogo")
            startActivity(intent)
        }
    }

    override fun onAdClicked(p0: MaxAd?) {}
    override fun onAdLoadFailed(p0: String?, p1: MaxError?) {}
    override fun onAdDisplayFailed(p0: MaxAd?, p1: MaxError?) {}
    override fun onAdExpanded(p0: MaxAd?) {}
    override fun onAdCollapsed(p0: MaxAd?) {}

    //for native Ad library dependency
    override fun onAdRevenuePaid(p0: MaxAd?) {}

    //native Ad show
    private fun createNativeAdView(): MaxNativeAdView {

        val binder =
            MaxNativeAdViewBinder.Builder(R.layout.native_custom_ad_view)
                .setTitleTextViewId(R.id.title_text_view)
                .setBodyTextViewId(R.id.body_text_view)
                .setAdvertiserTextViewId(R.id.advertiser_textView)
                .setIconImageViewId(R.id.icon_image_view)
                .setMediaContentViewGroupId(R.id.media_view_container)
                .setOptionsContentViewGroupId(R.id.ad_options_view)
                .setCallToActionButtonId(R.id.cta_button)
                .build()

        return MaxNativeAdView(binder, this)
    }

    private fun createNativeAdLoader() {

        nativeAdContainerView = bindingMain.appLovinNativeAdView
        nativeAdLoader = MaxNativeAdLoader(resources.getString(R.string.nativeAd), this)
        nativeAdLoader!!.setRevenueListener(this)

        //loading Ad
        nativeAdLoader!!.loadAd(createNativeAdView())

        nativeAdLoader!!.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, nativeAd: MaxAd?) {
                super.onNativeAdLoaded(nativeAdView, nativeAd)

                // Clean up any pre-existing native ad to prevent memory leaks.
                if (loadedNativeAd != null) {
                    nativeAdLoader!!.destroy(loadedNativeAd)
                }

                Log.d("NativeApplovin", "onNativeAdLoaded: ${nativeAd.toString()}")

                loadedNativeAd = nativeAd

                nativeAdContainerView.removeAllViews()
                nativeAdContainerView.addView(nativeAdView)
            }

            override fun onNativeAdLoadFailed(p0: String?, p1: MaxError?) {
                super.onNativeAdLoadFailed(p0, p1)
                Log.d("NativeAppLovin", "onNativeAdFailed: " + p1?.message.toString())

            }

            override fun onNativeAdClicked(p0: MaxAd?) {
                super.onNativeAdClicked(p0)
            }
        })
    }

    @SuppressLint("NewApi")
    private fun checkForInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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