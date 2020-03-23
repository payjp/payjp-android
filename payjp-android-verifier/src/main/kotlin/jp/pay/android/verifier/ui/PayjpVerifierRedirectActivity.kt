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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import jp.pay.android.verifier.PayjpVerifier

class PayjpVerifierRedirectActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_KEY_SUCCESS = "EXTRA_KEY_SUCCESS"

        fun getResult(data: Intent?): PayjpVerifyCardResult {
            val success = data?.getBooleanExtra(EXTRA_KEY_SUCCESS, false) ?: false
            return if (success) {
                PayjpVerifyCardResult.Success
            } else {
                PayjpVerifyCardResult.Canceled
            }
        }

        fun setEnabled(context: Context, enabled: Boolean) {
            context.applicationContext.run {
                val enabledFlag = if (enabled) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                packageManager.setComponentEnabledSetting(
                    ComponentName(packageName, PayjpVerifierRedirectActivity::class.java.name),
                    enabledFlag,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.data?.let {
            val intent = Intent().apply {
                setClassName(packageName, "jp.pay.android.ui.PayjpCardFormActivity")
                putExtra(EXTRA_KEY_SUCCESS, true)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }

        PayjpVerifier.logger().d("return uri ${intent.data}")

        finish()
    }
}
