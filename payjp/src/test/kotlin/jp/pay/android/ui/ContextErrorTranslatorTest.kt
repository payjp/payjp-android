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

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.IOException
import jp.pay.android.R
import jp.pay.android.exception.PayjpApiException
import jp.pay.android.model.ApiError
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class ContextErrorTranslatorTest {

    @Mock
    private lateinit var mockContext: Context

    private val mockErrorMessageUnknown = "unknown"
    private val mockErrorMessageNetwork = "network"

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(mockContext.applicationContext)
            .thenReturn(mockContext)
        `when`(mockContext.getString(R.string.payjp_card_form_screen_error_unknown))
            .thenReturn(mockErrorMessageUnknown)
        `when`(mockContext.getString(R.string.payjp_card_form_screen_error_network))
            .thenReturn(mockErrorMessageNetwork)
    }

    @Test
    fun error_api_error_to_own_message() {
        val message = "omg"
        val error = PayjpApiException(
            message = message,
            cause = RuntimeException(),
            httpStatusCode = 400,
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
    fun error_network_error_to_fixed_message() {
        val error = IOException("omg")
        assertThat(
            ContextErrorTranslator(mockContext).translate(error).toString(),
            `is`(mockErrorMessageNetwork)
        )
    }

    @Test
    fun error_unknown_error_to_fixed_message() {
        val error = java.lang.RuntimeException("omg")
        assertThat(
            ContextErrorTranslator(mockContext).translate(error).toString(),
            `is`(mockErrorMessageUnknown)
        )
    }
}
