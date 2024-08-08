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

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.R
import jp.pay.android.anyNullable
import jp.pay.android.data.PhoneNumberService
import jp.pay.android.model.CountryCode
import jp.pay.android.model.FormInputError
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.MockitoAnnotations
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class CardPhoneNumberInputTransformerTest {

    @Mock
    private lateinit var mockPhoneNumberService: PhoneNumberService

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun transform_empty() {
        val transformer = CardPhoneNumberInputTransformer(
            context = ApplicationProvider.getApplicationContext(),
            service = mockPhoneNumberService
        ).apply {
            currentCountryCode = CountryCode("JP", 81, Locale.US)
        }
        val result = transformer.transform(" ")
        assertThat(result.value, `is`(nullValue()))
        assertThat(result.errorMessage, `is`(FormInputError(R.string.payjp_card_form_error_no_phone_number, true)))
    }

    @Test
    fun transform_normalized() {
        val transformer = CardPhoneNumberInputTransformer(
            context = ApplicationProvider.getApplicationContext(),
            service = mockPhoneNumberService
        ).apply {
            currentCountryCode = CountryCode("JP", 81, Locale.US)
        }
        val validNumber = "+819012345678"
        `when`(mockPhoneNumberService.normalize(anyNullable(), anyString(), anyNullable())).thenReturn(validNumber)
        val result = transformer.transform("09012345678")
        assertThat(result.value, `is`(validNumber))
        assertThat(result.errorMessage, `is`(nullValue()))
    }

    @Test
    fun transform_invalid_tooLong() {
        val transformer = CardPhoneNumberInputTransformer(
            context = ApplicationProvider.getApplicationContext(),
            service = mockPhoneNumberService
        ).apply {
            currentCountryCode = CountryCode("JP", 81, Locale.US)
        }
        `when`(mockPhoneNumberService.normalize(anyNullable(), anyString(), anyNullable())).thenReturn(null)
        `when`(mockPhoneNumberService.examplePhoneNumber(anyNullable(), anyNullable())).thenReturn("09012345678")
        val result = transformer.transform("09012345678901")
        assertThat(result.value, `is`(nullValue()))
        assertThat(result.errorMessage, `is`(FormInputError(R.string.payjp_card_form_error_invalid_phone_number, false)))
    }

    @Test
    fun transform_invalid_not_tooLong() {
        val transformer = CardPhoneNumberInputTransformer(
            context = ApplicationProvider.getApplicationContext(),
            service = mockPhoneNumberService
        ).apply {
            currentCountryCode = CountryCode("JP", 81, Locale.US)
        }
        `when`(mockPhoneNumberService.normalize(anyNullable(), anyString(), anyNullable())).thenReturn(null)
        `when`(mockPhoneNumberService.examplePhoneNumber(anyNullable(), anyNullable())).thenReturn("09012345678")
        val result = transformer.transform("090123456")
        assertThat(result.value, `is`(nullValue()))
        assertThat(result.errorMessage, `is`(FormInputError(R.string.payjp_card_form_error_invalid_phone_number, true)))
    }
}
