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
package jp.pay.android.testing

import jp.pay.android.PayjpCardForm
import jp.pay.android.PayjpLogger
import jp.pay.android.PayjpTokenBackgroundHandler
import jp.pay.android.PayjpTokenService
import org.junit.rules.ExternalResource
import java.util.Locale
import java.util.concurrent.Executor

/**
 * Test rule that configure PayjpService
 */
class PayjpCardFormTestRule(
    private val tokenService: PayjpTokenService,
    private val handler: PayjpTokenBackgroundHandler? = null,
    private val callbackExecutor: Executor? = null,
) : ExternalResource() {

    override fun before() {
        PayjpCardForm.configure(
            logger = PayjpLogger.get(debugEnabled = true),
            tokenService = tokenService,
            handler = handler,
            callbackExecutor = callbackExecutor,
            locale = Locale("en")
        )
    }
}
