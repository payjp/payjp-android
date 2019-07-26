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
package jp.pay.android.model

import jp.pay.android.R
import jp.pay.android.validator.CardExpirationProcessor
import jp.pay.android.validator.CardExpirationProcessorService

/**
 * Card expiration input
 *
 * @param input input string e.g. `01/20`
 * @param delimiter e.g. `/` in `01/20`
 * @param processor process card expiration value from input.
 * @see [CardExpirationProcessorService]
 */
internal data class CardExpirationInput(
    val input: String?,
    val delimiter: Char,
    val processor: CardExpirationProcessorService = CardExpirationProcessor
) : CardComponentInput<CardExpiration> {

    override val value: CardExpiration?
    override val errorMessage: FormInputError?

    init {
        val (value, error) = when (input) {
            // empty
            null, "" -> null to FormInputError(R.string.payjp_card_form_error_no_expiration, true)
            else -> when (val monthYear = input.let { processor.processExpirationMonthYear(it, delimiter) }) {
                // no formatted value
                null -> null to FormInputError(R.string.payjp_card_form_error_invalid_expiration, true)
                else -> {
                    val (month, year) = monthYear
                    val invalidExpirationMessage = R.string.payjp_card_form_error_invalid_expiration
                    when {
                        // invalid month
                        !processor.validateMonth(month) -> null to FormInputError(invalidExpirationMessage, false)
                        // no year
                        year == null -> null to FormInputError(invalidExpirationMessage, true)
                        else -> when (val value = processor.processCardExpiration(month to year)) {
                            // invalid date
                            null -> null to FormInputError(invalidExpirationMessage, false)
                            // valid
                            else -> value to null
                        }
                    }
                }
            }
        }
        this.value = value
        this.errorMessage = error
    }
}