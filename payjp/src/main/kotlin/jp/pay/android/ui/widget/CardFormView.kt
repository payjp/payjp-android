/*
 *
 * Copyright (c) 2019 PAY, Inc.
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

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.VisibleForTesting
import androidx.transition.TransitionManager
import com.google.android.material.textfield.TextInputLayout
import jp.pay.android.PayjpToken
import jp.pay.android.PayjpTokenService
import jp.pay.android.R
import jp.pay.android.Task
import jp.pay.android.exception.PayjpInvalidCardFormException
import jp.pay.android.model.CardCvcInput
import jp.pay.android.model.CardExpirationInput
import jp.pay.android.model.CardHolderNameInput
import jp.pay.android.model.CardNumberInput
import jp.pay.android.model.Token
import jp.pay.android.ui.widget.CardComponentInputView.OnChangeInputListener
import jp.pay.android.util.Tasks

/**
 * CardForm Widget
 */
class CardFormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), TokenCreatableView {

    // view
    private val numberLayout: TextInputLayout
    @VisibleForTesting
    internal val numberEditText: CardNumberEditText
    @VisibleForTesting
    internal val expirationEditText: CardExpirationEditText
    @VisibleForTesting
    internal val cvcEditText: CardCvcEditText
    private val holderNameLayout: TextInputLayout
    @VisibleForTesting
    internal val holderNameEditText: CardHolderNameEditText
    // listener
    private var onValidateInputListener: TokenCreatableView.OnValidateInputListener? = null
    // service
    @VisibleForTesting
    internal var tokenService: PayjpTokenService
    // input value
    private var cardNumberInput: CardNumberInput? = null
        set(value) {
            field = value
            onUpdateInput()
        }
    private var cardExpirationInput: CardExpirationInput? = null
        set(value) {
            field = value
            onUpdateInput()
        }
    private var cardCvcInput: CardCvcInput? = null
        set(value) {
            field = value
            onUpdateInput()
        }
    private var cardHolderNameInput: CardHolderNameInput? = null
        set(value) {
            field = value
            onUpdateInput()
        }
    private var cardHolderNameEnabled: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                holderNameLayout.visibility = if (value) { View.VISIBLE } else { View.GONE }
                TransitionManager.beginDelayedTransition(this)
                onUpdateInput()
            }
        }

    init {
        orientation = VERTICAL
        View.inflate(context, R.layout.card_form_view, this)
        numberLayout = findViewById(R.id.input_layout_number)
        numberEditText = findViewById(R.id.input_edit_number)
        expirationEditText = findViewById(R.id.input_edit_expiration)
        cvcEditText = findViewById(R.id.input_edit_cvc)
        holderNameLayout = findViewById(R.id.input_layout_holder_name)
        holderNameEditText = findViewById(R.id.input_edit_holder_name)
        watchInputUpdate()
        // request
        tokenService = PayjpToken.getInstance()
    }

    override fun isValid(): Boolean {
        return (cardNumberInput?.valid ?: false) &&
            (cardExpirationInput?.valid ?: false) &&
            (cardCvcInput?.valid ?: false) &&
            (!cardHolderNameEnabled || cardHolderNameInput?.valid ?: false)
    }

    override fun validateCardForm(): Boolean {
        forceValidate()
        updateErrorUI()
        return isValid
    }

    override fun setOnValidateInputListener(listener: TokenCreatableView.OnValidateInputListener?) {
        this.onValidateInputListener = listener
        onValidateInputListener?.onValidateInput(this, isValid)
    }

    override fun setCardHolderNameInputEnabled(enabled: Boolean) {
        cardHolderNameEnabled = enabled
    }

    override fun createToken(): Task<Token> {
        return if (isValid) {
            // TODO いい感じにする
            tokenService.createToken(
                number = cardNumberInput!!.value!!,
                expMonth = cardExpirationInput!!.value!!.month,
                expYear = cardExpirationInput!!.value!!.year,
                cvc = cardCvcInput!!.value!!,
                name = holderNameEditText.text.toString()
            )
        } else {
            Tasks.failure(
                PayjpInvalidCardFormException("Card form is not valid")
            )
        }
    }

    private fun updateErrorUI() {
        // TODO show error
    }

    private fun onUpdateInput() {
        val valid = isValid
        updateErrorUI()
        onValidateInputListener?.onValidateInput(this, valid)
    }

    private fun forceValidate() {
        numberEditText.validate()
        expirationEditText.validate()
        cvcEditText.validate()
        holderNameEditText.validate()
    }

    private fun watchInputUpdate() {
        numberEditText.onChangeInputListener = object : OnChangeInputListener<CardNumberInput> {
            override fun onChangeInput(input: CardNumberInput) {
                // TODO validation
                // TODO ブランドロゴの表示
                numberLayout.helperText = "brand = ${input.brand}"
                cardNumberInput = input
            }
        }
        expirationEditText.onChangeInputListener = object : OnChangeInputListener<CardExpirationInput> {
            override fun onChangeInput(input: CardExpirationInput) {
                cardExpirationInput = input
            }
        }
        cvcEditText.onChangeInputListener = object : OnChangeInputListener<CardCvcInput> {
            override fun onChangeInput(input: CardCvcInput) {
                cardCvcInput = input
            }
        }
        holderNameEditText.onChangeInputListener = object : OnChangeInputListener<CardHolderNameInput> {
            override fun onChangeInput(input: CardHolderNameInput) {
                cardHolderNameInput = input
            }
        }
    }
}