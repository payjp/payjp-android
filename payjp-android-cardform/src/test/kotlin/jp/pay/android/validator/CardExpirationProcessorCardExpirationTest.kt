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

import java.util.Calendar
import jp.pay.android.model.CardExpiration
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

/**
 * @see [CardExpirationProcessor.processCardExpiration]
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class CardExpirationProcessorCardExpirationTest(
    private val currentYear: Int,
    private val currentMonth: Int,
    private val monthYear: Pair<String, String>,
    private val output: CardExpiration?
) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf(2020, 1, "0" to "20", null),
                arrayOf(2020, 1, "13" to "20", null),
                arrayOf(2020, 1, "12" to "100", null),
                arrayOf(2020, 1, "12" to "0", null),
                arrayOf(2020, 1, "12" to "21", CardExpiration("12", "2021")),
                arrayOf(2020, 1, "04" to "21", CardExpiration("04", "2021")),
                arrayOf(2022, 1, "04" to "21", null),
                arrayOf(2021, 3, "04" to "21", CardExpiration("04", "2021")),
                arrayOf(2021, 4, "04" to "21", CardExpiration("04", "2021")),
                arrayOf(2021, 5, "04" to "21", null)
            )
        }
    }

    @Test
    fun processCardExpiration() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, currentYear)
        // prevent setting next month (e.g. 4/31 -> 5/1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        // calendar month is zero-based (0, 1, 2, .. 11)
        calendar.set(Calendar.MONTH, currentMonth - 1)
        assertThat(
            "current = $currentMonth/$currentYear, monthYear = $monthYear",
            CardExpirationProcessor.processCardExpiration(monthYear, calendar),
            `is`(output)
        )
    }
}
