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
package jp.pay.android.validator

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.R
import jp.pay.android.model.FormInputError
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardEmailInputTransformerTest {

    @Test
    fun transform_empty() {
        val transformer = CardEmailInputTransformer()
        val result = transformer.transform(" ")
        assertThat(result.value, `is`(nullValue()))
        assertThat(result.errorMessage, `is`(FormInputError(R.string.payjp_card_form_error_no_email, true)))
    }

    @Test
    fun transform_invalid() {
        val transformer = CardEmailInputTransformer()
        val result = transformer.transform("invalid")
        assertThat(result.value, `is`(nullValue()))
        assertThat(result.errorMessage, `is`(FormInputError(R.string.payjp_card_form_error_invalid_email, false)))
    }

    @Test
    fun transform_valid() {
        val transformer = CardEmailInputTransformer()
        val email = "test@example.com"
        val result = transformer.transform(email)
        assertThat(result.value, `is`(email))
        assertThat(result.errorMessage, `is`(nullValue()))
    }
}
