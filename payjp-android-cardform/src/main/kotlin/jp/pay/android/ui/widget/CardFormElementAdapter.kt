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

import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardComponentInput
import jp.pay.android.plugin.CardScannerPlugin
import jp.pay.android.ui.extension.TextViewOnTextChanged
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormCvcElement
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormExpirationElement
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormHolderNameElement
import jp.pay.android.ui.widget.CardFormElementViewHolder.CardFormNumberElement

internal class CardFormElementAdapter(
    private val cardNumberFormatter: CardNumberFormatTextWatcher,
    private val cardExpirationFormatter: TextWatcher,
    private val scannerPlugin: CardScannerPlugin?,
    private val onClickScannerIcon: View.OnClickListener?,
    private val onCvcEditorActionListener: TextView.OnEditorActionListener,
    private val onTextChangedNumber: TextViewOnTextChanged? = null,
    private val onTextChangedExpiration: TextViewOnTextChanged? = null,
    private val onTextChangedHolderName: TextViewOnTextChanged? = null,
    private val onTextChangedCvc: TextViewOnTextChanged? = null
) : RecyclerView.Adapter<CardFormElementViewHolder>() {

    companion object {
        const val ITEM_NUMBER_ELEMENT = 0
        const val ITEM_EXPIRATION_ELEMENT = 1
        const val ITEM_HOLDER_NAME_ELEMENT = 2
        const val ITEM_CVC_ELEMENT = 3
    }

    var cardNumberInput: CardComponentInput.CardNumberInput? = null
    var cardExpirationInput: CardComponentInput.CardExpirationInput? = null
    var cardHolderNameInput: CardComponentInput.CardHolderNameInput? = null
    var cardCvcInput: CardComponentInput.CardCvcInput? = null
    var brand: CardBrand = CardBrand.UNKNOWN
    var showErrorImmediately: Boolean = false

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CardFormElementViewHolder {
        return when (viewType) {
            ITEM_NUMBER_ELEMENT -> CardFormNumberElement(
                parent,
                onTextChangedNumber,
                cardNumberFormatter,
                scannerPlugin,
                onClickScannerIcon
            )
            ITEM_EXPIRATION_ELEMENT -> CardFormExpirationElement(
                parent,
                onTextChangedExpiration,
                cardExpirationFormatter
            )
            ITEM_HOLDER_NAME_ELEMENT -> CardFormHolderNameElement(parent, onTextChangedHolderName)
            ITEM_CVC_ELEMENT -> CardFormCvcElement(
                parent,
                onTextChangedCvc,
                onCvcEditorActionListener
            )
            else -> throw IllegalArgumentException("Unexpected viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: CardFormElementViewHolder, position: Int) {
        when (holder) {
            is CardFormNumberElement -> holder.bindData(cardNumberInput, showErrorImmediately)
            is CardFormExpirationElement -> holder.bindData(cardExpirationInput, showErrorImmediately)
            is CardFormHolderNameElement -> holder.bindData(cardHolderNameInput, showErrorImmediately)
            is CardFormCvcElement -> holder.bindData(cardCvcInput, brand, showErrorImmediately)
        }
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> ITEM_NUMBER_ELEMENT
        1 -> ITEM_EXPIRATION_ELEMENT
        2 -> ITEM_HOLDER_NAME_ELEMENT
        3 -> ITEM_CVC_ELEMENT
        else -> throw IllegalArgumentException("Unexpected position $position")
    }

    override fun getItemCount(): Int = 4

    override fun getItemId(position: Int): Long = position.toLong()
}
