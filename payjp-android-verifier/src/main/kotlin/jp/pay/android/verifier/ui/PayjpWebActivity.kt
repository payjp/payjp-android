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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import jp.pay.android.PayjpLogger
import jp.pay.android.verifier.PayjpVerifier
import jp.pay.android.verifier.R

/**
 * PayjpCardVerifyActivity
 *
 */
class PayjpWebActivity : AppCompatActivity(R.layout.payjp_verify_card_activity),
    LifecycleObserver {

    companion object {
        private const val EXTRA_KEY_SUCCESS = "EXTRA_KEY_SUCCESS"
        private const val EXTRA_KEY_START_URI = "EXTRA_KEY_START_URI"
        private const val EXTRA_KEY_CALLBACK_URI = "EXTRA_KEY_CALLBACK_URI"

        fun createIntent(context: Context, startUri: Uri, callbackUri: Uri): Intent {
            return Intent(context, PayjpWebActivity::class.java)
                .putExtra(EXTRA_KEY_START_URI, startUri.toString())
                .putExtra(EXTRA_KEY_CALLBACK_URI, callbackUri.toString())
        }

        fun onActivityResult(data: Intent?, callback: PayjpVerifyCardResultCallback) {
            val success = data?.getBooleanExtra(EXTRA_KEY_SUCCESS, false) ?: false
            val result = if (success) {
                PayjpVerifyCardResult.Success
            } else {
                PayjpVerifyCardResult.Canceled
            }
            callback.onResult(result)
        }
    }

    private val startUri: Uri by lazy {
        intent.getStringExtra(EXTRA_KEY_START_URI).let { Uri.parse(it) }
    }

    private val callbackUri: Uri by lazy {
        intent.getStringExtra(EXTRA_KEY_CALLBACK_URI).let { Uri.parse(it) }
    }

    private lateinit var webView: VerifyCardWebView
    private val logger: PayjpLogger = PayjpVerifier.logger()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setUpUI()
        lifecycle.addObserver(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun startSafeBrowse() {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.START_SAFE_BROWSING)) {
            WebViewCompat.startSafeBrowsing(this) { success ->
                if (success) {
                    logger.i("Initialized Safe Browsing.")
                } else {
                    logger.w("Unable to initialize Safe Browsing.")
                }
                startLoad()
            }
        } else {
            logger.w("Safe Browsing is not supported.")
            startLoad()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun cleanUpWebView() {
        webView.destroy()
    }

    private fun startLoad() {
        val authorization: String = PayjpVerifier.tokenService().getAuthorization()
        webView.loadUrl(
            startUri.toString(),
            mutableMapOf("Authorization" to authorization)
        )
    }

    private fun setUpUI() {
        webView = findViewById(R.id.web_view)
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
        webView.addInterceptor { uri ->
            logger.d("interceptor uri: $uri")
            if (!URLUtil.isNetworkUrl(uri.toString())) {
                openExternal(uri)
                true
            } else {
                false
            }
        }
        webView.addLoadStateWatcher(WebViewLoadingDelegate(
            logger = logger,
            errorView = findViewById(R.id.error_view),
            progressBar = findViewById(R.id.progress_bar),
            swipeRefresh = swipeRefresh
        ))
        webView.addOnFinishedLoadState { _, url ->
            if (url.startsWith(callbackUri.toString())) {
                openExternal(Uri.parse(url))
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        swipeRefresh.setOnChildScrollUpCallback { _, _ -> webView.scrollY > 10 }
        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }
    }

    private fun openExternal(uri: Uri) {
        val intent = if (uri.scheme === "intent") {
            Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
        } else {
            Intent(Intent.ACTION_VIEW, uri)
        }
        logger.d("intent: $intent")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}
