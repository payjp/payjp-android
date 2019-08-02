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
package com.example.payjp.samplekotlin

import android.app.Application
import jp.pay.android.PayjpToken
import jp.pay.android.PayjpTokenConfiguration
import jp.pay.android.cardio.PayjpCardScannerPlugin

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PayjpToken.init(PayjpTokenConfiguration.Builder("pk_test_0383a1b8f91e8a6e3ea0e2a9")
            .setDebugEnabled(BuildConfig.DEBUG)
            .setCardScannerPlugin(PayjpCardScannerPlugin)
            .build())
    }
}