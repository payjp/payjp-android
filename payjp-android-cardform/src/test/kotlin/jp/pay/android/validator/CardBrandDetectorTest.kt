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

import jp.pay.android.model.CardBrand
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class CardBrandDetectorTest(
    private val digits: String,
    private val brand: CardBrand
) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            return listOf(
                // UNKNOWN
                arrayOf("0", CardBrand.UNKNOWN),
                arrayOf("9999", CardBrand.UNKNOWN),
                arrayOf("77", CardBrand.UNKNOWN),
                arrayOf("1234", CardBrand.UNKNOWN),
                arrayOf("111122223333444455556666", CardBrand.UNKNOWN),
                arrayOf("99999999999999999999999999999999999999999999999999", CardBrand.UNKNOWN),
                // VISA
                arrayOf("4", CardBrand.VISA),
                arrayOf("42", CardBrand.VISA),
                arrayOf("4211", CardBrand.VISA),
                arrayOf("4242111122223333", CardBrand.VISA),
                arrayOf("42421111222233334444", CardBrand.VISA),
                // MASTER_CARD
                arrayOf("51", CardBrand.MASTER_CARD),
                arrayOf("52", CardBrand.MASTER_CARD),
                arrayOf("53", CardBrand.MASTER_CARD),
                arrayOf("54", CardBrand.MASTER_CARD),
                arrayOf("55", CardBrand.MASTER_CARD),
                arrayOf("5151", CardBrand.MASTER_CARD),
                arrayOf("2221", CardBrand.MASTER_CARD),
                arrayOf("2230", CardBrand.MASTER_CARD),
                arrayOf("2300", CardBrand.MASTER_CARD),
                arrayOf("2537", CardBrand.MASTER_CARD),
                arrayOf("2720", CardBrand.MASTER_CARD),
                arrayOf("5151222233334444", CardBrand.MASTER_CARD),
                arrayOf("2221222233334444", CardBrand.MASTER_CARD),
                arrayOf("22212222333344445555", CardBrand.MASTER_CARD),
                // JCB
                arrayOf("35", CardBrand.UNKNOWN),
                arrayOf("352", CardBrand.UNKNOWN),
                arrayOf("353", CardBrand.JCB),
                arrayOf("3528", CardBrand.JCB),
                arrayOf("3511", CardBrand.UNKNOWN),
                arrayOf("3531222233334444", CardBrand.JCB),
                arrayOf("35312222333344445555", CardBrand.JCB),
                // AMEX
                arrayOf("34", CardBrand.AMEX),
                arrayOf("37", CardBrand.AMEX),
                arrayOf("340", CardBrand.AMEX),
                arrayOf("3718", CardBrand.AMEX),
                arrayOf("341122223333444", CardBrand.AMEX),
                arrayOf("34112222333344445555", CardBrand.AMEX),
                // DINERS_CLUB
                arrayOf("300", CardBrand.DINERS_CLUB),
                arrayOf("301", CardBrand.DINERS_CLUB),
                arrayOf("302", CardBrand.DINERS_CLUB),
                arrayOf("303", CardBrand.DINERS_CLUB),
                arrayOf("304", CardBrand.DINERS_CLUB),
                arrayOf("305", CardBrand.DINERS_CLUB),
                arrayOf("306", CardBrand.UNKNOWN),
                arrayOf("36", CardBrand.DINERS_CLUB),
                arrayOf("38", CardBrand.DINERS_CLUB),
                arrayOf("3001", CardBrand.DINERS_CLUB),
                arrayOf("3052", CardBrand.DINERS_CLUB),
                arrayOf("363", CardBrand.DINERS_CLUB),
                arrayOf("384", CardBrand.DINERS_CLUB),
                arrayOf("30012222333344", CardBrand.DINERS_CLUB),
                arrayOf("38012222333344", CardBrand.DINERS_CLUB),
                arrayOf("38012222333344445555", CardBrand.DINERS_CLUB),
                // DISCOVER
                arrayOf("6", CardBrand.UNKNOWN),
                arrayOf("601", CardBrand.UNKNOWN),
                arrayOf("6011", CardBrand.DISCOVER),
                arrayOf("60112", CardBrand.DISCOVER),
                arrayOf("6011222233334444", CardBrand.DISCOVER),
                arrayOf("6511222233334444", CardBrand.DISCOVER),
                arrayOf("65112222333344445555", CardBrand.DISCOVER),
                arrayOf("65", CardBrand.DISCOVER),
                arrayOf("650", CardBrand.DISCOVER)
            )
        }
    }

    @Test
    fun detectBrand() {
        assertThat("digits = $digits", CardBrandDetector.detectWithDigits(digits), `is`(brand))
    }
}
