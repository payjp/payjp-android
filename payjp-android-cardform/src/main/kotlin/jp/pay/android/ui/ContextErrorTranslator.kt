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
package jp.pay.android.ui

import android.content.Context
import jp.pay.android.R
import jp.pay.android.exception.PayjpApiException
import jp.pay.android.exception.PayjpCardException
import jp.pay.android.exception.PayjpRateLimitException
import java.io.IOException

/**
 * Provide error message by context or from api response.
 *
 * @param context context
 */
internal class ContextErrorTranslator(context: Context) : ErrorTranslator {

    private val context = context.applicationContext

    override fun translate(throwable: Throwable): CharSequence {
        return when (throwable) {
            is PayjpCardException -> throwable.apiError.message
            is PayjpRateLimitException -> context.getString(R.string.payjp_card_form_screen_error_rate_limit_exceeded)
            is PayjpApiException -> when (throwable.httpStatusCode) {
                in 500 until 600 -> context.getString(R.string.payjp_card_form_screen_error_server)
                else -> context.getString(R.string.payjp_card_form_screen_error_application)
            }
            is IOException -> context.getString(R.string.payjp_card_form_screen_error_network)
            else -> context.getString(R.string.payjp_card_form_screen_error_unknown)
        }
    }
}
