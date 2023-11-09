package com.esport.logo.maker.unlimited.main.recent_work.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
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
import com.esport.logo.maker.unlimited.databinding.FragmentPreviewBinding
import com.esport.logo.maker.unlimited.main.edit_create_logo.CreateOrEditTemplateActivity
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.SavedLogo
import com.esport.logo.maker.unlimited.utils.MainUtils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.io.File


class PreviewFragment(
    private var deleteItem: DeleteItemInPreview,
    private var backButton: BackButtonClickEvent) : Fragment(),
    MaxAdListener, MaxAdViewAdListener {

    private lateinit var binding: FragmentPreviewBinding
    private var clickedLogo: SavedLogo? = null
    private var pathImagePreview: File? = null
    private lateinit var typeLogoClicked: String
    private lateinit var dialogExit: Dialog
    private lateinit var adRequest: AdRequest
    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialAd: MaxInterstitialAd? = null
    private lateinit var adViewTop: MaxAdView
    private lateinit var adViewBottom: MaxAdView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // Inflate the layout for this fragment
        binding = FragmentPreviewBinding.inflate(inflater, container, false)

        //got the clicked logos
        if (requireArguments().getSerializable("logoClicked") != null)
            clickedLogo =  requireArguments().getSerializable("logoClicked") as SavedLogo

        if (requireArguments().getSerializable("itemSentMain") != null)
            pathImagePreview = requireArguments().getSerializable("itemSentMain") as File

        typeLogoClicked = requireArguments().getString("typeLogo", "")

        //Exit dialog
        dialogExit = Dialog(requireContext())
        dialogExit.setContentView(R.layout.exit_dialog_layout)
        dialogExit.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogExit.setCancelable(false)

        //Initializing Google Ads
        MobileAds.initialize(requireContext())
        AppLovinSdk.initializeSdk(requireContext())

        //Initializing AppLovin Ads
        AppLovinSdk.getInstance(requireContext()).mediationProvider = "max"
        AppLovinSdk.initializeSdk(requireContext())

        adRequest = AdRequest.Builder().build()

        //AppLovin
        interstitialAd = MaxInterstitialAd(resources.getString(R.string.interstitialAd), requireActivity())
        interstitialAd!!.setListener(this)

        //Creating banner for AppLovin
        BannerAdAppLovinTop()
        BannerAdAppLovinBottom()

        //display the image
        return binding.root
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                //top
                Banner2Ads()
                //Bottom
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
                binding.applovinAdView1.visibility = View.VISIBLE

                adViewBottom.loadAd()
            }
        }

        setAd()
        interstitialAd!!.loadAd()

        //on click listener
        binding.backButton.setOnClickListener {

            backButton.backButtonClicked()
        }

        //delete the clicked item
        binding.deleteButton.setOnClickListener {

            deleteDialog()
        }

        //get the arguments of fragment and set the image to the imageview
        if (pathImagePreview != null){

            binding.deleteButton.visibility = View.GONE
            binding.logoPreview.setImageBitmap(BitmapFactory.decodeFile(pathImagePreview!!.absolutePath))
        }
        else{
            binding.deleteButton.visibility = View.VISIBLE
            if (clickedLogo != null)
                binding.logoPreview.setImageBitmap(BitmapFactory.decodeFile(clickedLogo!!.filePath))
        }

        //button to share the apps
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
                Toast.makeText(requireContext(), "WhatsApp is not installed.", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Snapchat is not installed.", Toast.LENGTH_SHORT).show()
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
            if (shareIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(chooser)
            } else {
                // Handle the case where no apps can handle the intent
                Toast.makeText(
                    requireContext(),
                    "No apps available to handle this action.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        //back pressed button
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                backButton.backButtonClicked()
            }
        })
    }

    //exit dialog function
    @SuppressLint("SetTextI18n")
    private fun deleteDialog() {

        val done = dialogExit.findViewById<AppCompatButton>(R.id.exit_button)
        val cancel = dialogExit.findViewById<AppCompatButton>(R.id.cancel_button)
        val message = dialogExit.findViewById<AppCompatTextView>(R.id.exit_dialog_message)

        //updating the view for this dialog
        done.text = "DONE"
        cancel.text = "CANCEL"
        message.text = "Are your sure you want to delete?"

        //delete button
        done.setOnClickListener {

            //deleting from the list the item here
            if (clickedLogo != null)
                deleteItem.deletePreviewItemClicked(clickedLogo!!, typeLogoClicked)

            MainUtils.finishFragment(requireActivity().supportFragmentManager,this@PreviewFragment)

            dialogExit.dismiss()

            MainUtils.finishFragment(requireActivity().supportFragmentManager,this@PreviewFragment)
        }

        //cancel button
        cancel.setOnClickListener {
            dialogExit.dismiss()
        }

        dialogExit.show()

    }

    interface DeleteItemInPreview {
        fun deletePreviewItemClicked(clickedLogo: SavedLogo, typeLogo: String)
    }

    override fun onAdLoaded(p0: MaxAd?) {}
    override fun onAdDisplayed(p0: MaxAd?) {}
    override fun onAdHidden(p0: MaxAd?) {
        //loading next ad
        interstitialAd!!.loadAd()

        //finish this fragment
        MainUtils.finishFragment(requireActivity().supportFragmentManager,this@PreviewFragment)
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

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(requireContext(), resources)
            val adView = AdView(requireContext())
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner1.addView(adView)
            adView.loadAd(adRequest)
        }
        else{
            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(requireContext(), resources)
            val adView = AdView(requireContext())
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_RELEASE
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner1.addView(adView)
            adView.loadAd(adRequest)
        }
    }
    private fun Banner2Ads() {

        if (BuildConfig.DEBUG){

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(requireContext(), resources)
            val adView = AdView(requireContext())
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner2.addView(adView)
            adView.loadAd(adRequest)
        }
        else{
            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(requireContext(), resources)
            val adView = AdView(requireContext())
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
                requireContext(),
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
                requireContext(),
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

    interface BackButtonClickEvent{
        fun backButtonClicked()
    }
}