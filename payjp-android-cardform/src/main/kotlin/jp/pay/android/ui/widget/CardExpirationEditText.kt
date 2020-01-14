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
package jp.pay.android.ui.widget

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import android.view.autofill.AutofillValue
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import jp.pay.android.model.CardExpiration

internal class CardExpirationEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TextInputEditText(context, attrs) {

    var expiration: CardExpiration? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            importantForAutofill = IMPORTANT_FOR_AUTOFILL_YES_EXCLUDE_DESCENDANTS
        }
    }

    override fun getAutofillType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View.AUTOFILL_TYPE_DATE
        } else {
            0
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun getAutofillValue(): AutofillValue? {
        return expiration?.let { expiration ->
            val calendar = Calendar.getInstance()
            calendar.clear()
            calendar.set(Calendar.YEAR, expiration.yearValue)
            calendar.set(Calendar.MONTH, expiration.monthValueZeroBased)
            AutofillValue.forDate(calendar.timeInMillis)
        }
    }

    override fun autofill(value: AutofillValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (value.isDate) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = value.dateValue
                val formatted = DateFormat.format("MM/yy", calendar)
                setText(formatted)
            }
        }
    }
}
