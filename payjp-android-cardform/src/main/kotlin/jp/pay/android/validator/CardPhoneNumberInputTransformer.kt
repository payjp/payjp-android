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

import android.content.Context
import jp.pay.android.R
import jp.pay.android.data.PhoneNumberService
import jp.pay.android.model.CardComponentInput
import jp.pay.android.model.CountryCode
import jp.pay.android.model.FormInputError

internal class CardPhoneNumberInputTransformer(
    private val context: Context,
    private val service: PhoneNumberService
) : CardPhoneNumberInputTransformerService {

    override var currentCountryCode: CountryCode? = null

    override fun transform(input: String?): CardComponentInput.CardPhoneNumberInput {
        val trimmed = input?.trim()
        val countryCode = currentCountryCode ?: service.defaultCountryCode()
        val normalized = trimmed?.takeIf { it.isNotEmpty() }?.let { service.normalize(context, it, countryCode) }

        val errorMessage = when {
            trimmed.isNullOrEmpty() -> FormInputError(R.string.payjp_card_form_error_no_phone_number, true)
            normalized == null -> {
                //
                val maybeTooLong = trimmed.length > service.examplePhoneNumber(context, countryCode).length
                FormInputError(R.string.payjp_card_form_error_invalid_phone_number, !maybeTooLong)
            }
            else -> null
        }
        return CardComponentInput.CardPhoneNumberInput(input, normalized, errorMessage)
    }

    override fun injectPreset(region: String, number: String?): CardComponentInput.CardPhoneNumberInput {
        currentCountryCode = service.findCountryCodeByRegion(context, region) ?: service.defaultCountryCode()
        return transform(number)
    }
}
