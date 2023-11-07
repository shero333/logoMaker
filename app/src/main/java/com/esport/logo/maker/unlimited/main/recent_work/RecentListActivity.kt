package com.esport.logo.maker.unlimited.main.recent_work

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
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
import com.esport.logo.maker.unlimited.databinding.ActivityRecentsListBinding
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.SavedLogo
import com.esport.logo.maker.unlimited.main.recent_work.adapters.RecentListAdapter
import com.esport.logo.maker.unlimited.main.recent_work.adapters.SpecificDateListAdapter
import com.esport.logo.maker.unlimited.main.recent_work.adapters.YesterdayListAdapter
import com.esport.logo.maker.unlimited.main.recent_work.fragments.PreviewFragment
import com.esport.logo.maker.unlimited.preferences.SharedPreference
import com.esport.logo.maker.unlimited.utils.MainUtils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.Date


class RecentListActivity : AppCompatActivity(), RecentListAdapter.RecentListItemClick,
    YesterdayListAdapter.YesterdayListItemClick, SpecificDateListAdapter.BeforeYesterdayList,
    PreviewFragment.DeleteItemInPreview, MaxAdListener, MaxAdViewAdListener {

    private var beforeyesterdayClicked = false
    private var yesterdayClicked = false
    private var recentClicked = false
    private lateinit var binding: ActivityRecentsListBinding
    private lateinit var previewFragment: PreviewFragment
    private var container :Int = 0
    private val recentLogos = ArrayList<SavedLogo>()
    private val yesterdayLogos = ArrayList<SavedLogo>()
    private val beforeYesterdayLogos = ArrayList<SavedLogo>()
    private lateinit var recentListAdapter: RecentListAdapter
    private lateinit var yesterdayListAdapter: YesterdayListAdapter
    private lateinit var specificDateListAdapter: SpecificDateListAdapter

    private lateinit var adRequest: AdRequest
    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialAd: MaxInterstitialAd? = null
    private var recentLogo:SavedLogo? = null
    private var yesterdayLogo: SavedLogo? = null
    private var beforeYesterdayLogo: SavedLogo? = null
    private lateinit var adViewTop: MaxAdView
    private lateinit var adViewBottom: MaxAdView

    @SuppressLint("NewApi", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecentsListBinding.inflate(layoutInflater)
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

        //initialization of ViewModel


        //Creating banner for AppLovin
        BannerAdAppLovinTop()
        BannerAdAppLovinBottom()

        var backButton = binding.backButton

        //top Ad
        when (LogoMakerApp.RECENTS_ACTIVITY_BANNER_TOP) {
            "0" -> {

                //no ad will be loaded
                binding.adaptiveBanner2.visibility = View.GONE
                binding.applovinAdView2.visibility = View.GONE

                // Create layout params for the skip button
                val layoutParams = backButton.layoutParams as ConstraintLayout.LayoutParams

                // Set the margins in pixels (adjust the values as needed)
                val leftMarginInPixels = resources.getDimensionPixelSize(R.dimen.marginStartDp) // Use your margin resource
                val topMarginInPixels = resources.getDimensionPixelSize(R.dimen.marginTopDp)   // Use your margin resource

                layoutParams.setMargins(leftMarginInPixels, topMarginInPixels, 0, 0)

                backButton.layoutParams = layoutParams

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

                // Create layout params for the skip button
                val layoutParams = backButton.layoutParams as ConstraintLayout.LayoutParams

                // Set the margins in pixels (adjust the values as needed)
                val leftMarginInPixels = resources.getDimensionPixelSize(R.dimen.marginStartDpAd) // Use your margin resource
                val topMarginInPixels = resources.getDimensionPixelSize(R.dimen.marginTopDpAd)   // Use your margin resource

                layoutParams.setMargins(leftMarginInPixels, topMarginInPixels, 0, 0)

                backButton.layoutParams = layoutParams
            }
            "2" -> {

                //load AppLovin Banner Ads
                binding.adaptiveBanner2.visibility = View.INVISIBLE
                binding.applovinAdView2.visibility = View.VISIBLE

                adViewTop.loadAd()

                // Create layout params for the skip button
                val layoutParams = backButton.layoutParams as ConstraintLayout.LayoutParams

                // Set the margins in pixels (adjust the values as needed)
                val leftMarginInPixels = resources.getDimensionPixelSize(R.dimen.marginStartDpAd) // Use your margin resource
                val topMarginInPixels = resources.getDimensionPixelSize(R.dimen.marginTopDpAd)   // Use your margin resource

                layoutParams.setMargins(leftMarginInPixels, topMarginInPixels, 0, 0)

                backButton.layoutParams = layoutParams

            }
        }

        //bottom Ad
        when (LogoMakerApp.RECENTS_ACTIVITY_BANNER_BOTTOM) {
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

        setAd()
        interstitialAd!!.loadAd()



        container = R.id.container_fragment_activity_Recent_Lists

        previewFragment = PreviewFragment(this)

        //setting status bar color
        MainUtils.statusBarColor(this@RecentListActivity)

        binding.backButton.setOnClickListener {
            finish()
        }

        //back pressed button
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        //adapters
        recentListAdapter = RecentListAdapter(this)
        yesterdayListAdapter = YesterdayListAdapter(this)
        specificDateListAdapter = SpecificDateListAdapter(this)

        //setting recyclerview
        //recent list
        binding.recentsList.layoutManager = GridLayoutManager(this,1,GridLayoutManager.HORIZONTAL,false)
        binding.recentsList.setHasFixedSize(true)
        binding.recentsList.adapter = recentListAdapter

        //yesterday
        binding.yesterdayList.layoutManager = GridLayoutManager(this,1,GridLayoutManager.HORIZONTAL,false)
        binding.yesterdayList.setHasFixedSize(true)
        binding.yesterdayList.adapter = yesterdayListAdapter

        //before yesterday
        binding.beforeYesterday.layoutManager = GridLayoutManager(this,1,GridLayoutManager.HORIZONTAL,false)
        binding.beforeYesterday.setHasFixedSize(true)
        binding.beforeYesterday.adapter = specificDateListAdapter



        //make 3 lists for recent, yesterday and the previous day lists
        val currentTimeMillis = System.currentTimeMillis()
        val oneDayMillis = currentTimeMillis - (24 * 60 * 60 * 1000) // Milliseconds in a day
        val yesterdayMillis = currentTimeMillis - oneDayMillis
        val dayBeforeYesterdayMillis = oneDayMillis - yesterdayMillis

        //preparing lists for the recycler views
        if (SharedPreference.downloadedLogos != null){

            for (logo in getTheListFromRecentInPreference()){

                when (logo.timeNDate) {
                    in oneDayMillis until currentTimeMillis -> recentLogos.add(logo)
                    in yesterdayMillis until oneDayMillis -> yesterdayLogos.add(logo)
                    in dayBeforeYesterdayMillis until yesterdayMillis -> beforeYesterdayLogos.add(logo)
                }
            }

            //set the recyclerview of the recent logos
            if (recentLogos.isNotEmpty()){

                binding.headingListRecent.visibility = View.VISIBLE
                binding.recentsListCard.visibility = View.VISIBLE

                //set recent list
                recentListAdapter.setRecentListToRecentAdapter(recentLogos)
                recentListAdapter.notifyDataSetChanged()
            }
            else{
                binding.headingListRecent.visibility = View.GONE
                binding.recentsListCard.visibility = View.GONE
            }

            if (yesterdayLogos.isNotEmpty()){

                binding.headingListYesterday.visibility = View.VISIBLE
                binding.yesterdayListCard.visibility = View.VISIBLE

                //set yesterday list
                yesterdayListAdapter.setYesterdayListToRecentAdapter(yesterdayLogos)
                yesterdayListAdapter.notifyDataSetChanged()
            }
            else{
                binding.headingListYesterday.visibility = View.GONE
                binding.yesterdayListCard.visibility = View.GONE
            }

            if (beforeYesterdayLogos.isNotEmpty()){

                binding.headingListDate.visibility = View.VISIBLE
                binding.beforeYesterdayCard.visibility = View.VISIBLE

                //set beforeYesterdayLogos list
                binding.headingListDate.text = Date(dayBeforeYesterdayMillis).toString()
                specificDateListAdapter.setBeforeYesterdayListToRecentAdapter(beforeYesterdayLogos)
                specificDateListAdapter.notifyDataSetChanged()
            }
            else{
                binding.headingListDate.visibility = View.GONE
                binding.beforeYesterdayCard.visibility = View.GONE
            }
        }
    }

    private fun getTheListFromRecentInPreference(): ArrayList<SavedLogo> {

        //get the array string from the preference
        val recentString = SharedPreference.downloadedLogos

        val typeData: Type? = object : TypeToken<ArrayList<SavedLogo>>() {}.type

        val gson = Gson()
        val savedLogos: ArrayList<SavedLogo> = gson.fromJson(recentString,typeData)

        return savedLogos
    }

    //recent listItem click
    override fun recentListItemClick(recentLogo: SavedLogo) {

        recentClicked = true
        this.recentLogo = recentLogo

        if (LogoMakerApp.RECENTS_ACTIVITY_RECENTLIST_ITEM_CLICK_INTERSTITIAL == "0"){

            //no Ad
            //navigate to the preview fragment
            val bundle = Bundle()
            bundle.putSerializable("logoClicked",recentLogo)
            bundle.putString("typeLogo","recent")
            previewFragment.arguments = bundle
            MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
        }
        else if (LogoMakerApp.RECENTS_ACTIVITY_RECENTLIST_ITEM_CLICK_INTERSTITIAL == "1"){

            //Admob interstitial
            if (mInterstitialAd != null) {
                mInterstitialAd!!.show(this)

                mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()

                        setAd()
                        //navigate to the preview fragment
                        val bundle = Bundle()
                        bundle.putSerializable("logoClicked",recentLogo)
                        bundle.putString("typeLogo","recent")
                        previewFragment.arguments = bundle
                        MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
                    }
                }
            }
            else{
                setAd()
                //navigate to the preview fragment
                val bundle = Bundle()
                bundle.putSerializable("logoClicked",recentLogo)
                bundle.putString("typeLogo","recent")
                previewFragment.arguments = bundle
                MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
            }

        }
        else if (LogoMakerApp.RECENTS_ACTIVITY_RECENTLIST_ITEM_CLICK_INTERSTITIAL == "2"){

            //AppLovin interstitial
            if(interstitialAd!!.isReady)
                interstitialAd!!.showAd()
            else{

                //navigate to the preview fragment
                val bundle = Bundle()
                bundle.putSerializable("logoClicked",recentLogo)
                bundle.putString("typeLogo","recent")
                previewFragment.arguments = bundle
                MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
            }
        }
        else{
            //navigate to the preview fragment
            val bundle = Bundle()
            bundle.putSerializable("logoClicked",recentLogo)
            bundle.putString("typeLogo","recent")
            previewFragment.arguments = bundle
            MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
        }
    }

    //yesterday listItem click
    override fun yesterdayListItemClick(yesterdayLogo: SavedLogo) {

        yesterdayClicked = true
        this.yesterdayLogo = yesterdayLogo

        if (LogoMakerApp.RECENTS_ACTIVITY_RECENTLIST_ITEM_CLICK_INTERSTITIAL == "0"){

            //no Ad
            //navigate to the preview fragment
            val bundle = Bundle()
            bundle.putSerializable("logoClicked",yesterdayLogo)
            bundle.putString("typeLogo","yesterday")
            previewFragment.arguments = bundle
            MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
        }
        else if (LogoMakerApp.RECENTS_ACTIVITY_RECENTLIST_ITEM_CLICK_INTERSTITIAL == "1"){

            //Admob Ad
            if (mInterstitialAd != null) {
                mInterstitialAd!!.show(this)

                mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()

                        setAd()
                        //navigate to the preview fragment
                        val bundle = Bundle()
                        bundle.putSerializable("logoClicked",yesterdayLogo)
                        bundle.putString("typeLogo","yesterday")
                        previewFragment.arguments = bundle
                        MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
                    }
                }
            }
            else{
                setAd()
                //navigate to the preview fragment
                val bundle = Bundle()
                bundle.putSerializable("logoClicked",yesterdayLogo)
                bundle.putString("typeLogo","yesterday")
                previewFragment.arguments = bundle
                MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
            }
        }
        else if (LogoMakerApp.RECENTS_ACTIVITY_RECENTLIST_ITEM_CLICK_INTERSTITIAL == "2"){

            //AppLovin Ad
            if(interstitialAd!!.isReady)
                interstitialAd!!.showAd()
            else{
                //navigate to the preview fragment
                val bundle = Bundle()
                bundle.putSerializable("logoClicked",yesterdayLogo)
                bundle.putString("typeLogo","yesterday")
                previewFragment.arguments = bundle
                MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
            }
        }
        else{
            //navigate to the preview fragment
            val bundle = Bundle()
            bundle.putSerializable("logoClicked",yesterdayLogo)
            bundle.putString("typeLogo","yesterday")
            previewFragment.arguments = bundle
            MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
        }
    }

    override fun beforeYesterdayItemClick(beforeYesterdayLogo: SavedLogo) {

        beforeyesterdayClicked = true
        this.beforeYesterdayLogo = beforeYesterdayLogo

        if (LogoMakerApp.RECENTS_ACTIVITY_RECENTLIST_ITEM_CLICK_INTERSTITIAL == "0"){

            //No Ad
            //navigate to the preview fragment
            val bundle = Bundle()
            bundle.putSerializable("logoClicked",beforeYesterdayLogo)
            bundle.putString("typeLogo","beforeYesterday")
            previewFragment.arguments = bundle
            MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)

        }
        else if (LogoMakerApp.RECENTS_ACTIVITY_RECENTLIST_ITEM_CLICK_INTERSTITIAL == "1"){

            //AdMob Ad
            if (mInterstitialAd != null) {
                mInterstitialAd!!.show(this)

                mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()

                        setAd()
                        //navigate to the preview fragment
                        val bundle = Bundle()
                        bundle.putSerializable("logoClicked",beforeYesterdayLogo)
                        bundle.putString("typeLogo","beforeYesterday")
                        previewFragment.arguments = bundle
                        MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
                    }
                }
            }
            else{
                setAd()
                //navigate to the preview fragment
                val bundle = Bundle()
                bundle.putSerializable("logoClicked",beforeYesterdayLogo)
                bundle.putString("typeLogo","beforeYesterday")
                previewFragment.arguments = bundle
                MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
            }

        }
        else if (LogoMakerApp.RECENTS_ACTIVITY_RECENTLIST_ITEM_CLICK_INTERSTITIAL == "2"){

            //AppLovin Ad
            if(interstitialAd!!.isReady)
                interstitialAd!!.showAd()
            else{
                //navigate to the preview fragment
                val bundle = Bundle()
                bundle.putSerializable("logoClicked",beforeYesterdayLogo)
                bundle.putString("typeLogo","beforeYesterday")
                previewFragment.arguments = bundle
                MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
            }
        }
        else{
            //navigate to the preview fragment
            val bundle = Bundle()
            bundle.putSerializable("logoClicked",beforeYesterdayLogo)
            bundle.putString("typeLogo","beforeYesterday")
            previewFragment.arguments = bundle
            MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
        }
    }

    override fun deletePreviewItemClicked(clickedLogo:SavedLogo,typeLogo:String) {

        //check from which list the entry should be deleted
        if (typeLogo == "recent"){

            //delete item from recent list and reset the adapter here
            if (recentLogos.isNotEmpty()){

                //deleted element from the list of recent logos
                recentLogos.remove(clickedLogo)

                //resetting the list
                binding.recentsList.layoutManager = GridLayoutManager(this,1,GridLayoutManager.HORIZONTAL,false)
                binding.recentsList.setHasFixedSize(true)
                binding.recentsList.adapter = recentListAdapter
            }

        }
        else if (typeLogo == "yesterday"){

            //delete item from yesterday list and reset the adapter here
            if (yesterdayLogos.isNotEmpty()){

                //deleted element from the list of recent logos
                yesterdayLogos.remove(clickedLogo)
                //resetting the list
                binding.yesterdayList.layoutManager = GridLayoutManager(this,1,GridLayoutManager.HORIZONTAL,false)
                binding.yesterdayList.setHasFixedSize(true)
                binding.yesterdayList.adapter = yesterdayListAdapter
            }
        }
        else if (typeLogo == "beforeYesterday"){

            //delete item from beforeYesterday list and reset the adapter here
            if (beforeYesterdayLogos.isNotEmpty()){

                //deleted element from the list of recent logos
                beforeYesterdayLogos.remove(clickedLogo)
                //resetting the list
                binding.beforeYesterday.layoutManager = GridLayoutManager(this,1,GridLayoutManager.HORIZONTAL,false)
                binding.beforeYesterday.setHasFixedSize(true)
                binding.beforeYesterday.adapter = specificDateListAdapter
            }
        }

        //updating the list in preference
        val updatedList = recentLogos + yesterdayLogos + beforeYesterdayLogos
        val gson  = Gson()
        //updating list in preference
        SharedPreference.downloadedLogos = gson.toJson(updatedList)
    }

    override fun onAdLoaded(p0: MaxAd?) {}
    override fun onAdDisplayed(p0: MaxAd?) { TODO("Not yet implemented") }
    override fun onAdHidden(p0: MaxAd?) {

        //loading next ad
        interstitialAd!!.loadAd()

        if (recentClicked){
            recentClicked = false
            //navigate to the preview fragment
            val bundle = Bundle()
            bundle.putSerializable("logoClicked",recentLogo)
            bundle.putString("typeLogo","recent")
            previewFragment.arguments = bundle
            MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
        }
        else if (yesterdayClicked){
            yesterdayClicked = true
            //navigate to the preview fragment
            val bundle = Bundle()
            bundle.putSerializable("logoClicked",yesterdayLogo)
            bundle.putString("typeLogo","yesterday")
            previewFragment.arguments = bundle
            MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)
        }
        else if (beforeyesterdayClicked){

            beforeyesterdayClicked = false
            //navigate to the preview fragment
            val bundle = Bundle()
            bundle.putSerializable("logoClicked",beforeYesterdayLogo)
            bundle.putString("typeLogo","beforeYesterday")
            previewFragment.arguments = bundle
            MainUtils.replaceFragment(previewFragment,supportFragmentManager,container)

        }
    }
    override fun onAdClicked(p0: MaxAd?) {}
    override fun onAdLoadFailed(p0: String?, p1: MaxError?) {}
    override fun onAdDisplayFailed(p0: MaxAd?, p1: MaxError?) {}
    override fun onAdExpanded(p0: MaxAd?) {
        TODO("Not yet implemented")
    }

    override fun onAdCollapsed(p0: MaxAd?) {
        TODO("Not yet implemented")
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
}