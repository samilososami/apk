// MainActivity.kt
package dev.sami.webwrap

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.content.Intent
import android.net.Uri

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var swipe: SwipeRefreshLayout

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        filePathCallback?.onReceiveValue(uris?.toTypedArray() ?: emptyArray())
        filePathCallback = null
    }

    private val requestPerms = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ -> }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        swipe = SwipeRefreshLayout(this)
        webView = WebView(this)
        swipe.addView(webView)
        setContentView(swipe)

        val startUrl = "https://samsungfind.samsung.com/"

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            mediaPlaybackRequiresUserGesture = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportMultipleWindows(true)
            userAgentString = userAgentString + " SamiWebWrap/1.0"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url ?: return false
                val scheme = url.scheme ?: "https"
                return if (scheme == "http" || scheme == "https") {
                    false
                } else {
                    try { startActivity(Intent(Intent.ACTION_VIEW, url)) } catch (_: Exception) {}
                    true
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                swipe.isRefreshing = newProgress < 100 && swipe.isRefreshing
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback = filePathCallback
                val mime = fileChooserParams?.acceptTypes?.firstOrNull()?.ifBlank { "*/*" } ?: "*/*"
                pickFile.launch(mime)
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                val resources = mutableListOf<String>()
                request?.resources?.forEach {
                    when (it) {
                        PermissionRequest.RESOURCE_AUDIO_CAPTURE -> resources.add(Manifest.permission.RECORD_AUDIO)
                        PermissionRequest.RESOURCE_VIDEO_CAPTURE -> resources.add(Manifest.permission.CAMERA)
                    }
                }
                if (resources.isNotEmpty()) {
                    requestPerms.launch(resources.toTypedArray())
                }
                request?.grant(request?.resources)
            }

            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                requestPerms.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                callback?.invoke(origin, true, false)
            }
        }

        swipe.setOnRefreshListener { webView.reload() }
        webView.loadUrl(startUrl)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK && this::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
