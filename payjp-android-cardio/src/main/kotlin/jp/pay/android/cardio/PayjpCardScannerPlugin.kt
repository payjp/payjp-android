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
package jp.pay.android.cardio

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import io.card.payment.CardIOActivity
import io.card.payment.CreditCard
import jp.pay.android.plugin.CardScannerPlugin

object PayjpCardScannerPlugin : CardScannerPlugin {

    private const val REQUEST_CODE = 100

    override fun startScanActivity(activity: Activity) {
        activity.startActivityForResult(createIntent(activity), REQUEST_CODE)
    }

    override fun startScanActivity(fragment: Fragment) {
        fragment.activity?.let {
            fragment.startActivityForResult(createIntent(it), REQUEST_CODE)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        listener: CardScannerPlugin.CardScanOnResultListener?
    ): Boolean {
        if (requestCode != requestCode) {
            return false
        }
        data?.getParcelableExtra<CreditCard>(CardIOActivity.EXTRA_SCAN_RESULT)?.let { card ->
            listener?.onScanResult(cardNumber = card.cardNumber)
        }
        return true
    }

    private fun createIntent(context: Context): Intent {
        return Intent(context, CardIOActivity::class.java)
            .putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, false)
            .putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false)
            .putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false)
            .putExtra(CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, false)
            .putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, true)
            .putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }
}