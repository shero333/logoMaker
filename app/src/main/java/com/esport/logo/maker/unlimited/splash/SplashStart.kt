package com.esport.logo.maker.unlimited.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.utils.MainUtils


class SplashStart : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen: SplashScreen = installSplashScreen()

        setContentView(R.layout.splash_start)

        //disabling night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        splashScreen.setKeepOnScreenCondition { true }

        //handling the status bar color
        MainUtils.statusBarColor(this@SplashStart)

        // Hide the navigation bar
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        //currency activity
        startActivity(Intent(this@SplashStart, SplashActivity::class.java))
        finish()
    }
}