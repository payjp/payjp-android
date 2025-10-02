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
import jp.pay.android.databinding.PayjpCardFormElementEmailPhoneLayoutBinding
import jp.pay.android.databinding.PayjpCardFormElementExpirationLayoutBinding
import jp.pay.android.databinding.PayjpCardFormElementHolderNameLayoutBinding
import jp.pay.android.databinding.PayjpCardFormElementNumberLayoutBinding
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardComponentInput
import jp.pay.android.model.CountryCode
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormCvcElement
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormExpirationElement
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormHolderNameElement
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormNumberElement

internal class CardFormElementAdapter(
    private val inputTypes: List<CardFormInputType>,
    private val cardNumberFormatter: CardNumberFormatTextWatcher,
    private val cardExpirationFormatter: TextWatcher,
    private val onElementTextChanged: OnCardFormElementTextChanged,
    private val onElementEditorAction: OnCardFormElementEditorAction,
    private val onElementFocusChanged: OnCardFormElementFocusChanged,
    private val onElementKeyDownDeleteWithEmpty: OnCardFormElementKeyDownDeleteWithEmpty,
    private val onCardNumberInputChanged: (s: CharSequence) -> Unit,
    autofillManager: AutofillManager?,
    private val onClickCountryCode: View.OnClickListener?,
    var countryCode: CountryCode,
    private val lastInputType: CardFormInputType,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var cardNumberInput: CardComponentInput.CardNumberInput? = null
    var cardExpirationInput: CardComponentInput.CardExpirationInput? = null
    var cardHolderNameInput: CardComponentInput.CardHolderNameInput? = null
    var cardCvcInput: CardComponentInput.CardCvcInput? = null
    var cardEmailInput: CardComponentInput.CardEmailInput? = null
    var cardPhoneNumberInput: CardComponentInput.CardPhoneNumberInput? = null
    var brand: CardBrand = CardBrand.UNKNOWN
    var showErrorImmediately: Boolean = false
    private val elementTypes: List<CardFormElementType> = inputTypes.map { it.elementType() }.distinct()
    private val itemSize = elementTypes.size
    private val autofillIds: Map<CardFormInputType, Any>

    init {
        setHasStableIds(true)
        autofillIds =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && autofillManager != null) {
                CardFormInputType.entries.associateWith { autofillManager.nextAutofillId as Any }
            } else emptyMap()
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
    ): RecyclerView.ViewHolder {
        val elementType = CardFormElementType.entries[viewType]
        val inflater = LayoutInflater.from(parent.context)
        return when (elementType) {
            CardFormElementType.Number -> CardFormNumberElement(
                PayjpCardFormElementNumberLayoutBinding.inflate(inflater, parent, false),
                cardNumberFormatter,
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onCardNumberInputChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillIds[CardFormInputType.Number],
                lastInputType == CardFormInputType.Number,
            )
            CardFormElementType.Expiration -> CardFormExpirationElement(
                PayjpCardFormElementExpirationLayoutBinding.inflate(inflater, parent, false),
                cardExpirationFormatter,
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillIds[CardFormInputType.Expiration],
                lastInputType == CardFormInputType.Expiration,
            )
            CardFormElementType.Cvc -> CardFormCvcElement(
                PayjpCardFormElementCvcLayoutBinding.inflate(inflater, parent, false),
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillIds[CardFormInputType.Cvc],
                lastInputType == CardFormInputType.Cvc,
            )
            CardFormElementType.HolderName -> CardFormHolderNameElement(
                PayjpCardFormElementHolderNameLayoutBinding.inflate(inflater, parent, false),
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillIds[CardFormInputType.HolderName],
                lastInputType == CardFormInputType.HolderName,
            )
            CardFormElementType.EmailAndPhoneNumber -> CardFormEmailPhoneElementViewHolder(
                PayjpCardFormElementEmailPhoneLayoutBinding.inflate(inflater, parent, false),
                inputTypes.contains(CardFormInputType.Email),
                inputTypes.contains(CardFormInputType.PhoneNumber),
                onClickCountryCode,
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillIds[CardFormInputType.Email],
                autofillIds[CardFormInputType.PhoneNumber],
                lastInputType,
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
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
            is CardFormEmailPhoneElementViewHolder -> holder.bindData(
                countryCode,
                cardEmailInput,
                cardPhoneNumberInput,
                showErrorImmediately
            )
        }
    }

    override fun getItemViewType(position: Int): Int = elementTypes[position].ordinal

    override fun getItemCount(): Int = itemSize

    override fun getItemId(position: Int): Long = position.toLong()

    @IdRes
    fun findEditTextId(element: CardFormElementType): Int = when (element) {
        CardFormElementType.Number -> R.id.input_edit_number
        CardFormElementType.Expiration -> R.id.input_edit_expiration
        CardFormElementType.Cvc -> R.id.input_edit_cvc
        CardFormElementType.HolderName -> R.id.input_edit_holder_name
        CardFormElementType.EmailAndPhoneNumber -> when {
            inputTypes.contains(CardFormInputType.Email) -> R.id.input_edit_email
            else -> R.id.input_edit_phone_number
        }
    }
}
