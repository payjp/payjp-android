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
package jp.pay.android.validator

import jp.pay.android.model.CardBrand
import jp.pay.android.validator.CardNumberValidatorService.CardNumberLengthStatus.MATCH
import jp.pay.android.validator.CardNumberValidatorService.CardNumberLengthStatus.TOO_LONG
import jp.pay.android.validator.CardNumberValidatorService.CardNumberLengthStatus.TOO_SHORT
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

/**
 * [CardNumberValidator]
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
internal class CardNumberValidationTest(
    private val brand: CardBrand,
    private val cardNumber: String,
    private val isLuhnValid: Boolean,
    private val isLengthValid: CardNumberValidatorService.CardNumberLengthStatus
) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            // cf. https://www.freeformatter.com/credit-card-number-generator-validator.html
            return listOf(
                arrayOf(CardBrand.UNKNOWN, "", true, TOO_SHORT),
                arrayOf(CardBrand.UNKNOWN, "12345678901234", false, TOO_SHORT),
                arrayOf(CardBrand.UNKNOWN, "123456789012341", false, TOO_SHORT),
                arrayOf(CardBrand.UNKNOWN, "4242424242424242", true, MATCH),
                arrayOf(CardBrand.UNKNOWN, "12345678901234569", true, TOO_LONG),
                arrayOf(CardBrand.UNKNOWN, "12345678901237ab", false, MATCH),
                arrayOf(CardBrand.UNKNOWN, "ab12345678901237", false, MATCH),
                // Visa
                arrayOf(CardBrand.VISA, "420025079664883", true, TOO_SHORT),
                arrayOf(CardBrand.VISA, "4200250796648831", true, MATCH),
                arrayOf(CardBrand.VISA, "4929613427952262", true, MATCH),
                arrayOf(CardBrand.VISA, "4929610527143692", false, MATCH),
                // Master
                arrayOf(CardBrand.MASTER_CARD, "526927848873749", false, TOO_SHORT),
                arrayOf(CardBrand.MASTER_CARD, "5269278488737492", true, MATCH),
                arrayOf(CardBrand.MASTER_CARD, "5106733522040110", true, MATCH),
                arrayOf(CardBrand.MASTER_CARD, "5589306849102132", false, MATCH),
                // Amex
                arrayOf(CardBrand.AMEX, "34619181662010", false, TOO_SHORT),
                arrayOf(CardBrand.AMEX, "34619181662010801", false, TOO_LONG),
                arrayOf(CardBrand.AMEX, "346191816620108", true, MATCH),
                arrayOf(CardBrand.AMEX, "341179142096577", true, MATCH),
                arrayOf(CardBrand.AMEX, "372086951160373", false, MATCH),
                // Discover
                arrayOf(CardBrand.DISCOVER, "601134165156244", false, TOO_SHORT),
                arrayOf(CardBrand.DISCOVER, "6011341651562441", true, MATCH),
                arrayOf(CardBrand.DISCOVER, "6011290763638088", true, MATCH),
                arrayOf(CardBrand.DISCOVER, "6011621030885715", false, MATCH),
                // Diners
                arrayOf(CardBrand.DINERS_CLUB, "3686800380127", false, TOO_SHORT),
                arrayOf(CardBrand.DINERS_CLUB, "36868003801279", true, MATCH),
                arrayOf(CardBrand.DINERS_CLUB, "36785415704877", true, MATCH),
                arrayOf(CardBrand.DINERS_CLUB, "36267608413862", false, MATCH),
                // JCB
                arrayOf(CardBrand.JCB, "353340187998212", false, TOO_SHORT),
                arrayOf(CardBrand.JCB, "3533401879982122", true, MATCH),
                arrayOf(CardBrand.JCB, "3535909680226735", true, MATCH),
                arrayOf(CardBrand.JCB, "3534067821171002", false, MATCH)
            )
        }
    }

    @Test
    fun checkLuhn() {
        assertThat(
            "cardNumber $cardNumber",
            CardNumberValidator.isLuhnValid(cardNumber),
            `is`(isLuhnValid)
        )
    }

    @Test
    fun checkLength() {
        assertThat(
            "cardNumber $cardNumber",
            CardNumberValidator.isCardNumberLengthValid(cardNumber, brand),
            `is`(isLengthValid)
        )
    }
}
