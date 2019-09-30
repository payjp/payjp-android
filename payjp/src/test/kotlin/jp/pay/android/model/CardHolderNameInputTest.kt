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
package jp.pay.android.model

import jp.pay.android.R
import jp.pay.android.validator.CardHolderNameInputTransformer
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class CardHolderNameInputTest(
    private val input: String?,
    private val value: String?,
    private val errorMessage: FormInputError?
) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf(null as? String, null as? String,
                    FormInputError(R.string.payjp_card_form_error_no_holder_name, true)),
                arrayOf("", null,
                    FormInputError(R.string.payjp_card_form_error_no_holder_name, true)),
                arrayOf(" ", null,
                    FormInputError(R.string.payjp_card_form_error_no_holder_name, true)),
                arrayOf("JANE DOE", "JANE DOE", null),
                arrayOf(" JANE DOE ", "JANE DOE", null)
            )
        }
    }

    private lateinit var holderNameInput: CardComponentInput.CardHolderNameInput

    @Before
    fun setUp() {
        holderNameInput = CardHolderNameInputTransformer.transform(input)
    }

    @Test
    fun checkValue() {
        assertThat("input = $input", holderNameInput.value, `is`(value))
    }

    @Test
    fun checkErrorMessage() {
        assertThat("input = $input", holderNameInput.errorMessage, `is`(errorMessage))
    }
}