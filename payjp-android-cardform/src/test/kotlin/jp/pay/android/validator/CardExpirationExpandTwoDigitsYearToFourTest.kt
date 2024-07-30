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

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import java.util.Calendar

/**
 * @see [CardExpirationProcessor.expandTwoDigitsYearToFour]
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class CardExpirationExpandTwoDigitsYearToFourTest(
    private val currentYear: Int,
    private val inputYear: Int,
    private val outputYear: Int
) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf(2019, 80, 2080),
                arrayOf(2019, 81, 1981),
                arrayOf(2020, 81, 2081),
                arrayOf(2080, 19, 2019),
                arrayOf(2081, 19, 2119),
                arrayOf(2081, 20, 2020)
            )
        }
    }

    @Test
    fun expandTwoDigitsYearToFour() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, currentYear)
        assertThat(
            "current = $currentYear, input = $inputYear",
            CardExpirationProcessor.expandTwoDigitsYearToFour(inputYear, calendar),
            `is`(outputYear)
        )
    }
}
