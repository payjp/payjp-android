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

import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.autofill.AutofillId
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import jp.pay.android.databinding.PayjpCardFormElementEmailPhoneLayoutBinding
import jp.pay.android.model.CardComponentInput
import jp.pay.android.model.CountryCode
import jp.pay.android.ui.extension.setErrorOrNull

@Suppress("LongParameterList")
internal class CardFormEmailPhoneElementViewHolder(
    private val binding: PayjpCardFormElementEmailPhoneLayoutBinding,
    emailEnabled: Boolean,
    phoneNumberEnabled: Boolean,
    onClickCountryCode: View.OnClickListener?,
    onTextChanged: OnCardFormElementTextChanged,
    onEditorAction: OnCardFormElementEditorAction,
    onFocusChanged: OnCardFormElementFocusChanged,
    onKeyDownDeleteWithEmpty: OnCardFormElementKeyDownDeleteWithEmpty,
    autoFillIdForEmail: Any?,
    autoFillIdForPhone: Any?,
    lastInputType: CardFormInputType,
) : RecyclerView.ViewHolder(binding.root) {
    private val inputTextWatcherEmail = object : TextWatcher {
        override fun afterTextChanged(s: Editable) = Unit

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            onTextChanged.invoke(CardFormInputType.Email, s, start, before, count)
        }
    }
    private val inputTextWatcherPhoneNumber = object : TextWatcher {
        override fun afterTextChanged(s: Editable) = Unit

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            onTextChanged.invoke(CardFormInputType.PhoneNumber, s, start, before, count)
        }
    }

    init {
        binding.contentEmailFrame.visibility = if (emailEnabled) View.VISIBLE else View.GONE
        binding.contentPhone.root.visibility = if (phoneNumberEnabled) View.VISIBLE else View.GONE
        // email
        binding.contentEmail.inputEditEmail.apply {
            addTextChangedListener(inputTextWatcherEmail)
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                onFocusChanged.invoke(CardFormElementType.EmailAndPhoneNumber, v, hasFocus)
            }
            setOnEditorActionListener { v, actionId, event ->
                onEditorAction.invoke(CardFormInputType.Email, v, actionId, event)
            }
            setOnKeyDown(this, CardFormInputType.Email, onKeyDownDeleteWithEmpty)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                (autoFillIdForEmail as? AutofillId)?.let { autofillId = it }
            }
            imeOptions = when (lastInputType) {
                CardFormInputType.Email -> EditorInfo.IME_ACTION_DONE
                else -> EditorInfo.IME_ACTION_NEXT
            }
        }
        // phone number
        binding.contentPhone.inputEditPhoneNumber.apply {
            addTextChangedListener(inputTextWatcherPhoneNumber)
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                onFocusChanged.invoke(CardFormElementType.EmailAndPhoneNumber, v, hasFocus)
            }
            setOnKeyDown(this, CardFormInputType.PhoneNumber, onKeyDownDeleteWithEmpty)
            setOnEditorActionListener { v, actionId, event ->
                onEditorAction.invoke(CardFormInputType.PhoneNumber, v, actionId, event)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                (autoFillIdForPhone as? AutofillId)?.let { autofillId = it }
            }
            imeOptions = when (lastInputType) {
                CardFormInputType.PhoneNumber -> EditorInfo.IME_ACTION_DONE
                else -> EditorInfo.IME_ACTION_NEXT
            }
        }
        binding.contentPhone.inputLayoutCountryCode.setEndIconOnClickListener(onClickCountryCode)
        binding.contentPhone.inputEditCountryCode.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            onFocusChanged.invoke(CardFormElementType.EmailAndPhoneNumber, v, hasFocus)
        }
    }

    fun bindData(
        countryCode: CountryCode,
        emailInput: CardComponentInput.CardEmailInput?,
        phoneNumberInput: CardComponentInput.CardPhoneNumberInput?,
        showErrorImmediately: Boolean
    ) {
        // email
        setTextDisablingInputWatcher(binding.contentEmail.inputEditEmail, emailInput, inputTextWatcherEmail)
        setInputError(binding.contentEmail.inputLayoutEmail, emailInput, showErrorImmediately)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.contentEmail.inputEditEmail.setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS)
        }
        // phone number
        binding.contentPhone.inputEditCountryCode.setText(countryCode.shortName)
        setTextDisablingInputWatcher(
            binding.contentPhone.inputEditPhoneNumber, phoneNumberInput, inputTextWatcherPhoneNumber
        )
        setInputError(binding.contentPhone.inputLayoutPhoneNumber, phoneNumberInput, showErrorImmediately)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.contentPhone.inputEditPhoneNumber.setAutofillHints(View.AUTOFILL_HINT_PHONE)
        }
    }

    private fun setOnKeyDown(
        editText: EditText,
        type: CardFormInputType,
        onKeyDownDeleteWithEmpty: OnCardFormElementKeyDownDeleteWithEmpty
    ) {
        editText.setOnKeyListener { v, keyCode, event ->
            takeIf {
                editText.text.isNullOrEmpty() &&
                    keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN
            }?.run {
                onKeyDownDeleteWithEmpty(type, v)
            } ?: false
        }
    }

    private fun setTextDisablingInputWatcher(
        editText: EditText,
        input: CardComponentInput<*>?,
        inputTextWatcher: TextWatcher
    ) {
        if (input?.input != editText.text.toString()) {
            editText.removeTextChangedListener(inputTextWatcher)
            editText.setText(input?.input)
            editText.addTextChangedListener(inputTextWatcher)
        }
    }

    private fun setInputError(
        inputLayout: TextInputLayout,
        input: CardComponentInput<*>?,
        showErrorImmediately: Boolean
    ) {
        inputLayout.setErrorOrNull(
            input?.errorMessage?.take(showErrorImmediately.not())?.let {
                itemView.resources.getString(it)
            }
        )
    }
}
