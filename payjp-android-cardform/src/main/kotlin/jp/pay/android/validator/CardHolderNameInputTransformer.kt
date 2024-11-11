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
import jp.pay.android.model.CardComponentInput
import jp.pay.android.model.FormInputError

internal object CardHolderNameInputTransformer :
    CardInputTransformer<CardComponentInput.CardHolderNameInput> {
    override fun transform(input: String?): CardComponentInput.CardHolderNameInput {
        val trimmed = input?.trim()
        val errorMessage = when {
            trimmed.isNullOrEmpty() -> FormInputError(
                R.string.payjp_card_form_error_no_holder_name,
                true
            )
            (MIN_LENGTH..MAX_LENGTH).contains(trimmed.length).not() -> FormInputError(
                R.string.payjp_card_form_error_invalid_length_holder_name,
                false
            )
            !trimmed.matches(cardNameRegex) -> FormInputError(
                R.string.payjp_card_form_error_invalid_holder_name,
                false
            )
            else -> null
        }
        val value = trimmed.takeIf { errorMessage == null }
        return CardComponentInput.CardHolderNameInput(input, value, errorMessage)
    }

    private val cardNameRegex by lazy { Regex("^[a-zA-Z0-9 -.]+$") }
    private const val MAX_LENGTH = 45
    private const val MIN_LENGTH = 2
}
