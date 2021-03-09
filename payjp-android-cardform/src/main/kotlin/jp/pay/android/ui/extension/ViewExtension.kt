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
@file:JvmName("ViewExtension")

package jp.pay.android.ui.extension

import android.app.Dialog
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.android.material.textfield.TextInputLayout
import jp.pay.android.ui.widget.DialogHolder

internal typealias TextViewOnTextChanged = ((s: CharSequence, start: Int, before: Int, count: Int) -> Unit)

internal fun TextInputLayout.setErrorOrNull(error: CharSequence?) {
    this.isErrorEnabled = error.isNullOrEmpty().not()
    this.error = error
}

internal fun TextView.addOnTextChanged(
    onTextChanged: TextViewOnTextChanged?
) {
    addTextChangedListener(
        object : TextWatcher {
            override fun afterTextChanged(s: Editable) = Unit

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) =
                onTextChanged?.invoke(s, start, before, count) ?: Unit
        }
    )
}

/**
 * Show with lifecycle awareness of activity using [jp.pay.android.ui.widget.DialogHolder].
 *
 * @param activity host activity
 */
internal fun Dialog.showWith(activity: ComponentActivity) {
    DialogHolder(this).show(activity)
}
