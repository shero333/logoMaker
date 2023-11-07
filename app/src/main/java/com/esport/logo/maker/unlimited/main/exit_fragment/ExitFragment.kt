package com.esport.logo.maker.unlimited.main.exit_fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinSdk
import com.esport.logo.maker.unlimited.BuildConfig
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.application.LogoMakerApp
import com.esport.logo.maker.unlimited.databinding.FragmentExitBinding
import com.esport.logo.maker.unlimited.main.ActivityMain
import com.esport.logo.maker.unlimited.utils.MainUtils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlin.properties.Delegates


class ExitFragment : Fragment(), MaxAdListener, MaxAdViewAdListener {

    private lateinit var binding: FragmentExitBinding
    private var ratingUser by Delegates.notNull<Float>()
    private lateinit var adRequest: AdRequest
    private lateinit var adViewTop: MaxAdView
    private lateinit var adViewBottom: MaxAdView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentExitBinding.inflate(inflater, container, false)

        //Initializing Google Ads
        MobileAds.initialize(requireContext())
        AppLovinSdk.initializeSdk(requireContext())

        //Initializing AppLovin Ads
        AppLovinSdk.getInstance(requireContext()).mediationProvider = "max"
        AppLovinSdk.initializeSdk(requireContext())

        adRequest = AdRequest.Builder().build()

        //Creating banner for AppLovin
        BannerAdAppLovinTop()
        BannerAdAppLovinBottom()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //top Ad
        when (LogoMakerApp.EXIT_SCREEN_BANNER_TOP) {
            "0" -> {

                //no ad will be loaded
                binding.adaptiveBanner1.visibility = View.GONE
                binding.adaptiveBanner2.visibility = View.GONE
                binding.applovinAdView1.visibility = View.GONE
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
        when (LogoMakerApp.EXIT_SCREEN_BANNER_BOTTOM) {
            "0" -> {

                //no ad will be loaded
                binding.adaptiveBanner1.visibility = View.GONE
                binding.applovinAdView1.visibility = View.GONE

            }

            "1" -> {

                //Admob Ad will be loaded
                binding.adaptiveBanner1.visibility = View.INVISIBLE
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

        val ratingBarB = binding.ratingBar
        ratingBarB.numStars = 5
        ratingBarB.setMinimumStars(1f)
        ratingBarB.setIsIndicator(false)
        ratingBarB.isClickable = true
        ratingBarB.isScrollable = true
        ratingBarB.isClearRatingEnabled = true
        ratingBarB.setEmptyDrawableRes(R.drawable.empty_star)
        ratingBarB.setFilledDrawableRes(R.drawable.filled_star)

        //get the rating
        ratingBarB.setOnRatingChangeListener { ratingBar, rating, fromUser ->

            //send the rating to the play store
            ratingUser = rating
            ratingBarB.rating = rating

            // Delay the Play Store redirection to display  the rating on the rating bar
            ratingBar.postDelayed(Runnable {

                if (rating <= 3)
                //open mail
                    openGmailForRating(rating)
                else
                //open playStore
                    openPlayStoreForRating()

            },700)

        }

        //back button
        binding.backButtonMain.setOnClickListener {

            //finish the fragment
            // Remove the fragment from the activity
            MainUtils.finishFragment(requireActivity().supportFragmentManager, this@ExitFragment)
        }

        binding.exitButtonMain.setOnClickListener {

            //open playStore for the rating
            requireActivity().finishAffinity()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (activity is ActivityMain) {
            (activity as ActivityMain).showButtons() // Call a method in your activity to show the buttons
        }
    }

    private fun openGmailForRating(rating: Float) {
        val recipientEmail = "testFunprim@gmail.com"
        val subject = "Its just implementation and testing"
        val message =
            "I would like to rate ${rating.toInt()}/5 Esport logo maker, The reason for this rating is"

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data =
            Uri.parse("mailto:") // This opens the "compose" screen of the default email app
        intent.putExtra(
            Intent.EXTRA_EMAIL,
            arrayOf(recipientEmail)
        ) // Set the recipient email address
        intent.putExtra(Intent.EXTRA_SUBJECT, subject) // Set the email subject

        startActivity(intent)

        //closing the fragment
        MainUtils.finishFragment(requireActivity().supportFragmentManager, this@ExitFragment)
    }

    private fun openPlayStoreForRating() {
        try {
            val uri = Uri.parse("market://details?id=" + requireActivity().packageName)
            val rateIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(rateIntent)
        } catch (e: ActivityNotFoundException) {
            val uri =
                Uri.parse("https://play.google.com/store/apps/details?id=" + requireActivity().packageName)
            val rateIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(rateIntent)
        }

        //closing the fragment
        MainUtils.finishFragment(requireActivity().supportFragmentManager, this@ExitFragment)
    }

    override fun onAdLoaded(p0: MaxAd?) {}
    override fun onAdDisplayed(p0: MaxAd?) {}
    override fun onAdHidden(p0: MaxAd?) {}
    override fun onAdClicked(p0: MaxAd?) {}
    override fun onAdLoadFailed(p0: String?, p1: MaxError?) {}
    override fun onAdDisplayFailed(p0: MaxAd?, p1: MaxError?) {}
    override fun onAdExpanded(p0: MaxAd?) {}
    override fun onAdCollapsed(p0: MaxAd?) {}

    //Ads
    private fun Banner1Ads() {
        if (BuildConfig.DEBUG) {

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(requireContext(), resources)
            val adView = AdView(requireContext())
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner1.addView(adView)
            adView.loadAd(adRequest)
        } else {

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(requireContext(), resources)
            val adView = AdView(requireContext())
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_RELEASE
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner1.addView(adView)
            adView.loadAd(adRequest)
        }
    }

    private fun Banner2Ads() {

        if (BuildConfig.DEBUG) {

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(requireContext(), resources)
            val adView = AdView(requireContext())
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner2.addView(adView)
            adView.loadAd(adRequest)
        } else {

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(requireContext(), resources)
            val adView = AdView(requireContext())
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_RELEASE
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner2.addView(adView)
            adView.loadAd(adRequest)
        }
    }

    private fun BannerAdAppLovinTop() {

        adViewTop = MaxAdView(resources.getString(R.string.bannerAd), requireContext())
        adViewTop.setListener(this)
        //preparing the AdView
        adViewTop.layoutParams = binding.applovinAdView2.layoutParams

        binding.applovinAdView2.addView(adViewTop)
    }

    private fun BannerAdAppLovinBottom() {

        adViewBottom = MaxAdView(resources.getString(R.string.bannerAd), requireContext())
        adViewBottom.setListener(this)
        //preparing the AdView
        adViewBottom.layoutParams = binding.applovinAdView2.layoutParams

        binding.applovinAdView1.addView(adViewBottom)
    }
}