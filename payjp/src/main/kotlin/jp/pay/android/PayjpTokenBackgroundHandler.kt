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
package jp.pay.android

import androidx.annotation.WorkerThread
import jp.pay.android.model.Token

/**
 * After card token created successfully, invoke handler with the token.
 * During this invocation, the card form activity will keep active,
 * and return [CardFormStatus.Complete], the activity will finish.
 * But return [CardFormStatus.Error], the activity will not finish and show error message.
 * The invocation execute in background.
 *
 * In most case, the application send the token to own server,
 * to apply the result to card form activity.
 */
interface PayjpTokenBackgroundHandler {

    /**
     * Invoke in background with [Token].
     *
     * @param token Card Token which is created by card form.
     * @return status Card Form will take the status; finish or show error message.
     */
    @WorkerThread
    fun handleTokenInBackground(token: Token): CardFormStatus

    /**
     * Card Form Status that is result of [PayjpTokenBackgroundHandler.handleTokenInBackground].
     * If the card token send to server successfully, status may be [CardFormStatus.Complete],
     * and the Card form will finish and return the token.
     * If an error happened in sending to server, status may be [CardFormStatus.Error],
     * and the Card form will not finish immediately.
     *
     */
    sealed class CardFormStatus {

        /**
         * Complete status
         */
        class Complete : CardFormStatus()

        /**
         * Error status
         *
         * @param message error message that will be shown.
         */
        data class Error(val message: CharSequence) : CardFormStatus()
    }
}
