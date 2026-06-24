package com.example.smart_home

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.smart_home.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable drawing under the notch and status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        
        // Make system bars transparent so the camera feed fills the whole screen
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webViewCamera.apply {
            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    // Only show error for the main page failing
                    if (request?.isForMainFrame == true) {
                        showError()
                    }
                }

                @Suppress("DeprecatedCallableAddReplaceWith")
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    showError()
                }

                override fun onReceivedHttpError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    errorResponse: WebResourceResponse?
                ) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    if (request?.isForMainFrame == true) {
                        showError()
                    }
                }

                private fun showError() {
                    binding.tvErrorMessage.visibility = View.VISIBLE
                    binding.webViewCamera.visibility = View.GONE
                }
            }
            settings.javaScriptEnabled = true
            // Ensure content fits the viewport
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            
//            val videoUrl = "http://192.168.0.248:5001/video_feed"
            val videoUrl = "http://10.0.2.2:5001/video_feed" // localhost
            loadUrl(videoUrl)
        }

        binding.btnBackFromCamera.setOnClickListener {
            finish()
        }
    }
}