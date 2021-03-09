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
package jp.pay.android.ui.widget

import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class CardExpirationFormatTextWatcherTest(
    private val input: String?,
    private val result: String?
) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf("1", "1"),
                arrayOf("12", "12/"),
                arrayOf("5", "05/"),
                arrayOf("122", "12/2"),
                arrayOf("1220", "12/20"),
                arrayOf("12/20", "12/20"),
                arrayOf("122012", "12/20"),
                arrayOf("1abc", "1")
            )
        }
    }

    private lateinit var textWatcher: CardExpirationFormatTextWatcher
    private lateinit var editText: EditText

    @Before
    fun setUp() {
        textWatcher = CardExpirationFormatTextWatcher('/')
        editText = EditText(ApplicationProvider.getApplicationContext())
        editText.addTextChangedListener(textWatcher)
    }

    @Test
    fun formatInput() {
        editText.setText(input)
        assertThat("input = $input", editText.text.toString(), `is`(result))
    }
}
