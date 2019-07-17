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
import com.google.android.material.textfield.TextInputEditText
import jp.pay.android.PayjpToken
import jp.pay.android.PayjpTokenService
import jp.pay.android.R
import jp.pay.android.Task
import jp.pay.android.model.Token

/**
 * CardForm Widget
 */
class CardFormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), TokenCreatableView {

    private val numberEditText: TextInputEditText
    private val expirationEditText: TextInputEditText
    private val cvcEditText: TextInputEditText
    private val holderNameEditText: TextInputEditText
    private var onValidateInputListener: TokenCreatableView.OnValidateInputListener? = null
    @VisibleForTesting
    internal var tokenService: PayjpTokenService

    init {
        orientation = VERTICAL
        View.inflate(context, R.layout.card_form_view, this)
        numberEditText = findViewById(R.id.input_edit_number)
        expirationEditText = findViewById(R.id.input_edit_expiration)
        cvcEditText = findViewById(R.id.input_edit_cvc)
        holderNameEditText = findViewById(R.id.input_edit_holder_name)
        // TODO: format input
        // request
        tokenService = PayjpToken.getInstance()
    }

    override fun isValid(): Boolean {
        // TODO validation
        return true
    }

    override fun setOnValidateInputListener(listener: TokenCreatableView.OnValidateInputListener?) {
        this.onValidateInputListener = listener
    }

    override fun createToken(): Task<Token> {
        // TODO validate
        // TODO いい感じにする
        return tokenService.createToken(
            number = numberEditText.text.toString(),
            expMonth = expirationEditText.text.toString().split("/")[0],
            expYear = expirationEditText.text.toString().split("/")[1],
            cvc = cvcEditText.text.toString(),
            name = holderNameEditText.text.toString()
        )
    }
}