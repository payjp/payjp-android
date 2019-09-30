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

import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import jp.pay.android.model.CardBrand
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class CardNumberFormatTextWatcherTest(
    private val input: String?,
    private val brand: CardBrand?,
    private val result: String?
) {

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf("1234123412341234", null, "1234 1234 1234 1234"),
                arrayOf("12", null, "12"),
                arrayOf("12345", null, "1234 5"),
                arrayOf("123456aaa", null, "1234 56"),
                arrayOf("aaaa", null, ""),
                arrayOf("123412341234123412", null, "1234 1234 1234 1234"),
                arrayOf("1234123412341234", CardBrand.VISA, "1234 1234 1234 1234"),
                arrayOf("1234123412341234", CardBrand.MASTER_CARD, "1234 1234 1234 1234"),
                arrayOf("1234123412341234", CardBrand.JCB, "1234 1234 1234 1234"),
                arrayOf("1234123412341234", CardBrand.DISCOVER, "1234 1234 1234 1234"),
                arrayOf("123412345612345", CardBrand.AMEX, "1234 123456 12345"),
                arrayOf("12341234561234", CardBrand.DINERS_CLUB, "1234 123456 1234"),
                arrayOf("123456", CardBrand.AMEX, "1234 56"),
                arrayOf("123456", CardBrand.DINERS_CLUB, "1234 56")
            )
        }
    }

    private lateinit var textWatcher: CardNumberFormatTextWatcher
    private lateinit var editText: EditText

    @Before
    fun setUp() {
        textWatcher = CardNumberFormatTextWatcher(' ')
        editText = EditText(ApplicationProvider.getApplicationContext())
        editText.addTextChangedListener(textWatcher)
    }

    @Test
    fun formatInput() {
        if (brand != null) {
            textWatcher.brand = brand
        }
        editText.setText(input)
        assertThat("input = $input, brand = $brand", editText.text.toString(), `is`(result))
    }
}