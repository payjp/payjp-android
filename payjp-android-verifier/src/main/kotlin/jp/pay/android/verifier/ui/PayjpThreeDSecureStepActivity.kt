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
        internal const val EXTRA_KEY_TOKEN_ID = "EXTRA_KEY_TOKEN_ID" // Kept for backward compatibility with older versions
        internal const val EXTRA_KEY_RESOURCE_ID = "EXTRA_KEY_RESOURCE_ID"
        internal val intentHolder: IntentHolder = IntentHolder()

        internal fun createLaunchIntent(context: Context, resourceId: String): Intent {
            return Intent(context, PayjpThreeDSecureStepActivity::class.java)
                .putExtra(EXTRA_KEY_RESOURCE_ID, resourceId)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        internal fun getResult(): PayjpThreeDSecureResult {
            val intent = intentHolder.pop() ?: return PayjpThreeDSecureResult.Canceled
            PayjpVerifier.logger().d("getResult intent: $intent")
            val uri = intent.data
            val resourceId = intent.getStringExtra(EXTRA_KEY_RESOURCE_ID) ?: intent.getStringExtra(EXTRA_KEY_TOKEN_ID)
            return when {
                uri == null -> PayjpThreeDSecureResult.Canceled
                resourceId != null -> PayjpThreeDSecureResult.SuccessResourceId(id = resourceId)
                else -> PayjpThreeDSecureResult.Canceled
            }
        }
    }

    private var currentResourceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_KEY_RESOURCE_ID)) {
                currentResourceId = savedInstanceState.getString(EXTRA_KEY_RESOURCE_ID)
            } else if (savedInstanceState.containsKey(EXTRA_KEY_TOKEN_ID)) {
                currentResourceId = savedInstanceState.getString(EXTRA_KEY_TOKEN_ID)

                if (currentResourceId == null) {
                    try {
                        val tokenId = savedInstanceState.getParcelable<TokenId>(EXTRA_KEY_TOKEN_ID)
                        currentResourceId = tokenId?.id
                        PayjpVerifier.logger().d("Migrated from legacy TokenId format: $currentResourceId")
                    } catch (e: Exception) {
                        PayjpVerifier.logger().e("Error retrieving legacy TokenId: ${e.message}")
                    }
                }
            }
        } else {
            intent?.let { i ->
                val resourceId = i.getStringExtra(EXTRA_KEY_RESOURCE_ID) ?: i.getStringExtra(EXTRA_KEY_TOKEN_ID)
                resourceId?.let { resourceId ->
                    currentResourceId = resourceId
                    intentHolder.pop()
                    PayjpVerifier.openThreeDSecure(resourceId, this)
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
        currentResourceId?.let { resourceId ->
            outState.putString(EXTRA_KEY_RESOURCE_ID, resourceId)
        }
    }

    @VisibleForTesting
    internal fun onNewIntentInternal(intent: Intent?) {
        PayjpVerifier.logger().d("onNewIntent uri ${intent?.data}")
        intent?.data?.let { uri ->
            intentHolder.intent = Intent().setData(uri)
                .putExtra(EXTRA_KEY_RESOURCE_ID, currentResourceId)
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}
