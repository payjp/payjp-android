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

import android.os.Build
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import jp.pay.android.R
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardComponentInput
import jp.pay.android.plugin.CardScannerPlugin
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormCvcElement
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormExpirationElement
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormHolderNameElement
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormNumberElement

internal class CardFormElementAdapter(
    private val cardNumberFormatter: CardNumberFormatTextWatcher,
    private val cardExpirationFormatter: TextWatcher,
    private val scannerPlugin: CardScannerPlugin?,
    private val onClickScannerIcon: View.OnClickListener?,
    private val onElementTextChanged: OnCardFormElementTextChanged,
    private val onElementEditorAction: OnCardFormElementEditorAction,
    private val onElementFocusChanged: OnCardFormElementFocusChanged,
    private val onElementKeyDownDeleteWithEmpty: OnCardFormElementKeyDownDeleteWithEmpty,
    private val onCardNumberInputChanged: (s: CharSequence) -> Unit,
    autofillManager: AutofillManager?
) : RecyclerView.Adapter<CardFormElementViewHolder>() {

    companion object {
        @IdRes
        fun findEditTextId(element: CardFormElementType): Int = when (element) {
            CardFormElementType.Number -> R.id.input_edit_number
            CardFormElementType.Expiration -> R.id.input_edit_expiration
            CardFormElementType.Cvc -> R.id.input_edit_cvc
            CardFormElementType.HolderName -> R.id.input_edit_holder_name
        }
    }

    var cardNumberInput: CardComponentInput.CardNumberInput? = null
    var cardExpirationInput: CardComponentInput.CardExpirationInput? = null
    var cardHolderNameInput: CardComponentInput.CardHolderNameInput? = null
    var cardCvcInput: CardComponentInput.CardCvcInput? = null
    var brand: CardBrand = CardBrand.UNKNOWN
    var showErrorImmediately: Boolean = false
    private val itemSize = CardFormElementType.values().size
    private val autofillIds: List<Any>

    init {
        setHasStableIds(true)
        autofillIds =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && autofillManager != null) {
                (0 until itemSize).map { autofillManager.nextAutofillId }.toList()
            } else emptyList()
    }

    fun getPositionForElementType(cardFormElementType: CardFormElementType): Int {
        return cardFormElementType.ordinal
    }

    fun getElementTypeForPosition(position: Int): CardFormElementType {
        require(position < itemCount) { "item count is $itemCount" }
        return CardFormElementType.values()[position]
    }

    fun notifyCardFormElementChanged(cardFormElementType: CardFormElementType) {
        notifyItemChanged(cardFormElementType.ordinal)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CardFormElementViewHolder {
        val type = CardFormElementType.values()[viewType]
        val autofillId = autofillIds.getOrNull(type.ordinal)
        return when (type) {
            CardFormElementType.Number -> CardFormNumberElement(
                parent,
                cardNumberFormatter,
                scannerPlugin,
                onClickScannerIcon,
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onCardNumberInputChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillId
            )
            CardFormElementType.Expiration -> CardFormExpirationElement(
                parent,
                cardExpirationFormatter,
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillId
            )
            CardFormElementType.Cvc -> CardFormCvcElement(
                parent,
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillId
            )
            CardFormElementType.HolderName -> CardFormHolderNameElement(
                parent,
                onElementTextChanged,
                onElementEditorAction,
                onElementFocusChanged,
                onElementKeyDownDeleteWithEmpty,
                autofillId
            )
        }
    }

    override fun onBindViewHolder(holder: CardFormElementViewHolder, position: Int) {
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
        }
    }

    override fun getItemViewType(position: Int): Int = position

    override fun getItemCount(): Int = itemSize

    override fun getItemId(position: Int): Long = position.toLong()
}
