/*
 *
 * Copyright (c) 2020 PAY, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package jp.pay.android.verifier.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import java.text.SimpleDateFormat
import java.util.Locale
import jp.pay.android.PayjpLogger
import jp.pay.android.verifier.PayjpVerifier
import jp.pay.android.verifier.R

internal class VerifierWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private val interceptors: MutableList<Interceptor> = mutableListOf()
    private val loadStateWatchers: MutableList<LoadStateWatcher> = mutableListOf()
    private val logger: PayjpLogger = PayjpVerifier.logger()

    init {
        webViewClient = CardVerifyWebViewClient()
        webChromeClient = CardVerifyWebChromeClient()

        scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        configureSettings()

        // webview vulnerability
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            removeJavascriptInterface("searchBoxJavaBridge_")
            removeJavascriptInterface("accessibility")
            removeJavascriptInterface("accessibilityTraversal")
        }
    }

    override fun destroy() {
        stopLoading()
        pauseTimers()
        clearHistory()
        clearCache(true)
        removeAllViews()
        webChromeClient = null
        webViewClient = null
        loadStateWatchers.clear()
        interceptors.clear()
        super.destroy()
    }

    override fun loadUrl(url: String?) {
        if (intercept(Uri.parse(url))) {
            return
        }
        super.loadUrl(url)
    }

    fun addInterceptor(interceptor: (uri: Uri) -> Boolean) {
        interceptors.add(object : Interceptor {
            override fun intercept(uri: Uri): Boolean = interceptor.invoke(uri)
        })
    }

    fun addLoadStateWatcher(loadStateWatcher: LoadStateWatcher) {
        loadStateWatchers.add(loadStateWatcher)
    }

    fun addOnFinishedLoadState(onFinished: (webView: WebView, url: String) -> Unit) {
        addLoadStateWatcher(onFinished = onFinished)
    }

    fun addLoadStateWatcher(
        onStarted: ((webView: WebView, url: String) -> Unit)? = null,
        onFinished: ((webView: WebView, url: String) -> Unit)? = null,
        onError: ((webView: WebView, errorCode: Int, description: String?, failingUrl: String) -> Unit)? = null,
        onSslError: ((webView: WebView, sslError: SslError) -> Unit)? = null,
        onProgressChanged: ((webView: WebView, newProgress: Int) -> Unit)? = null
    ) {
        loadStateWatchers.add(object : LoadStateWatcher {
            override fun onStarted(webView: WebView, url: String) {
                onStarted?.invoke(webView, url)
            }

            override fun onFinished(webView: WebView, url: String) {
                onFinished?.invoke(webView, url)
            }

            override fun onError(
                webView: WebView,
                errorCode: Int,
                description: String?,
                failingUrl: String
            ) {
                onError?.invoke(webView, errorCode, description, failingUrl)
            }

            override fun onSslError(webView: WebView, sslError: SslError) {
                onSslError?.invoke(webView, sslError)
            }

            override fun onProgressChanged(webView: WebView, newProgress: Int) {
                onProgressChanged?.invoke(webView, newProgress)
            }
        })
    }

    private fun intercept(uri: Uri): Boolean {
        return interceptors.any { it.intercept(uri) }
    }

    private fun configureSettings() {
        if (logger.debuggable()) {
            val webViewPackageInfo = WebViewCompat.getCurrentWebViewPackage(context)
            logger.d("WebView version: ${webViewPackageInfo?.versionName}")
        }
        settings.apply {
            allowFileAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
            allowContentAccess = false
            builtInZoomControls = false
            displayZoomControls = false
            databaseEnabled = true
            domStorageEnabled = true
            setAppCacheEnabled(true)
            @SuppressLint("SetJavaScriptEnabled")
            javaScriptEnabled = true
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                savePassword = false
            }
        }
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
            WebSettingsCompat.setSafeBrowsingEnabled(settings, true)
        }
    }

    internal class CardVerifyWebViewClient : WebViewClientCompat() {
        private var loadingState: LoadingState = LoadingState.STOPPED

        @RequiresApi(21)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if (!request.hasGesture()) {
                return false
            }
            return shouldOverrideUrlLoading(view, request.url.toString())
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return (view as? VerifierWebView)?.intercept(Uri.parse(url)) ?: false
        }

        @RequiresApi(21)
        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceErrorCompat
        ) {
            val errorCode =
                if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_RESOURCE_ERROR_GET_CODE)) {
                    error.errorCode
                } else 0
            val errorDescription =
                if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_RESOURCE_ERROR_GET_DESCRIPTION)) {
                    error.description.toString()
                } else null
            onReceivedError(view, errorCode, errorDescription, request.url.toString())
        }

        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String?,
            failingUrl: String
        ) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            loadingState = LoadingState.ERROR
            (view as? VerifierWebView)?.loadStateWatchers?.forEach {
                it.onError(view, errorCode, description, failingUrl)
            }
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (loadingState === LoadingState.STOPPED) {
                loadingState = LoadingState.LOADING
                (view as? VerifierWebView)?.loadStateWatchers?.forEach {
                    it.onStarted(view, url)
                    it.onProgressChanged(view, 20)
                }
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            if (loadingState === LoadingState.LOADING) {
                (view as? VerifierWebView)?.loadStateWatchers?.forEach {
                    it.onFinished(view, url)
                }
            }
            loadingState = LoadingState.STOPPED
        }

        override fun onReceivedSslError(
            view: WebView,
            handler: SslErrorHandler,
            error: SslError
        ) {
            loadingState = LoadingState.ERROR
            createSslErrorDialog(view.context, handler, error).show()
            (view as? VerifierWebView)?.loadStateWatchers?.forEach {
                it.onSslError(view, error)
            }
        }

        private fun createSslErrorDialog(
            context: Context,
            handler: SslErrorHandler,
            error: SslError
        ): AlertDialog =
            AlertDialog.Builder(context)
                .setTitle(R.string.payjp_verifier_ssl_error_title)
                .setMessage(createErrorMessage(error))
                .setNegativeButton(android.R.string.ok) { dialog, _ ->
                    dialog.cancel()
                    handler.cancel()
                }
                .create()

        private fun createErrorMessage(error: SslError): String? {
            val cert = error.certificate
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
            val result = StringBuilder()
                .append(R.string.payjp_verifier_ssl_error_head)
            return when (error.primaryError) {
                SslError.SSL_EXPIRED -> {
                    result.append(R.string.payjp_verifier_ssl_error_expired)
                        .append(dateFormat.format(cert.validNotAfterDate))
                    result.toString()
                }
                SslError.SSL_IDMISMATCH -> {
                    result.append(R.string.payjp_verifier_ssl_error_mismatch)
                        .append(cert.issuedTo.cName)
                    result.toString()
                }
                SslError.SSL_NOTYETVALID -> {
                    result.append(R.string.payjp_verifier_ssl_error_not_yet_valid)
                        .append(dateFormat.format(cert.validNotBeforeDate))
                    result.toString()
                }
                SslError.SSL_UNTRUSTED -> {
                    result.append(R.string.payjp_verifier_ssl_error_untrusted)
                        .append(cert.issuedBy.dName)
                    result.toString()
                }
                else -> {
                    result.append(R.string.payjp_verifier_ssl_error_unknown)
                    result.toString()
                }
            }
        }
    }

    internal enum class LoadingState {
        STOPPED, LOADING, ERROR
    }

    internal interface Interceptor {
        fun intercept(uri: Uri): Boolean
    }

    internal interface LoadStateWatcher {
        fun onStarted(webView: WebView, url: String)
        fun onFinished(webView: WebView, url: String)
        fun onError(webView: WebView, errorCode: Int, description: String?, failingUrl: String)
        fun onSslError(webView: WebView, sslError: SslError)
        fun onProgressChanged(webView: WebView, newProgress: Int)
    }

    internal class CardVerifyWebChromeClient : WebChromeClient() {
        private fun showAlertDialog(context: Context, message: String, result: JsResult) {
            AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> result.cancel() }
                .show()
        }

        override fun onJsAlert(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {
            showAlertDialog(view.context, message, result)
            return true
        }

        override fun onJsConfirm(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {
            showAlertDialog(view.context, message, result)
            return true
        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            (view as? VerifierWebView)?.loadStateWatchers?.forEach {
                if (newProgress > 20) {
                    it.onProgressChanged(view, newProgress)
                }
            }
        }
    }
}
