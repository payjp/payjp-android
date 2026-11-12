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

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.autofill.AutofillValue
import com.google.android.material.textfield.TextInputEditText

/**
 * EditText for CVC.
 *
 * Since the CVC field is a password field, [android.text.method.PasswordTransformationMethod] attaches
 * a span that references the original [android.text.Editable] while showing the last typed character.
 * [AutofillValue.forText] keeps that span (it removes only `NoCopySpan`),
 * so the [android.app.assist.AssistStructure] retained by the autofill framework leaks this view
 * and its Activity through the span.
 * Return the value without any spans to avoid it.
 */
internal class CardCvcEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TextInputEditText(context, attrs) {

    @TargetApi(Build.VERSION_CODES.O)
    override fun getAutofillValue(): AutofillValue? {
        val value = super.getAutofillValue() ?: return null
        return if (value.isText) {
            AutofillValue.forText(value.textValue.toString())
        } else {
            value
        }
    }
}
