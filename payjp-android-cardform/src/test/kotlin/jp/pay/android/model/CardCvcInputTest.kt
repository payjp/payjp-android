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
package jp.pay.android.model

import jp.pay.android.R
import jp.pay.android.model.CardBrand.AMEX
import jp.pay.android.model.CardBrand.DINERS_CLUB
import jp.pay.android.model.CardBrand.DISCOVER
import jp.pay.android.model.CardBrand.JCB
import jp.pay.android.model.CardBrand.MASTER_CARD
import jp.pay.android.model.CardBrand.UNKNOWN
import jp.pay.android.model.CardBrand.VISA
import jp.pay.android.validator.CardCvcInputTransformer
import jp.pay.android.validator.CardCvcInputTransformerService
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class CardCvcInputTest(
    private val brand: CardBrand,
    private val input: String?,
    private val value: String?,
    private val errorMessage: FormInputError?
) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf(
                    UNKNOWN,
                    null,
                    null,
                    FormInputError(R.string.payjp_card_form_error_no_cvc, true)
                ),
                arrayOf(
                    UNKNOWN,
                    "",
                    null,
                    FormInputError(R.string.payjp_card_form_error_no_cvc, true)
                ),
                arrayOf(
                    UNKNOWN,
                    "abc",
                    null,
                    FormInputError(R.string.payjp_card_form_error_no_cvc, false)
                ),
                arrayOf(
                    UNKNOWN,
                    "12",
                    null,
                    FormInputError(R.string.payjp_card_form_error_invalid_cvc, true)
                ),
                arrayOf(
                    UNKNOWN,
                    "12abc",
                    null,
                    FormInputError(R.string.payjp_card_form_error_invalid_cvc, true)
                ),
                arrayOf(
                    UNKNOWN,
                    " 12 ",
                    null,
                    FormInputError(R.string.payjp_card_form_error_invalid_cvc, true)
                ),
                arrayOf(
                    UNKNOWN,
                    "12345",
                    null,
                    FormInputError(R.string.payjp_card_form_error_invalid_cvc, false)
                ),
                arrayOf(VISA, "123", "123", null),
                arrayOf(VISA, "123a", "123", null),
                arrayOf(VISA, " 123 ", "123", null),
                arrayOf(MASTER_CARD, "123", "123", null),
                arrayOf(JCB, "123", "123", null),
                arrayOf(DINERS_CLUB, "123", "123", null),
                arrayOf(DISCOVER, "123", "123", null),
                arrayOf(
                    AMEX,
                    "123",
                    null,
                    FormInputError(R.string.payjp_card_form_error_invalid_cvc, true)
                ),
                arrayOf(
                    UNKNOWN,
                    "123",
                    null,
                    FormInputError(R.string.payjp_card_form_error_invalid_cvc, true)
                ),
                arrayOf(
                    VISA,
                    "1234",
                    null,
                    FormInputError(R.string.payjp_card_form_error_invalid_cvc, false)
                ),
                arrayOf(
                    MASTER_CARD,
                    "1234",
                    null,
                    FormInputError(R.string.payjp_card_form_error_invalid_cvc, false)
                ),
                arrayOf(
                    JCB,
                    "1234",
                    null,
                    FormInputError(R.string.payjp_card_form_error_invalid_cvc, false)
                ),
                arrayOf(
                    DINERS_CLUB,
                    "1234",
                    null,
                    FormInputError(R.string.payjp_card_form_error_invalid_cvc, false)
                ),
                arrayOf(
                    DISCOVER,
                    "1234",
                    null,
                    FormInputError(R.string.payjp_card_form_error_invalid_cvc, false)
                ),
                arrayOf(AMEX, "1234", "1234", null),
                arrayOf(UNKNOWN, "1234", "1234", null),
                arrayOf(UNKNOWN, " 1234 ", "1234", null)
            )
        }
    }

    private lateinit var transformer: CardCvcInputTransformerService
    private lateinit var cvcInput: CardComponentInput.CardCvcInput

    @Before
    fun setUp() {
        transformer = CardCvcInputTransformer()
        transformer.brand = brand
        cvcInput = transformer.transform(input)
    }

    @Test
    fun checkValue() {
        assertThat("input = $input", cvcInput.value, `is`(value))
    }

    @Test
    fun checkErrorMessage() {
        assertThat("input = $input", cvcInput.errorMessage, `is`(errorMessage))
    }
}
