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
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.R
import jp.pay.android.exception.PayjpApiException
import jp.pay.android.exception.PayjpCardException
import jp.pay.android.exception.PayjpRateLimitException
import jp.pay.android.model.ApiError
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ContextErrorTranslatorTest {

    @Mock
    private lateinit var mockContext: Context

    private val mockErrorMessageUnknown = "unknown"
    private val mockErrorMessageNetwork = "network"
    private val mockErrorMessageServer = "server"
    private val mockErrorMessageApplication = "application"
    private val mockErrorMessageRateLimit = "rateLimit"

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(mockContext.applicationContext)
            .thenReturn(mockContext)
        `when`(mockContext.getString(R.string.payjp_card_form_screen_error_unknown))
            .thenReturn(mockErrorMessageUnknown)
        `when`(mockContext.getString(R.string.payjp_card_form_screen_error_network))
            .thenReturn(mockErrorMessageNetwork)
        `when`(mockContext.getString(R.string.payjp_card_form_screen_error_server))
            .thenReturn(mockErrorMessageServer)
        `when`(mockContext.getString(R.string.payjp_card_form_screen_error_application))
            .thenReturn(mockErrorMessageApplication)
        `when`(mockContext.getString(R.string.payjp_card_form_screen_error_rate_limit_exceeded))
            .thenReturn(mockErrorMessageRateLimit)
    }

    @Test
    fun translate_card_exception_to_own_message() {
        val message = "omg"
        val error = PayjpCardException(
            message = message,
            cause = RuntimeException(),
            apiError = ApiError(
                code = "invalid_number",
                message = message,
                type = "card_error"
            ),
            source = ""
        )
        assertThat(ContextErrorTranslator(mockContext).translate(error).toString(), `is`(message))
    }

    @Test
    fun translate_api_error_401_to_fixed_message_with_code() {
        val message = "omg"
        val code = "unauthorized"
        val error = PayjpApiException(
            message = message,
            cause = RuntimeException(),
            httpStatusCode = 401,
            apiError = ApiError(
                code = code,
                message = message,
                type = "client_error"
            ),
            source = ""
        )
        assertThat(
            ContextErrorTranslator(mockContext).translate(error).toString(),
            `is`("$mockErrorMessageApplication (code:$code)")
        )
    }

    @Test
    fun translate_api_error_403_to_fixed_message_with_code() {
        val message = "omg"
        val code = "card_flagged"
        val error = PayjpApiException(
            message = message,
            cause = RuntimeException(),
            httpStatusCode = 403,
            apiError = ApiError(
                code = code,
                message = message,
                type = "forbidden"
            ),
            source = ""
        )
        assertThat(
            ContextErrorTranslator(mockContext).translate(error).toString(),
            `is`("$mockErrorMessageApplication (code:$code)")
        )
    }

    @Test
    fun translate_api_error_502_to_own_message() {
        val message = "omg"
        val code = "maintenance"
        val error = PayjpApiException(
            message = message,
            cause = RuntimeException(),
            httpStatusCode = 502,
            apiError = ApiError(
                code = code,
                message = message,
                type = "server_error"
            ),
            source = ""
        )
        assertThat(
            ContextErrorTranslator(mockContext).translate(error).toString(),
            `is`("$mockErrorMessageServer (code:$code)")
        )
    }

    @Test
    fun translate_rate_limit_exception_to_own_message() {
        val message = "omg"
        val error = PayjpRateLimitException(
            message = message,
            cause = RuntimeException(),
            apiError = ApiError(
                code = "pg_wrong",
                message = message,
                type = "server_error"
            ),
            source = ""
        )
        assertThat(
            ContextErrorTranslator(mockContext).translate(error).toString(),
            `is`(mockErrorMessageRateLimit)
        )
    }

    @Test
    fun translate_network_error_to_fixed_message() {
        val error = IOException("omg")
        assertThat(
            ContextErrorTranslator(mockContext).translate(error).toString(),
            `is`(mockErrorMessageNetwork)
        )
    }

    @Test
    fun translate_unknown_error_to_fixed_message() {
        val error = java.lang.RuntimeException("omg")
        assertThat(
            ContextErrorTranslator(mockContext).translate(error).toString(),
            `is`(mockErrorMessageUnknown)
        )
    }
}
