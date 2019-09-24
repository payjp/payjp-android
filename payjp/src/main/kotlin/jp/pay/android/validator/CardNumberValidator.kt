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

import jp.pay.android.model.CardBrand
import jp.pay.android.model.numberLength
import jp.pay.android.validator.CardNumberValidatorService.CardNumberLengthStatus

internal interface CardNumberValidatorService {
    fun isCardNumberLengthValid(
        cardNumber: String,
        brand: CardBrand = CardBrand.UNKNOWN
    ): CardNumberLengthStatus

    fun isLuhnValid(cardNumber: String): Boolean

    enum class CardNumberLengthStatus {
        MATCH, TOO_LONG, TOO_SHORT
    }
}

internal object CardNumberValidator : CardNumberValidatorService {

    /**
     * Card number length check
     *
     * @param cardNumber card number
     * @param brand brand
     */
    override fun isCardNumberLengthValid(
        cardNumber: String,
        brand: CardBrand
    ): CardNumberLengthStatus {
        val brandLength = brand.numberLength
        return when {
            cardNumber.length < brandLength -> CardNumberLengthStatus.TOO_SHORT
            cardNumber.length > brandLength -> CardNumberLengthStatus.TOO_LONG
            else -> CardNumberLengthStatus.MATCH
        }
    }

    /**
     * Check Luhn algorithm
     *
     * @param cardNumber card number
     * @return valid if true
     */
    override fun isLuhnValid(cardNumber: String): Boolean {
        var isOdd = true
        var sum = 0

        for (index in cardNumber.length - 1 downTo 0) {
            val c = cardNumber[index]
            if (!Character.isDigit(c)) {
                return false
            }

            var digit = Character.getNumericValue(c)
            isOdd = !isOdd

            if (isOdd) {
                digit *= 2
            }

            if (digit > 9) {
                digit -= 9
            }

            sum += digit
        }

        return sum % 10 == 0
    }
}