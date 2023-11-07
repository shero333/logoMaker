package com.esport.logo.maker.unlimited.policy.policy_page

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import com.esport.logo.maker.unlimited.databinding.ActivityPolicyPageBinding
import com.esport.logo.maker.unlimited.utils.MainUtils

class PolicyPage : AppCompatActivity() {

    private lateinit var binding: ActivityPolicyPageBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPolicyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MainUtils.statusBarColor(this@PolicyPage)

        // Enable JavaScript (optional, if required by the web page)
        binding.policyPage.settings.javaScriptEnabled = true

        // Set a WebViewClient to handle redirects and links within the WebView
        binding.policyPage.webViewClient = WebViewClient()

        // Load a URL into the WebView
        binding.policyPage.loadUrl("https://github.com/burhanrashid52/PhotoEditor")

        //on back pressed
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //on back pressed
                finish()
            }
        })
    }
}