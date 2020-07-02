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
package jp.pay.android.model

import jp.pay.android.R
import jp.pay.android.anyNullable
import jp.pay.android.validator.CardExpirationInputTransformer
import jp.pay.android.validator.CardExpirationProcessorService
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyChar
import org.mockito.Mockito.anyString
import org.mockito.MockitoAnnotations
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class CardExpirationInputTest(
    private val input: String?,
    private val mockMonthYear: Pair<String, String?>?,
    private val mockValidateMonth: Boolean,
    private val mockExpiration: CardExpiration?,
    private val result: CardExpiration?,
    private val errorMessage: FormInputError?
) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {

            return listOf(
                arrayOf(null, null, false, null, null, FormInputError(R.string.payjp_card_form_error_no_expiration, true)),
                arrayOf("", null, false, null, null, FormInputError(R.string.payjp_card_form_error_no_expiration, true)),
                arrayOf("12/20", null, false, null, null, FormInputError(R.string.payjp_card_form_error_invalid_expiration, true)),
                arrayOf(
                    "12/20", "12" to "20", false, null,
                    null, FormInputError(R.string.payjp_card_form_error_invalid_expiration, false)
                ),
                arrayOf(
                    "12/20", "12" to null, true, null,
                    null, FormInputError(R.string.payjp_card_form_error_invalid_expiration, true)
                ),
                arrayOf(
                    "12/20", "12" to "20", true, null,
                    null, FormInputError(R.string.payjp_card_form_error_invalid_expiration, false)
                ),
                arrayOf(
                    "12/20", "12" to "20", true, CardExpiration("12", "2020"),
                    CardExpiration("12", "2020"), null
                )
            )
        }
    }

    @Mock
    private lateinit var processor: CardExpirationProcessorService
    private lateinit var expirationInput: CardComponentInput.CardExpirationInput

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(processor.processExpirationMonthYear(anyString(), anyChar())).thenReturn(mockMonthYear)
        `when`(processor.validateMonth(anyString())).thenReturn(mockValidateMonth)
        `when`(processor.processCardExpiration(anyNullable(), anyNullable())).thenReturn(mockExpiration)
        val transformer = CardExpirationInputTransformer('/', processor)
        expirationInput = transformer.transform(input)
    }

    @Test
    fun checkValue() {
        assertThat(expirationInput.value, `is`(result))
    }

    @Test
    fun checkErrorMessage() {
        assertThat(expirationInput.errorMessage, `is`(errorMessage))
    }
}
