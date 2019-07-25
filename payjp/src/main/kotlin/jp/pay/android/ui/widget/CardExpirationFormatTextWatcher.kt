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

// card number max digits size
private const val TOTAL_MAX_DIGITS = 4
// digits + 3 delimiters
private const val TOTAL_MAX_SYMBOLS = 5
private const val DELIMITER_INDEX = 2

internal class CardExpirationFormatTextWatcher(private val delimiter: Char) : TextWatcher {
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

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable) {
        if (ignoreChanges) {
            return
        }
        if (!isInputCorrect(s)) {
            ignoreChanges = true
            s.replace(0, s.length, buildCorrectString(createDigitArray(s)))
            ignoreChanges = false
        }
    }

    private fun isInputCorrect(s: Editable): Boolean = when {
        // too long
        s.length > TOTAL_MAX_SYMBOLS -> false
        // When we add `1` from `1`, input should be `11/`.
        s.length == 2 && latestInsertionSize > 0 -> false
        // When we delete 1 character from `11/`, input should be `1`.
        s.length == 2 && latestChangeStart == 2 && latestInsertionSize == 0 -> false
        // When we delete 1 character from `12/1`, input should be `12`.
        s.length == 3 && latestChangeStart == 3 && latestInsertionSize == 0 -> false
        else -> (0 until s.length).all { i ->
            val c = s[i]
            when (i) {
                DELIMITER_INDEX -> delimiter == c
                // index 0 should be 0 or 1 (month in 01 ~ 12).
                0 -> c == '0' || c == '1'
                else -> Character.isDigit(c)
            }
        }
    }

    private fun buildCorrectString(digits: CharArray): String {
        val formatted = StringBuilder()

        for (i in digits.indices) {
            val c = digits[i]
            var index = i
            if (i == 0 && c != '0' && c != '1') {
                formatted.append('0')
                index++
            }
            if (Character.isDigit(c)) {
                formatted.append(c)
                if (index < digits.size - 1 && index + 1 == DELIMITER_INDEX && latestInsertionSize > 0) {
                    formatted.append(delimiter)
                }
            }
        }
        if (formatted.length == 2 && latestChangeStart == 2 && latestInsertionSize == 0) {
            formatted.delete(formatted.length - 1, formatted.length)
        }

        return formatted.toString()
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