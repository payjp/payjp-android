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
import android.content.Intent
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import jp.pay.android.PayjpLogger
import jp.pay.android.PayjpTokenService
import jp.pay.android.model.ThreeDSecureId
import jp.pay.android.verifier.ui.PayjpVerifyCardActivity
import jp.pay.android.verifier.ui.PayjpVerifyCardResultCallback

object PayjpVerifier {

    private var logger: PayjpLogger = PayjpLogger.None
    private var tokenService: PayjpTokenService? = null

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
    fun startWebVerify(tdsId: ThreeDSecureId, activity: Activity, requestCode: Int? = null) {
        PayjpVerifyCardActivity.start(activity, tdsId, requestCode)
    }

    @MainThread
    fun startWebVerify(tdsId: ThreeDSecureId, fragment: Fragment, requestCode: Int? = null) {
        PayjpVerifyCardActivity.start(fragment, tdsId, requestCode)
    }

    @MainThread
    fun handleWebVerifyResult(data: Intent?, callback: PayjpVerifyCardResultCallback) {
        PayjpVerifyCardActivity.onActivityResult(data, callback)
    }
}
