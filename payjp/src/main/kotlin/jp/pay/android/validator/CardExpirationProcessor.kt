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

import androidx.annotation.IntRange
import androidx.annotation.VisibleForTesting
import java.util.Calendar
import jp.pay.android.model.CardExpiration

/**
 * Process card expiration from input.
 *
 */
internal object CardExpirationProcessor : CardExpirationProcessorService {
    override fun processExpirationMonthYear(
        input: String,
        delimiter: Char
    ): Pair<String, String?>? {
        return input
            .split(delimiter)
            .map { part -> part.filter(Character::isDigit).takeIf { it.length == 2 } }
            .takeIf { parts -> parts.size == 2 && parts[0] != null }
            ?.let { parts ->
                checkNotNull(parts[0]) to parts[1]
            }
    }

    override fun validateMonth(month: String): Boolean {
        val monthInt = month.toInt()
        return monthInt in 1..12
    }

    override fun processCardExpiration(
        monthYear: Pair<String, String>,
        calendar: Calendar
    ): CardExpiration? {
        val (monthString, yearString) = monthYear
        val month = monthString.toInt()
        val year = yearString.toInt()
        if (!validateMonth(monthString) || year !in 0..99) {
            return null
        }
        val fullYear = expandTwoDigitsYearToFour(year, calendar)
        val currentYear = calendar[Calendar.YEAR]
        val expired = when {
            fullYear < currentYear -> true
            fullYear > currentYear -> false
            else -> {
                val currentMonth = calendar[Calendar.MONTH] + 1
                month < currentMonth
            }
        }
        if (expired) {
            return null
        }
        return CardExpiration(monthString, fullYear.toString())
    }

    /**
     * Expand a two digits year to a four digits year.
     * We round-off 20-years deferment.
     * Because in the end of century, expiration may be beyond the century.
     *
     * For example:
     * - Now it is 2081, and input is `19`, guess that `2119` (not `2019`).
     * - Now it is 2079, and input is `19`, guess that `2019` (not `2119`).
     * - Now it is 2019, and input is `81`, guess that `1981` (not `2081`).
     * - Now it is 2019, and input is `80`, guess that `2080` (not `1980`).
     *
     * @param twoDigitYear two digits year e.g. `19`, `8`.
     * @param calendar calendar instance.
     * @return four digits year e.g. `2019`, `2108`.
     */
    @VisibleForTesting
    fun expandTwoDigitsYearToFour(
        @IntRange(from = 0, to = 99) twoDigitYear: Int,
        calendar: Calendar
    ): Int {
        val year = calendar.get(Calendar.YEAR)
        var century = year / 100
        if (year % 100 > 80 && twoDigitYear < 20) {
            century++
        } else if (year % 100 < 20 && twoDigitYear > 80) {
            century--
        }
        return century * 100 + twoDigitYear
    }
}
