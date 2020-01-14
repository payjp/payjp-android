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
package jp.pay.android.validator

import android.util.Log
import jp.pay.android.PayjpConstants

/**
 * Validate publicKey
 *
 */
internal object PublicKeyValidator {

    fun validate(publicKey: String): String = publicKey.apply {
        require(isNotBlank()) {
            "You need to set publickey for PAY.JP. You can find in https://pay.jp/d/settings ."
        }
        require(!startsWith("sk_")) {
            "You are using secretkey (`sk_xxxx`) instead of PAY.JP publickey." +
                "You can find **public** key like `pk_xxxxxx` in https://pay.jp/d/settings ."
        }
        if (startsWith("pk_test")) {
            Log.w(
                PayjpConstants.TAG_FOR_LOG, "PAY.JP now use **TEST** mode key." +
                    "In production, you should use livemode key like `pk_live_xxxx`."
            )
        }
    }
}
