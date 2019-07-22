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

import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

/**
 * @see [CardExpirationProcessor.processExpirationMonthYear]
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class CardExpirationProcessorMonthYearTest(
    private val input: String,
    private val monthYear: Pair<String, String>?
) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf("", null),
                arrayOf("aa", null),
                arrayOf("aa/bb", null),
                arrayOf("1", null),
                arrayOf("12", null),
                arrayOf("1225", null),
                arrayOf("/", null),
                arrayOf("/2", null),
                arrayOf("/25", null),
                arrayOf("1/", null),
                arrayOf("1/20", null),
                arrayOf("12/", null),
                arrayOf("12/3", null),
                arrayOf("12-25", null),
                arrayOf("012/25", null),
                arrayOf("12/2099", null),
                arrayOf("12/22", ("12" to "22")),
                arrayOf("02/22", ("02" to "22")),
                arrayOf("02/01", ("02" to "01"))
            )
        }
    }

    @Test
    fun checkExpirationFormat() {
        assertThat(
            "input = $input",
            CardExpirationProcessor.processExpirationMonthYear(input, '/'),
            `is`(monthYear)
        )
    }
}