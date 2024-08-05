/*
 *
 * Copyright (c) 2021 PAY, Inc.
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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import jp.pay.android.PayjpLogger
import jp.pay.android.verifier.PayjpVerifier
import jp.pay.android.verifier.databinding.PayjpWebActivityBinding

/**
 * PayjpWebActivity
 *
 */
class PayjpWebActivity : AppCompatActivity(), DefaultLifecycleObserver {

    companion object {
        internal const val EXTRA_KEY_START_URI = "EXTRA_KEY_START_URI"
        internal const val EXTRA_KEY_CALLBACK_URI = "EXTRA_KEY_CALLBACK_URI"
        internal const val EXTRA_KEY_TITLE = "EXTRA_KEY_TITLE"

        fun createIntent(context: Context, startUri: Uri, callbackUri: Uri, title: String): Intent {
            return Intent(context, PayjpWebActivity::class.java)
                .putExtra(EXTRA_KEY_START_URI, startUri.toString())
                .putExtra(EXTRA_KEY_CALLBACK_URI, callbackUri.toString())
                .putExtra(EXTRA_KEY_TITLE, title)
        }
    }

    private val startUri: Uri by lazy {
        intent.getStringExtra(EXTRA_KEY_START_URI).let { Uri.parse(it) }
    }

    private val callbackUri: Uri by lazy {
        intent.getStringExtra(EXTRA_KEY_CALLBACK_URI).let { Uri.parse(it) }
    }

    private val customTitle: String by lazy {
        intent.getStringExtra(EXTRA_KEY_TITLE) ?: ""
    }

    private lateinit var binding: PayjpWebActivityBinding
    private val logger: PayjpLogger = PayjpVerifier.logger()

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        binding = PayjpWebActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setUpUI()
        lifecycle.addObserver(this)
        title = customTitle
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onCreate(owner)
        startSafeBrowse()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onDestroy(owner)
        cleanUpWebView()
    }

    private fun startSafeBrowse() {
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

    private fun cleanUpWebView() {
        binding.webView.destroy()
    }

    private fun startLoad() {
        binding.webView.loadUrl(startUri.toString())
    }

    private fun setUpUI() {
        binding.webView.run {
            addInterceptor { uri ->
                logger.d("interceptor uri: $uri")
                if (!URLUtil.isNetworkUrl(uri.toString())) {
                    openExternal(uri)
                    true
                } else {
                    false
                }
            }
            addLoadStateWatcher(
                WebViewLoadingDelegate(
                    logger = logger,
                    errorView = binding.errorView,
                    progressBar = binding.progressBar,
                    swipeRefresh = binding.swipeRefresh
                )
            )
            addOnFinishedLoadState { _, url ->
                if (url.startsWith(callbackUri.toString())) {
                    logger.d("url matches with callbackUri $url")
                    redirectWithResult(Uri.parse(url))
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
        binding.swipeRefresh.run {
            setOnChildScrollUpCallback { _, _ -> binding.webView.scrollY > 10 }
            setOnRefreshListener { binding.webView.reload() }
        }
    }

    private fun redirectWithResult(uri: Uri) {
        val intent = Intent(this, PayjpThreeDSecureStepActivity::class.java)
            .setData(uri)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
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
