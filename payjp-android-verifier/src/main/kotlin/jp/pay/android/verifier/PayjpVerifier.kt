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
package jp.pay.android.verifier

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.MainThread
import jp.pay.android.PayjpLogger
import jp.pay.android.PayjpTokenService
import jp.pay.android.model.ThreeDSecureToken
import jp.pay.android.verifier.ui.PayjpVerifierRedirectActivity
import jp.pay.android.verifier.ui.PayjpVerifyCardResult
import jp.pay.android.verifier.ui.PayjpVerifyCardResultCallback

object PayjpVerifier {

    internal const val VERIFY_WEB_ENDPOINT_HOST = "api.pay-stage.com" // TODO
    private const val REQUEST_CODE_VERIFY = 10

    private var logger: PayjpLogger = PayjpLogger.None
    private var tokenService: PayjpTokenService? = null
    private val webBrowserResolver = WebBrowserResolver(
        WebBrowser.ChromeTab,
        WebBrowser.AnyBrowsable,
        WebBrowser.InAppWeb
    )

    fun configure(
        logger: PayjpLogger,
        tokenService: PayjpTokenService
    ) {
        this.logger = logger
        this.tokenService = tokenService
    }

    internal fun logger(): PayjpLogger = logger

    internal fun tokenService(): PayjpTokenService = checkNotNull(tokenService) {
        "You must initialize Payjp first"
    }

    @MainThread
    fun startWebVerify(tdsToken: ThreeDSecureToken, activity: Activity, requestCode: Int = REQUEST_CODE_VERIFY) {
        val intent = webBrowserResolver.resolve(
            context = activity,
            uri = tdsToken.getTdsEntryUri(),
            callbackUri = tdsToken.getTdsFinishUri()
        )
        if (intent == null) {
            logger.w("Any activity which open Web not found.")
        } else {
            PayjpVerifierRedirectActivity.setEnabled(activity, true)
            activity.startActivityForResult(intent, requestCode)
        }
    }

    @MainThread
    fun handleWebVerifyResult(
        context: Context,
        data: Intent?,
        callback: PayjpVerifyCardResultCallback
    ) {
        PayjpVerifierRedirectActivity.setEnabled(context, false)
        // TODO check deeplink activity
        callback.onResult(PayjpVerifyCardResult.Canceled)
    }
}
