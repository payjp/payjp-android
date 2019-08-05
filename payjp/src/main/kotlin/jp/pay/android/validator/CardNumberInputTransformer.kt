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
package jp.pay.android.validator

import jp.pay.android.R
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardBrandDetector
import jp.pay.android.model.CardBrandDetectorService
import jp.pay.android.model.CardComponentInput
import jp.pay.android.model.FormInputError

internal interface CardNumberInputTransformerServise : CardInputTransformer<CardComponentInput.CardNumberInput> {
    var acceptedBrands: List<CardBrand>?
}

internal class CardNumberInputTransformer(
    private val brandDetector: CardBrandDetectorService = CardBrandDetector,
    private val cardNumberValidator: CardNumberValidatorService = CardNumberValidator
) : CardNumberInputTransformerServise {

    override var acceptedBrands: List<CardBrand>? = null

    override fun transform(input: String?): CardComponentInput.CardNumberInput {
        val brand = input?.filter(Character::isDigit)
            ?.let(brandDetector::detectWithDigits)
            ?: CardBrand.UNKNOWN
        val digits = input?.filter(Character::isDigit)
        val lengthCheck = cardNumberValidator.isCardNumberLengthValid(digits.orEmpty(), brand)
        val errorMessage = when {
            digits.isNullOrEmpty() -> FormInputError(messageId = R.string.payjp_card_form_error_no_number, lazy = true)
            lengthCheck == CardNumberValidatorService.CardNumberLengthStatus.TOO_LONG ->
                FormInputError(messageId = R.string.payjp_card_form_error_invalid_number, lazy = false)
            lengthCheck == CardNumberValidatorService.CardNumberLengthStatus.TOO_SHORT ->
                FormInputError(messageId = R.string.payjp_card_form_error_invalid_number, lazy = true)
            !cardNumberValidator.isLuhnValid(digits) ->
                FormInputError(messageId = R.string.payjp_card_form_error_invalid_number, lazy = false)
            brand == CardBrand.UNKNOWN ->
                FormInputError(messageId = R.string.payjp_card_form_error_invalid_brand, lazy = true)
            acceptedBrands?.contains(brand) == false ->
                FormInputError(messageId = R.string.payjp_card_form_error_invalid_brand, lazy = false)
            else -> null
        }
        val value = digits.takeIf { errorMessage == null }
        return CardComponentInput.CardNumberInput(input, value, errorMessage, brand)
    }
}