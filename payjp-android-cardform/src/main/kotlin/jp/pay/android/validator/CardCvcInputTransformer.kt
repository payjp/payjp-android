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
package jp.pay.android.validator

import jp.pay.android.R
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardComponentInput
import jp.pay.android.model.FormInputError

internal class CardCvcInputTransformer : CardCvcInputTransformerService {

    override var brand: CardBrand = CardBrand.UNKNOWN

    override fun transform(input: String?): CardComponentInput.CardCvcInput {
        val digits = input?.filter(Character::isDigit)
        val errorMessage = when {
            digits.isNullOrEmpty() -> FormInputError(
                R.string.payjp_card_form_error_no_cvc,
                input.isNullOrEmpty()
            )
            digits.length != brand.cvcLength ->
                FormInputError(
                    R.string.payjp_card_form_error_invalid_cvc,
                    digits.length < brand.cvcLength
                )
            else -> null
        }
        val value = digits.takeIf { errorMessage == null }
        return CardComponentInput.CardCvcInput(input, value, errorMessage)
    }
}
