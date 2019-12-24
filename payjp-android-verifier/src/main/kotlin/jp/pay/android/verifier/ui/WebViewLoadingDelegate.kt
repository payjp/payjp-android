/*
 *
 * Copyright (c) 2019 PAY, Inc.
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

import android.net.http.SslError
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import jp.pay.android.PayjpLogger

internal class WebViewLoadingDelegate(
    private val logger: PayjpLogger,
    private val errorView: ViewGroup,
    private val progressBar: ProgressBar,
    private val swipeRefresh: SwipeRefreshLayout
) : CardVerifyWebView.LoadStateWatcher {
    private val animation = AlphaAnimation(1.0f, 0.0f).apply {
        duration = 1000
    }

    override fun onStarted(webView: WebView, url: String) {
        logger.d("onStarted: $url")
        progressBar.clearAnimation()
        progressBar.visibility = View.VISIBLE
        errorView.visibility = View.GONE
    }

    override fun onFinished(webView: WebView, url: String) {
        logger.d("onFinished: $url")
        progressBar.startAnimation(animation)
        webView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        progressBar.visibility = View.GONE
        swipeRefresh.isRefreshing = false
    }

    override fun onError(
        webView: WebView,
        errorCode: Int,
        description: String?,
        failingUrl: String
    ) {
        logger.d("onError: $errorCode, $description, $failingUrl")
        progressBar.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        webView.visibility = View.INVISIBLE
        swipeRefresh.isRefreshing = false
    }

    override fun onSslError(webView: WebView, sslError: SslError) {
        logger.d("onSslError: $sslError")
        progressBar.visibility = View.GONE
        swipeRefresh.isRefreshing = false
    }

    override fun onProgressChanged(webView: WebView, newProgress: Int) {
        if (newProgress > 80 && webView.visibility != View.VISIBLE) {
            webView.visibility = View.VISIBLE
        }
        progressBar.progress = newProgress
    }
}
