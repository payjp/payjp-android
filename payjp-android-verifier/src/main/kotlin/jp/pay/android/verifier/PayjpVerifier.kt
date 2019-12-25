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
package jp.pay.android.verifier

import android.app.Activity
import android.content.Intent
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import jp.pay.android.PayjpLogger
import jp.pay.android.model.Card
import jp.pay.android.model.Token
import jp.pay.android.verifier.ui.PayjpVerifyCardActivity
import jp.pay.android.verifier.ui.PayjpVerifyCardResultCallback

object PayjpVerifier {

    internal const val VERIFY_WEB_ENDPOINT_HOST = "api.pay-stage.com" // TODO

    var logger: PayjpLogger = PayjpLogger.None

    @MainThread
    fun startWebVerify(token: Token, activity: Activity, requestCode: Int? = null) {
        PayjpVerifyCardActivity.start(activity, token, requestCode)
    }

    @MainThread
    fun startWebVerify(card: Card, activity: Activity, requestCode: Int? = null) {
        PayjpVerifyCardActivity.start(activity, card, requestCode)
    }

    @MainThread
    fun startWebVerify(card: Card, fragment: Fragment, requestCode: Int? = null) {
        PayjpVerifyCardActivity.start(fragment, card, requestCode)
    }

    @MainThread
    fun handleWebVerifyResult(data: Intent?, callback: PayjpVerifyCardResultCallback) {
        PayjpVerifyCardActivity.onActivityResult(data, callback)
    }
}
