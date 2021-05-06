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
package jp.pay.android.verifier

import android.app.Activity
import androidx.annotation.MainThread
import jp.pay.android.PayjpLogger
import jp.pay.android.PayjpTokenService
import jp.pay.android.Task
import jp.pay.android.model.Token
import jp.pay.android.model.TokenId
import jp.pay.android.verifier.ui.PayjpThreeDSecureResult
import jp.pay.android.verifier.ui.PayjpThreeDSecureResultCallback
import jp.pay.android.verifier.ui.PayjpThreeDSecureStepActivity

object PayjpVerifier {
    private const val REQUEST_CODE_VERIFY_LAUNCHER = 10
    private const val REQUEST_CODE_VERIFY = 11
    private var logger: PayjpLogger = PayjpLogger.None
    private var tokenService: PayjpTokenService? = null
    private var threeDSecureRedirectName: String? = null
    private val webBrowserResolver = WebBrowserResolver(
        WebBrowser.ChromeTab,
        // WebBrowser.AnyBrowsable,
        WebBrowser.InAppWeb
    )

    fun configure(
        logger: PayjpLogger,
        tokenService: PayjpTokenService,
        threeDSecureRedirectName: String?
    ) {
        this.logger = logger
        this.tokenService = tokenService
        this.threeDSecureRedirectName = threeDSecureRedirectName
    }

    internal fun logger(): PayjpLogger = logger

    private fun tokenService(): PayjpTokenService = checkNotNull(tokenService) {
        "You must initialize Payjp first"
    }

    /**
     * Start 3DS authorization flow with [Token].
     *
     * @param tokenId id of token which has 3DS-unverified card.
     * @param activity current activity.
     */
    @MainThread
    fun startThreeDSecureFlow(tokenId: TokenId, activity: Activity) {
        val intent = PayjpThreeDSecureStepActivity.createLaunchIntent(activity, tokenId)
        activity.startActivityForResult(intent, REQUEST_CODE_VERIFY_LAUNCHER)
    }

    @MainThread
    internal fun openThreeDSecure(tokenId: TokenId, activity: Activity) {
        val intent = webBrowserResolver.resolve(
            context = activity,
            uri = tokenId.getVerificationEntryUri(
                publicKey = tokenService().getPublicKey(),
                redirectUrlName = threeDSecureRedirectName
            ),
            callbackUri = tokenId.getVerificationFinishUri()
        )
        if (intent == null) {
            logger.w("Any activity which open Web not found.")
        } else {
            logger.d("Intent $intent")
            logger.d("data ${intent.data}")
            activity.startActivityForResult(intent, REQUEST_CODE_VERIFY)
        }
    }

    @MainThread
    fun handleThreeDSecureResult(
        requestCode: Int,
        callback: PayjpThreeDSecureResultCallback
    ) {
        if (requestCode == REQUEST_CODE_VERIFY_LAUNCHER) {
            callback.onResult(PayjpThreeDSecureStepActivity.getResult())
        }
    }

    fun completeTokenThreeDSecure(result: PayjpThreeDSecureResult): Task<Token>? = when (result) {
        is PayjpThreeDSecureResult.SuccessTokenId -> tokenService().finishTokenThreeDSecure(result.id)
        else -> null
    }
}
