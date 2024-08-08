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
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import jp.pay.android.R
import jp.pay.android.databinding.PayjpCardFormElementCvcLayoutBinding
import jp.pay.android.databinding.PayjpCardFormElementEmailLayoutBinding
import jp.pay.android.databinding.PayjpCardFormElementExpirationLayoutBinding
import jp.pay.android.databinding.PayjpCardFormElementHolderNameLayoutBinding
import jp.pay.android.databinding.PayjpCardFormElementNumberLayoutBinding
import jp.pay.android.databinding.PayjpCardFormElementPhoneNumberLayoutBinding
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardComponentInput
import jp.pay.android.model.CountryCode
import jp.pay.android.plugin.CardScannerPlugin
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormCvcElement
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormEmailElement
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormExpirationElement
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormHolderNameElement
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormNumberElement
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormPhoneNumberElement

internal class CardFormElementAdapter(
    private val inputTypes: List<CardFormElementType>,
    private val cardNumberFormatter: CardNumberFormatTextWatcher,
    private val cardExpirationFormatter: TextWatcher,
    private val scannerPlugin: CardScannerPlugin?,
    private val onClickScannerIcon: View.OnClickListener?,
    private val onElementTextChanged: OnCardFormElementTextChanged,
    private val onElementEditorAction: OnCardFormElementEditorAction,
    private val onElementFocusChanged: OnCardFormElementFocusChanged,
    private val onElementKeyDownDeleteWithEmpty: OnCardFormElementKeyDownDeleteWithEmpty,
    private val onCardNumberInputChanged: (s: CharSequence) -> Unit,
    autofillManager: AutofillManager?,
    private val onClickCountryCode: View.OnClickListener?,
    var countryCode: CountryCode,
    private val lastInputElementType: CardFormElementType,
) : RecyclerView.Adapter<CardFormElementViewHolder<*>>() {

    companion object {
        @IdRes
        fun findEditTextId(element: CardFormElementType): Int = when (element) {
            CardFormElementType.Number -> R.id.input_edit_number
            CardFormElementType.Expiration -> R.id.input_edit_expiration
            CardFormElementType.Cvc -> R.id.input_edit_cvc
            CardFormElementType.HolderName -> R.id.input_edit_holder_name
            CardFormElementType.Email -> R.id.input_edit_email
            CardFormElementType.PhoneNumber -> R.id.input_edit_phone_number
        }
    }

    var cardNumberInput: CardComponentInput.CardNumberInput? = null
    var cardExpirationInput: CardComponentInput.CardExpirationInput? = null
    var cardHolderNameInput: CardComponentInput.CardHolderNameInput? = null
    var cardCvcInput: CardComponentInput.CardCvcInput? = null
    var cardEmailInput: CardComponentInput.CardEmailInput? = null
    var cardPhoneNumberInput: CardComponentInput.CardPhoneNumberInput? = null
    var brand: CardBrand = CardBrand.UNKNOWN
    var showErrorImmediately: Boolean = false
    private val itemSize = inputTypes.size
    private val autofillIds: List<Any>

    init {
        setHasStableIds(true)
        autofillIds =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && autofillManager != null) {
                (0 until itemSize).map { autofillManager.nextAutofillId as Any }.toList()
            } else emptyList()
    }

    fun getPositionForElementType(cardFormElementType: CardFormElementType): Int {
        return cardFormElementType.ordinal
    }

    fun getElementTypeForPosition(position: Int): CardFormElementType {
        require(position < itemCount) { "item count is $itemCount" }
        return CardFormElementType.entries[position]
    }

    fun notifyCardFormElementChanged(cardFormElementType: CardFormElementType) {
        notifyItemChanged(cardFormElementType.ordinal)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CardFormElementViewHolder<*> {
        val type = CardFormElementType.entries[viewType]
        val autofillId = autofillIds.getOrNull(type.ordinal)
        val inflater = LayoutInflater.from(parent.context)
        val isLast = lastInputElementType == type
        return when (type) {
            CardFormElementType.Number -> CardFormNumberElement(
                PayjpCardFormElementNumberLayoutBinding.inflate(inflater, parent, false),
                cardNumberFormatter,
                scannerPlugin,
                onClickScannerIcon,
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onCardNumberInputChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillId,
                isLast,
            )
            CardFormElementType.Expiration -> CardFormExpirationElement(
                PayjpCardFormElementExpirationLayoutBinding.inflate(inflater, parent, false),
                cardExpirationFormatter,
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillId,
                isLast,
            )
            CardFormElementType.Cvc -> CardFormCvcElement(
                PayjpCardFormElementCvcLayoutBinding.inflate(inflater, parent, false),
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillId,
                isLast,
            )
            CardFormElementType.HolderName -> CardFormHolderNameElement(
                PayjpCardFormElementHolderNameLayoutBinding.inflate(inflater, parent, false),
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillId,
                isLast,
            )
            CardFormElementType.Email -> CardFormEmailElement(
                PayjpCardFormElementEmailLayoutBinding.inflate(inflater, parent, false),
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillId,
                isLast,
            )
            CardFormElementType.PhoneNumber -> CardFormPhoneNumberElement(
                PayjpCardFormElementPhoneNumberLayoutBinding.inflate(inflater, parent, false),
                onClickCountryCode,
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillId,
                isLast,
            )
        }
    }

    override fun onBindViewHolder(holder: CardFormElementViewHolder<*>, position: Int) {
        when (holder) {
            is CardFormNumberElement -> holder.bindData(
                cardNumberInput,
                showErrorImmediately
            )
            is CardFormExpirationElement -> holder.bindData(
                cardExpirationInput,
                showErrorImmediately
            )
            is CardFormCvcElement -> holder.bindData(
                cardCvcInput,
                brand,
                showErrorImmediately
            )
            is CardFormHolderNameElement -> holder.bindData(
                cardHolderNameInput,
                showErrorImmediately
            )
            is CardFormEmailElement -> holder.bindData(
                cardEmailInput,
                showErrorImmediately
            )
            is CardFormPhoneNumberElement -> holder.bindData(
                cardPhoneNumberInput,
                countryCode,
                showErrorImmediately
            )
        }
    }

    override fun getItemViewType(position: Int): Int = inputTypes[position].ordinal

    override fun getItemCount(): Int = itemSize

    override fun getItemId(position: Int): Long = position.toLong()
}
