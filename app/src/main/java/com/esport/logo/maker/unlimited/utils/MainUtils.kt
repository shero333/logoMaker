package com.esport.logo.maker.unlimited.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.application.LogoMakerApp
import com.google.android.gms.ads.AdSize
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.json.JSONObject

class MainUtils {

    companion object{

        private var REQUEST_CODE_STORAGE = 3
        var splash_banner_liveData = MutableLiveData<String>()
        var openAd_id_release_liveData = MutableLiveData<String>()
        var openAd_id_debug_liveData = MutableLiveData<String>()

        fun makeStatusBarTransparent(activity: Activity) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }

        fun statusBarColor(activity: Activity) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(activity, R.color.theme_color)
        }

        fun hasPermissions(context: Context): Boolean {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

            return context.checkCallingOrSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED &&
                    context.checkCallingOrSelfPermission(permissions[1]) == PackageManager.PERMISSION_GRANTED &&
                    context.checkCallingOrSelfPermission(permissions[2]) == PackageManager.PERMISSION_GRANTED
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun getPermissions(activity: Activity) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            activity.requestPermissions(permissions, REQUEST_CODE_STORAGE)
        }

        fun permissionSettingsPage(activity: Activity){
            activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package: " + activity.packageName)))
        }

        //replace fragment
        fun replaceFragment(fragment: Fragment, fragmentManager: FragmentManager, fragmentContainer: Int) {
            // Begin a transaction to add the fragment to the layout
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()
            transaction.replace(fragmentContainer, fragment)
            transaction.commit()
        }

        fun finishFragment(fragmentManager: FragmentManager,fragment:Fragment){

            // Remove the fragment from the activity
            val transaction = fragmentManager.beginTransaction()
            transaction.remove(fragment).commit()
        }

        //Ad loaders
        fun getAdaptiveAdSize(context: Context, resources: Resources): AdSize {
            val widthPixels = resources.displayMetrics.widthPixels
            val density = resources.displayMetrics.density
            val widthDp = (widthPixels / density).toInt()
            val adWidth = widthDp.coerceAtMost(640)
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }

        fun fetchIdsOfAds() {

            val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

            val configSettings = FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(4000).build()
            firebaseRemoteConfig.setConfigSettingsAsync(configSettings)

            //remote config
            firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        //get the data ids
                        val adIdsJson = firebaseRemoteConfig.getString("Ad_ids_AdMob")
                        //get the data banner native networks
                        val banner_native_networkJson = firebaseRemoteConfig.getString("Banner_Native_Ads_AdMob_AppLovin")
                        //get the data interstitial openAd networks
                        val interstitial_open_networkJson = firebaseRemoteConfig.getString("Interstitial_openAd_AppLovin_AdMob")

                        //Ad ids
                        val parsedValuesConfigIds = JSONObject(adIdsJson)

                        val debugJson = parsedValuesConfigIds.getString("Debug")
                        val releaseJson = parsedValuesConfigIds.getString("Release")

                        val debug = JSONObject(debugJson)
                        val release = JSONObject(releaseJson)

                        //Ad Ids Admob {Debug}
                        LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG = debug.getString("banner_Ad_Admob_id")
                        LogoMakerApp.NATIVE_AD_ADMOB_ID_DEBUG = debug.getString("native_Ad_Admob_id")
                        LogoMakerApp.INTERSTITIAL_AD_ADMOB_ID_DEBUG = debug.getString("interstitial_Ad_Admob_id")
                        openAd_id_debug_liveData.value = release.getString("openAd_Ad_Admob_id")

                        //Ad Ids Admob {Release}
                        LogoMakerApp.BANNER_AD_ADMOB_ID_RELEASE = release.getString("banner_Ad_Admob_id")
                        LogoMakerApp.NATIVE_AD_ADMOB_ID_RELEASE = release.getString("native_Ad_Admob_id")
                        LogoMakerApp.INTERSTITIAL_AD_ADMOB_ID_RELEASE = release.getString("interstitial_Ad_Admob_id")
                        openAd_id_release_liveData.value = release.getString("openAd_Ad_Admob_id")

                        //banner and native networks.....
                        val parsedValuesBannerNativeNetworks = JSONObject(banner_native_networkJson)

                        //splash Screen
                        splash_banner_liveData.value = "1"
                        //mainScreen
                        LogoMakerApp.MAIN_SCREEN_BANNER_BOTTOM = parsedValuesBannerNativeNetworks.getString("MainScreen_banner_bottom")
                        LogoMakerApp.MAIN_SCREEN_BANNER_TOP = parsedValuesBannerNativeNetworks.getString("MainScreen_banner_top")
                        LogoMakerApp.MAIN_SCREEN_NATIVE_AD = parsedValuesBannerNativeNetworks.getString("MainScreen_nativeAd")
                        //recents screen
                        LogoMakerApp.RECENTS_ACTIVITY_BANNER_TOP = parsedValuesBannerNativeNetworks.getString("RecentsActivity_banner_top")
                        LogoMakerApp.RECENTS_ACTIVITY_BANNER_BOTTOM = parsedValuesBannerNativeNetworks.getString("RecentsActivity_banner_bottom")
                        //Preview
                        LogoMakerApp.PREVIEW_ACTIVITY_BANNER_TOP = parsedValuesBannerNativeNetworks.getString("PreviewActivity_banner_Top")
                        LogoMakerApp.PREVIEW_ACTIVITY_BANNER_BOTTOM = parsedValuesBannerNativeNetworks.getString("PreviewActivity_banner_bottom")
                        //creating logo screen
                        LogoMakerApp.CREATE_LOGO_SCREEN_BANNER_TOP = parsedValuesBannerNativeNetworks.getString("CreateLogoScreen_banner_top")
                        LogoMakerApp.CREATE_LOGO_SCREEN_BANNER_BOTTOM = parsedValuesBannerNativeNetworks.getString("CreateLogoScreen_banner_bottom")
                        //exit screen
                        LogoMakerApp.EXIT_SCREEN_BANNER_TOP = parsedValuesBannerNativeNetworks.getString("ExitScreen_banner_top")
                        LogoMakerApp.EXIT_SCREEN_BANNER_BOTTOM = parsedValuesBannerNativeNetworks.getString("ExitScreen_banner_bottom")

                        LogoMakerApp.POLICY_ACTIVITY_BANNER_TOP = parsedValuesBannerNativeNetworks.getString("PolicyActivity_banner_top")
                        LogoMakerApp.POLICY_ACTIVITY_BANNER_BOTTOM = parsedValuesBannerNativeNetworks.getString("PolicyActivity_banner_bottom")
                        LogoMakerApp.BOARDING_ACTIVITY_BANNER_TOP = parsedValuesBannerNativeNetworks.getString("BoardingActivity_banner_top")
                        LogoMakerApp.BOARDING_ACTIVITY_BANNER_BOTTOM = parsedValuesBannerNativeNetworks.getString("BoardingActivity_banner_bottom")
                        LogoMakerApp.PERMISSION_ACTIVITY_BANNER_TOP = parsedValuesBannerNativeNetworks.getString("PermissionActivity_banner_top")
                        LogoMakerApp.PERMISSION_ACTIVITY_BANNER_BOTTOM = parsedValuesBannerNativeNetworks.getString("PermissionActivity_banner_bottom")


                        //interstitial and openAd network.....
                        val parsedValuesInterstitialOpenAdNetworks = JSONObject(interstitial_open_networkJson)

                        //openAd
                        LogoMakerApp.SPLASH_OPENAD_AGREE_BUTTON =
                            parsedValuesInterstitialOpenAdNetworks.getString("Splash_openAd_agree_button")
                        //mainScreen
                        LogoMakerApp.MAINSCREEN_CREATELOGO_BUTTON_INTERSTITIAL =
                            parsedValuesInterstitialOpenAdNetworks.getString("MainScreen_createLogo_Button_interstitial")
                        LogoMakerApp.MAINSCREEN_EDITLOGO_BUTTON_INTERSTITIAL =
                            parsedValuesInterstitialOpenAdNetworks.getString("MainScreen_editLogo_Button_interstitial")
                        LogoMakerApp.MAINSCREEN_RECENTLIST_ITEM_CLICK_INTERSTITIAL =
                            parsedValuesInterstitialOpenAdNetworks.getString("MainScreen_recentList_item_click_interstitial")
                        //recents screen
                        LogoMakerApp.RECENTS_ACTIVITY_RECENTLIST_ITEM_CLICK_INTERSTITIAL =
                            parsedValuesInterstitialOpenAdNetworks.getString("RecentsActivity_recentList_item_click_interstitial")
                        //Preview
                        LogoMakerApp.PREVIEW_ACTIVITY_BACK_BUTTON_PRESS_CLICK_BUTTON_INTERSTITIAL =
                            parsedValuesInterstitialOpenAdNetworks.getString("PreviewActivity_back_button_press_click_interstitial")
                        //creating logo screen
                        LogoMakerApp.CREATE_LOGO_SCREEN_BACK_BUTTON_PRESS_INTERSTITIAL =
                            parsedValuesInterstitialOpenAdNetworks.getString("CreateLogoScreen_back_button_press_interstitial")
                        LogoMakerApp.CREATE_LOGO_SCREEN_DOWNLOAD_BUTTON_PRESS_INTERSTITIAL =
                            parsedValuesInterstitialOpenAdNetworks.getString("CreateLogoScreen_download_button_press_interstitial")
                        //policy activity
                        LogoMakerApp.POLICY_ACTIVITY_BUTTON_CLICK_INTERSTITIAL =
                            parsedValuesInterstitialOpenAdNetworks.getString("PolicyActivity_button_click_interstitial")
                        //permissions activity
                        LogoMakerApp.PERMISSION_ACTIVITY_BUTTON_CLICK_INTERSTITIAL =
                            parsedValuesInterstitialOpenAdNetworks.getString("PermissionActivity_button_click_interstitial")
                        //boarding activity
                        LogoMakerApp.BOARDING_ACTIVITY_BUTTON_CLICK_INTERSTITIAL =
                            parsedValuesInterstitialOpenAdNetworks.getString("BoardingActivity_button_click_interstitial")

                        Log.i("DEBUG_AD_IDs ", LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG)
                    } else {

                        Log.i("Task unSuccessful", "No data available")
                    }
                }
                .addOnFailureListener { exception ->

                    Log.i("remoteConfigFailure: ", exception.localizedMessage!!.toString())
                }
        }
    }

}