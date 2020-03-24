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
import androidx.appcompat.app.AppCompatActivity
import jp.pay.android.model.ThreeDSecureToken
import jp.pay.android.verifier.PayjpVerifier

class PayjpVerifierRedirectActivity : AppCompatActivity() {

    internal class UrlHolder {
        var uri: Uri? = null

        fun pop(): Uri? {
            val u = uri
            uri = null
            return u
        }
    }

    companion object {
        private const val EXTRA_KEY_TDS = "EXTRA_KEY_TDS"
        internal val returnUrlHolder: UrlHolder = UrlHolder()

        internal fun createLaunchIntent(context: Context, tdsToken: ThreeDSecureToken): Intent {
            return Intent(context, PayjpVerifierRedirectActivity::class.java)
                .putExtra(EXTRA_KEY_TDS, tdsToken.id)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        internal fun getResult(): PayjpVerifyCardResult {
            val uri = returnUrlHolder.pop()
            return if (uri != null) {
                PayjpVerifyCardResult.Success
            } else {
                PayjpVerifyCardResult.Canceled
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getStringExtra(EXTRA_KEY_TDS)?.let { id ->
            returnUrlHolder.pop()
            PayjpVerifier.startWebVerify(ThreeDSecureToken(id = id), this)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        PayjpVerifier.logger().d("onNewIntent uri ${intent?.data}")
        intent?.data?.let { uri ->
            returnUrlHolder.uri = uri
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PayjpVerifier.logger().d("onActivityResult")
        finish()
    }
}
