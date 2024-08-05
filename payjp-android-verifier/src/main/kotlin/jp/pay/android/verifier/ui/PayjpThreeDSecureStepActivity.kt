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
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import jp.pay.android.model.TokenId
import jp.pay.android.verifier.PayjpVerifier

class PayjpThreeDSecureStepActivity : AppCompatActivity() {

    internal class IntentHolder {
        var intent: Intent? = null

        fun pop(): Intent? {
            val i = intent
            intent = null
            return i
        }
    }

    companion object {
        internal const val EXTRA_KEY_TOKEN_ID = "EXTRA_KEY_TOKEN_ID"
        internal val intentHolder: IntentHolder = IntentHolder()

        internal fun createLaunchIntent(context: Context, tokenId: TokenId): Intent {
            return Intent(context, PayjpThreeDSecureStepActivity::class.java)
                .putExtra(EXTRA_KEY_TOKEN_ID, tokenId)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        internal fun getResult(): PayjpThreeDSecureResult {
            val intent = intentHolder.pop() ?: return PayjpThreeDSecureResult.Canceled
            PayjpVerifier.logger().d("getResult intent: $intent")
            val uri = intent.data
            val resource = IntentCompat.getParcelableExtra(intent, EXTRA_KEY_TOKEN_ID, TokenId::class.java)
            return when {
                uri == null -> PayjpThreeDSecureResult.Canceled
                resource is TokenId -> PayjpThreeDSecureResult.SuccessTokenId(id = resource)
                else -> PayjpThreeDSecureResult.Canceled
            }
        }
    }

    private var currentTokenId: TokenId? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.containsKey(EXTRA_KEY_TOKEN_ID) == true) {
            currentTokenId = BundleCompat.getParcelable(savedInstanceState, EXTRA_KEY_TOKEN_ID, TokenId::class.java)
        } else {
            intent?.let { i ->
                IntentCompat.getParcelableExtra(i, EXTRA_KEY_TOKEN_ID, TokenId::class.java)?.let { tokenId ->
                    currentTokenId = tokenId
                    intentHolder.pop()
                    PayjpVerifier.openThreeDSecure(tokenId, this)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        onNewIntentInternal(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PayjpVerifier.logger().d("onActivityResult requestCode $requestCode resultCode $resultCode")
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentTokenId?.let { tokenId ->
            outState.putParcelable(EXTRA_KEY_TOKEN_ID, tokenId)
        }
    }

    @VisibleForTesting
    internal fun onNewIntentInternal(intent: Intent?) {
        PayjpVerifier.logger().d("onNewIntent uri ${intent?.data}")
        intent?.data?.let { uri ->
            intentHolder.intent = Intent().setData(uri)
                .putExtra(EXTRA_KEY_TOKEN_ID, currentTokenId)
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}
