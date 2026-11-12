/*
 *
 * Copyright (c) 2026 PAY, Inc.
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

import android.os.Build
import android.text.InputType
import android.text.Spanned
import android.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.VANILLA_ICE_CREAM])
class CardCvcEditTextTest {

    private lateinit var editText: CardCvcEditText

    @Before
    fun setUp() {
        val context = ContextThemeWrapper(
            ApplicationProvider.getApplicationContext(),
            com.google.android.material.R.style.Theme_MaterialComponents_Light
        )
        editText = CardCvcEditText(context)
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
    }

    @Test
    fun autofill_value_does_not_contain_spans() {
        editText.setText("123")
        // PasswordTransformationMethod$Visible のように元の Editable を参照するスパンを想定
        editText.text!!.setSpan(Any(), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val value = editText.autofillValue!!

        assertThat(value.isText, `is`(true))
        assertThat(value.textValue.toString(), `is`("123"))
        assertThat(value.textValue is Spanned, `is`(false))
    }
}
