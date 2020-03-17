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

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import jp.pay.android.R
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardComponentInput
import jp.pay.android.plugin.CardScannerPlugin
import jp.pay.android.ui.extension.TextViewOnTextChanged
import jp.pay.android.ui.extension.cvcIconResourceId
import jp.pay.android.ui.extension.logoResourceId
import jp.pay.android.ui.extension.setErrorOrNull

internal sealed class CardFormElementViewHolder(
    parent: ViewGroup,
    layoutId: Int,
    inputLayoutId: Int,
    inputEditTextId: Int,
    private val textViewOnTextChanged: TextViewOnTextChanged?
) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false)) {

    private val inputTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {}

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            textViewOnTextChanged?.invoke(s, start, before, count)
        }
    }
    protected val inputLayout: TextInputLayout = itemView.findViewById(inputLayoutId)
    protected val editText: TextInputEditText = itemView.findViewById(inputEditTextId)

    init {
        editText.addTextChangedListener(inputTextWatcher)
    }

    protected fun setTextDisablingInputWatcher(input: CardComponentInput<*>?) {
        if (input?.input != editText.text.toString()) {
            editText.removeTextChangedListener(inputTextWatcher)
            editText.setText(input?.input)
            editText.addTextChangedListener(inputTextWatcher)
        }
    }

    protected fun setInputError(input: CardComponentInput<*>?, showErrorImmediately: Boolean) {
        inputLayout.setErrorOrNull(
            input?.errorMessage?.take(showErrorImmediately.not())?.let {
                itemView.resources.getString(it)
            }
        )
    }

    class CardFormNumberElement(
        parent: ViewGroup,
        textViewOnTextChanged: TextViewOnTextChanged?,
        private val cardNumberFormatter: CardNumberFormatTextWatcher,
        scannerPlugin: CardScannerPlugin?,
        onClickScannerIcon: View.OnClickListener?,
        onEditorActionListener: TextView.OnEditorActionListener
    ) :
        CardFormElementViewHolder(
            parent,
            R.layout.payjp_card_form_number_layout,
            R.id.input_layout_number,
            R.id.input_edit_number,
            textViewOnTextChanged
        ) {

        private var brand: CardBrand = CardBrand.UNKNOWN
            set(value) {
                if (field != value) {
                    inputLayout.setStartIconDrawable(value.logoResourceId)
                    cardNumberFormatter.brand = value
                }
                field = value
            }

        init {
            editText.addTextChangedListener(cardNumberFormatter)
            editText.setOnEditorActionListener(onEditorActionListener)
            scannerPlugin?.run {
                inputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                onClickScannerIcon?.let {
                    inputLayout.setEndIconOnClickListener(it)
                }
            }
        }

        fun bindData(input: CardComponentInput.CardNumberInput?, showErrorImmediately: Boolean) {
            setTextDisablingInputWatcher(input)
            setInputError(input, showErrorImmediately)
            this.brand = input?.brand ?: CardBrand.UNKNOWN
        }
    }

    class CardFormExpirationElement(
        parent: ViewGroup,
        textViewOnTextChanged: TextViewOnTextChanged?,
        expirationFormatter: TextWatcher,
        onEditorActionListener: TextView.OnEditorActionListener
    ) :
        CardFormElementViewHolder(
            parent, R.layout.payjp_card_form_expiration_layout,
            R.id.input_layout_expiration,
            R.id.input_edit_expiration,
            textViewOnTextChanged
        ) {

        init {
            editText.addTextChangedListener(expirationFormatter)
            editText.setOnEditorActionListener(onEditorActionListener)
        }

        fun bindData(
            input: CardComponentInput.CardExpirationInput?,
            showErrorImmediately: Boolean
        ) {
            setTextDisablingInputWatcher(input)
            setInputError(input, showErrorImmediately)
            (editText as? CardExpirationEditText)?.expiration = input?.value
        }
    }

    class CardFormHolderNameElement(
        parent: ViewGroup,
        textViewOnTextChanged: TextViewOnTextChanged?,
        onEditorActionListener: TextView.OnEditorActionListener
    ) :
        CardFormElementViewHolder(
            parent,
            R.layout.payjp_card_form_holder_name_layout,
            R.id.input_layout_holder_name,
            R.id.input_edit_holder_name,
            textViewOnTextChanged
        ) {

        init {
            editText.setOnEditorActionListener(onEditorActionListener)
        }

        fun bindData(
            input: CardComponentInput.CardHolderNameInput?,
            showErrorImmediately: Boolean
        ) {
            setTextDisablingInputWatcher(input)
            setInputError(input, showErrorImmediately)
        }
    }

    class CardFormCvcElement(
        parent: ViewGroup,
        textViewOnTextChanged: TextViewOnTextChanged?,
        onEditorActionListener: TextView.OnEditorActionListener
    ) :
        CardFormElementViewHolder(
            parent,
            R.layout.payjp_card_form_cvc_layout,
            R.id.input_layout_cvc,
            R.id.input_edit_cvc,
            textViewOnTextChanged
        ) {
        private var brand: CardBrand = CardBrand.UNKNOWN
            set(value) {
                if (field != value) {
                    editText.filters =
                        arrayOf<InputFilter>(InputFilter.LengthFilter(value.cvcLength))
                    inputLayout.setEndIconDrawable(value.cvcIconResourceId)
                }
                field = value
            }

        init {
            editText.setOnEditorActionListener(onEditorActionListener)
        }

        fun bindData(
            input: CardComponentInput.CardCvcInput?,
            brand: CardBrand,
            showErrorImmediately: Boolean
        ) {
            setTextDisablingInputWatcher(input)
            setInputError(input, showErrorImmediately)
            this.brand = brand
        }
    }
}
