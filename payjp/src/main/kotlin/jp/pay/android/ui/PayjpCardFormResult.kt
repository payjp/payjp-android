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
package jp.pay.android.ui

import jp.pay.android.model.Token

/**
 * State represents of result from the Activity that has card form.
 *
 */
sealed class PayjpCardFormResult {

    /**
     *  Activity finished with data which include [jp.pay.android.model.Token].
     *
     *  @param token token the card form created.
     */
    data class Success(val token: Token) : PayjpCardFormResult()

    /**
     * Activity finished with no data.
     */
    object Canceled : PayjpCardFormResult()

    /**
     * Return it is success.
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Return it is canceled.
     */
    fun isCanceled(): Boolean = this === Canceled

    /**
     * Get out token from result. If it is not success, throw exception.
     *
     */
    fun retrieveToken(): Token {
        val success = this as? Success
            ?: throw IllegalStateException("Cannot call retrieveToken() when it is not success")
        return success.token
    }
}
