package com.esport.logo.maker.unlimited.preferences

import android.annotation.SuppressLint
import androidx.preference.PreferenceManager
import com.esport.logo.maker.unlimited.application.LogoMakerApp

object SharedPreference {

    private val sharedPreference = PreferenceManager.getDefaultSharedPreferences(LogoMakerApp.appContext)


    @JvmStatic
    var isFirstRun: Boolean
        get() = sharedPreference.getBoolean("isFirstRun", true)
        set(isFirstRun) {
            sharedPreference.edit().putBoolean("isFirstRun", isFirstRun).apply()
        }

    @JvmStatic
    var downloadedLogos: String?
        get() = sharedPreference.getString("downloadedImages","")
        @SuppressLint("CommitPrefEdits")
        set(downloadedLogos){
            sharedPreference.edit().putString("downloadedImages",downloadedLogos).apply()
        }
}