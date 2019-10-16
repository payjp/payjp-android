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

import jp.pay.android.model.CardExpiration
import java.util.Calendar

/**
 * Process card expiration from input.
 *
 */
internal interface CardExpirationProcessorService {

    /**
     * Create month and year pair from input string.
     * Validate input format, but do not check as date. (only format)
     *
     * @param input input string e.g. `01/25`
     * @param delimiter e.g. `/` in `01/25`
     * @return month and year `("12", "20")` if valid. null if invalid. month is nonnull, but year is nullable
     *   because after input month and before year, we check only month not only format but also semantic.
     */
    fun processExpirationMonthYear(input: String, delimiter: Char): Pair<String, String?>?

    /**
     * Validate month string
     *
     * @param month input string month.
     */
    fun validateMonth(month: String): Boolean

    /**
     * Create card expiration value object as [CardExpiration] from month and year.
     * Check month and year correctness and validity with the current time.
     *
     * @param monthYear two-digits month and year pair. e.g. ("12", "20").
     * @param calendar calendar
     * @return card expiration if valid. null if invalid.
     */
    fun processCardExpiration(
        monthYear: Pair<String, String>,
        calendar: Calendar = Calendar.getInstance()
    ): CardExpiration?
}