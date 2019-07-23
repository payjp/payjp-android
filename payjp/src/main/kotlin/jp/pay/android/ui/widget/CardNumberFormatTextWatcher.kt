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
package jp.pay.android.ui.widget

import android.text.Editable
import android.text.TextWatcher
import jp.pay.android.model.CardBrand

// card number max digits size
// 16 as max even if brand is amex or diners
private const val TOTAL_MAX_DIGITS = 16
// digits + 3 delimiters
private const val TOTAL_MAX_SYMBOLS = 19

internal class CardNumberFormatTextWatcher(private val delimiter: Char) : TextWatcher {

    // position include delimiter
    private val delimiterPositionsCommon = listOf(4, 9, 14)

    private val delimiterPositionsAmexDiners = listOf(4, 11)

    var brand: CardBrand = CardBrand.UNKNOWN

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable) {
        if (!isInputCorrect(s)) {
            s.replace(0, s.length, buildCorrectString(createDigitArray(s)))
        }
    }

    private fun isInputCorrect(s: Editable): Boolean =
        s.length <= TOTAL_MAX_SYMBOLS &&
            (0 until s.length).all { i ->
                val delimiterPositions = getDelimiterPositions()
                when {
                    i > 0 && delimiterPositions.contains(i) -> delimiter == s[i]
                    else -> Character.isDigit(s[i])
                }
            }

    private fun buildCorrectString(digits: CharArray): String {
        val formatted = StringBuilder()
        val delimiterPositions = getDelimiterPositions()

        for (i in digits.indices) {
            val c = digits[i]
            if (Character.isDigit(c)) {
                formatted.append(c)
                if (i > 0 && i < digits.size - 1 && delimiterPositions.contains(formatted.lastIndex + 1)) {
                    formatted.append(delimiter)
                }
            }
        }

        return formatted.toString()
    }

    private fun getDelimiterPositions() = when (brand) {
        CardBrand.AMEX, CardBrand.DINERS_CLUB -> delimiterPositionsAmexDiners
        else -> delimiterPositionsCommon
    }

    private fun createDigitArray(s: Editable): CharArray {
        val digits = CharArray(TOTAL_MAX_DIGITS)
        var index = 0
        var i = 0
        while (i < s.length && index < TOTAL_MAX_DIGITS) {
            val current = s[i]
            if (Character.isDigit(current)) {
                digits[index] = current
                index++
            }
            i++
        }
        return digits
    }
}