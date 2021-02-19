package com.alexmoove

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class FullscreenActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    var blocklist = ""
    var currentUrl = "https://alexmoove.online"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)
        webView = findViewById(R.id.main_webview)
        val splash:ImageView = findViewById(R.id.splash)
        load()
        window.setFormat(PixelFormat.TRANSLUCENT);

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                splash.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return if (blocklist.contains(":::::" + request?.url?.host)) { // If blocklist equals url = Block
                    Log.d("Alexmoove", "Host Blockeado: ${request?.url}  ")
                    true
                } else {
                    Log.d("Alexmoove", "Host Permitido: ${request?.url}  ")
                    if (request?.url != null) {
                        currentUrl = request?.url.toString()
                    }
                    super.shouldOverrideUrlLoading(view, request)
                }
            }

    //
        }

        webView.settings.domStorageEnabled = true;
        webView.settings.setSupportMultipleWindows(false)
        webView.settings.javaScriptCanOpenWindowsAutomatically = false
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.setAppCacheEnabled(true)
        webView.settings.useWideViewPort =  true
        webView.settings.setGeolocationEnabled(true)
        webView.settings
        webView.webChromeClient = MyChrome(this)
        webView.loadUrl(currentUrl)

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        finish()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun load() { //Blocklist loading
        var strLine2:String?
        val blocklist2 = StringBuilder()
        val loddnormallist = "0"
        val fis2 = this.resources.openRawResource(R.raw.adblockserverlist) //Storage location
        val br2 = BufferedReader(InputStreamReader(fis2))
        if (fis2 != null) {
            try {
                while (br2.readLine().also { strLine2 = it } != null) {
                    if (loddnormallist.equals("0")) {
                        blocklist2.append(strLine2) //if ":::::" exists in blocklist | Line for Line
                        blocklist2.append("\n")
                    }
                    if (loddnormallist.equals("1")) {
                        blocklist2.append(":::::$strLine2") //if ":::::" not exists in blocklist | Line for Line
                        blocklist2.append("\n")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        blocklist = blocklist2.toString()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
    }

}


class MyChrome(var activity: FullscreenActivity) : WebChromeClient() {
    private var mCustomView: View? = null
    private var mCustomViewCallback: CustomViewCallback? = null
    private var mOriginalOrientation = 0
    private var mOriginalSystemUiVisibility = 0
    private val FULL_SCREEN_SETTING = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_IMMERSIVE

    override fun getDefaultVideoPoster(): Bitmap? {
        return BitmapFactory.decodeResource(
            activity.applicationContext.resources, 2130837573
        )
    }
    override fun onHideCustomView() {
        (activity.window.decorView as FrameLayout).removeView(mCustomView)
        mCustomView = null
        activity.window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
        activity.requestedOrientation = mOriginalOrientation
        mCustomViewCallback!!.onCustomViewHidden()
        mCustomViewCallback = null
    }

    override fun onShowCustomView(paramView: View, paramCustomViewCallback: CustomViewCallback) {
        if (mCustomView != null) {
            onHideCustomView()
            return
        }
        mCustomView = paramView
        mOriginalSystemUiVisibility = activity.window.decorView.systemUiVisibility
        mOriginalOrientation = activity.requestedOrientation
        mCustomViewCallback = paramCustomViewCallback
        (activity.window.decorView as FrameLayout).addView(
            mCustomView,
            FrameLayout.LayoutParams(-1, -1)
        )
        activity.window.decorView.systemUiVisibility = FULL_SCREEN_SETTING
        mCustomView!!.setOnSystemUiVisibilityChangeListener { updateControls() }
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    fun updateControls() {
        val params = mCustomView!!.layoutParams as FrameLayout.LayoutParams
        params.bottomMargin = 0
        params.topMargin = 0
        params.leftMargin = 0
        params.rightMargin = 0
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        mCustomView!!.layoutParams = params
        activity.window.decorView.systemUiVisibility = FULL_SCREEN_SETTING
    }
}