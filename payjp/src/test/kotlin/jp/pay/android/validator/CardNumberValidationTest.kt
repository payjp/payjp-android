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
package jp.pay.android.validator

import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

/**
 * [CardNumberValidator]
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class CardNumberValidationTest(
    private val cardNumber: String,
    private val valid: Boolean
) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            // cf. https://www.freeformatter.com/credit-card-number-generator-validator.html
            return listOf(
                arrayOf("", false),
                arrayOf("1234567890123", false), // 13
                arrayOf("12345678901234", false), // luhn ng
                arrayOf("12345678901237", true), // luhn ok
                arrayOf("4242424242424242", true),
                arrayOf("12345678901234569", false),
                arrayOf("12345678901237ab", false),
                arrayOf("ab12345678901237", false),
                // Visa
                arrayOf("4200250796648831", true),
                arrayOf("4929613427952262", true),
                arrayOf("4929610527143692", false),
                // Master
                arrayOf("5269278488737492", true),
                arrayOf("5106733522040110", true),
                arrayOf("5589306849102132", false),
                // Amex
                arrayOf("346191816620108", true),
                arrayOf("341179142096577", true),
                arrayOf("372086951160373", false),
                // Discover
                arrayOf("6011341651562441", true),
                arrayOf("6011290763638088", true),
                arrayOf("6011621030885715", false),
                // Diners
                arrayOf("36868003801279", true),
                arrayOf("36785415704877", true),
                arrayOf("36267608413862", false),
                // JCB
                arrayOf("3533401879982122", true),
                arrayOf("3535909680226735", true),
                arrayOf("3534067821171002", false)
            )
        }
    }

    @Test
    fun checkValidation() {
        assertThat("mismatch in $cardNumber", CardNumberValidator.isValidCardNumber(cardNumber), `is`(valid))
    }
}