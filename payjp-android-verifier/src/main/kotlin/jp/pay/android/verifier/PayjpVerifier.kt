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
import androidx.annotation.MainThread
import jp.pay.android.PayjpLogger
import jp.pay.android.PayjpTokenService
import jp.pay.android.model.ThreeDSecureToken
import jp.pay.android.verifier.ui.PayjpVerifierRedirectActivity
import jp.pay.android.verifier.ui.PayjpVerifyCardResultCallback

object PayjpVerifier {
    private const val REQUEST_CODE_VERIFY_LAUNCHER = 10
    private const val REQUEST_CODE_VERIFY = 11
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
    fun startWebVerifyLauncher(tdsToken: ThreeDSecureToken, activity: Activity) {
        val intent = PayjpVerifierRedirectActivity.createLaunchIntent(activity, tdsToken)
        activity.startActivityForResult(intent, REQUEST_CODE_VERIFY_LAUNCHER)
    }

    @MainThread
    internal fun startWebVerify(tdsToken: ThreeDSecureToken, activity: Activity) {
        val intent = webBrowserResolver.resolve(
            context = activity,
            uri = tdsToken.getTdsEntryUri(),
            callbackUri = tdsToken.getTdsFinishUri()
        )
        if (intent == null) {
            logger.w("Any activity which open Web not found.")
        } else {
            activity.startActivityForResult(intent, REQUEST_CODE_VERIFY)
        }
    }

    @MainThread
    fun handleWebVerifyResult(
        requestCode: Int,
        callback: PayjpVerifyCardResultCallback
    ) {
        if (requestCode == REQUEST_CODE_VERIFY_LAUNCHER) {
            callback.onResult(PayjpVerifierRedirectActivity.getResult())
        }
    }
}
