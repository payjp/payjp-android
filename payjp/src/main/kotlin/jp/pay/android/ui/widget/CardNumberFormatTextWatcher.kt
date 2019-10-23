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
import android.text.Selection
import android.text.TextWatcher
import jp.pay.android.model.CardBrand

internal class CardNumberFormatTextWatcher(private val delimiter: Char) : TextWatcher {

    // position include delimiter
    private val delimiterPositionsCommon = listOf(4, 9, 14)
    private val delimiterPositionsAmexDiners = listOf(4, 11)
    var brand: CardBrand = CardBrand.UNKNOWN
    private var ignoreChanges: Boolean = false
    private var latestChangeStart: Int = 0
    private var latestInsertionSize: Int = 0

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        if (ignoreChanges) {
            return
        }
        latestChangeStart = start
        latestInsertionSize = after
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable) {
        if (ignoreChanges) {
            return
        }
        if (!isInputCorrect(s)) {
            ignoreChanges = true
            val currentCursor = Selection.getSelectionStart(s)
            val original = s.toString()
            s.replace(0, s.length, buildCorrectString(createDigitArray(s), s))
            val formatted = s.toString()
            Selection.setSelection(s, getAdjustedCursorPosition(original, formatted, currentCursor))
            ignoreChanges = false
        }
    }

    private fun isInputCorrect(s: Editable): Boolean {
        val delimiterPositions = getDelimiterPositions()
        return when {
            // Case A. too long
            s.length > (getMaxDigits() + delimiterPositions.size) -> false
            // Case B. When we add `4` into `123`, input should be `1234 `.
            s.length in delimiterPositions && latestInsertionSize > 0 -> false
            // Case C. When we delete 1 character from `1234 `, input should be `123`.
            s.length in delimiterPositions && latestChangeStart == s.length && latestInsertionSize == 0 -> false
            // Case D. When we delete 1 character from `1234 5`, input should be `1234`.
            (s.length - 1) in delimiterPositions && latestChangeStart == s.length && latestInsertionSize == 0 -> false
            else -> s.indices.all { i ->
                when {
                    i > 0 && i in delimiterPositions -> delimiter == s[i]
                    else -> Character.isDigit(s[i])
                }
            }
        }
    }

    private fun buildCorrectString(digits: CharArray, original: CharSequence): String {
        val formatted = StringBuilder()
        val delimiterPositions = getDelimiterPositions()

        for (i in digits.indices) {
            val c = digits[i]
            if (Character.isDigit(c)) {
                formatted.append(c)
                val nextIndex = formatted.lastIndex + 1
                // for Case C and D, skip insertion if delete delimiter which is last.
                // ex. `4242-` or `4242-4` but not `4242-42`
                //          ^           ^               ^
                val deletionLastDelimiterOrNext = latestInsertionSize == 0 &&
                    latestChangeStart in nextIndex..nextIndex + 1 &&
                    latestChangeStart in original.length..original.length + 1
                if (i < digits.size - 1 &&
                    nextIndex in delimiterPositions &&
                    !deletionLastDelimiterOrNext
                ) {
                    formatted.append(delimiter)
                }
            }
        }
        // for Case C
        val nextIndex = formatted.lastIndex + 1
        if (nextIndex in delimiterPositions &&
            latestChangeStart == nextIndex &&
            latestInsertionSize == 0
        ) {
            formatted.delete(formatted.length - 1, formatted.length)
        }

        return formatted.toString()
    }

    private fun getDelimiterPositions() = when (brand) {
        CardBrand.AMEX, CardBrand.DINERS_CLUB -> delimiterPositionsAmexDiners
        else -> delimiterPositionsCommon
    }

    private fun getMaxDigits() = brand.numberLength

    private fun createDigitArray(s: Editable): CharArray {
        val max = getMaxDigits()
        val digits = CharArray(max)
        var index = 0
        var i = 0
        while (i < s.length && index < max) {
            val current = s[i]
            if (Character.isDigit(current)) {
                digits[index] = current
                index++
            }
            i++
        }
        return digits
    }

    private fun getAdjustedCursorPosition(original: String, formatted: String, cursor: Int): Int {
        if (cursor == original.length) {
            return formatted.length
        }
        val delimitersOriginal = original.substring(0, cursor).count { it == delimiter }
        val delimitersCorrect = getDelimiterPositions().count { it < cursor }
        if (delimitersCorrect > delimitersOriginal) {
            return cursor + (delimitersCorrect - delimitersOriginal)
        }
        return cursor
    }
}