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

import androidx.lifecycle.LiveData
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardComponentInput
import jp.pay.android.model.CardExpiration
import jp.pay.android.model.CountryCode
import jp.pay.android.util.OneOffValue

internal interface CardFormViewModelOutput {
    val cardNumberInput: LiveData<CardComponentInput.CardNumberInput>
    val cardNumberError: LiveData<Int?>
    val cardExpirationInput: LiveData<CardComponentInput.CardExpirationInput>
    val cardExpirationError: LiveData<Int?>
    val cardCvcError: LiveData<Int?>
    val cardHolderNameInput: LiveData<CardComponentInput.CardHolderNameInput>
    val cardHolderNameError: LiveData<Int?>
    val cardHolderNameEnabled: LiveData<Boolean>
    val cardCvcInput: LiveData<CardComponentInput.CardCvcInput>
    val cvcImeOptions: LiveData<Int>
    val cardNumberBrand: LiveData<CardBrand>
    val cardExpiration: LiveData<CardExpiration?>
    val isValid: LiveData<Boolean>
    val cardNumberValid: LiveData<Boolean>
    val cardExpirationValid: LiveData<Boolean>
    val cardCvcValid: LiveData<Boolean>
    val errorFetchAcceptedBrands: LiveData<OneOffValue<Throwable>>
    val acceptedBrands: LiveData<OneOffValue<List<CardBrand>>>
    val showErrorImmediately: LiveData<Boolean>
    val currentPrimaryInput: LiveData<CardFormElementType>
    val cardEmailEnabled: Boolean
    val cardEmailInput: LiveData<CardComponentInput.CardEmailInput>
    val cardEmailError: LiveData<Int?>
    val cardPhoneNumberEnabled: Boolean
    val cardPhoneNumberCountryCode: LiveData<CountryCode>
    val cardPhoneNumberInput: LiveData<CardComponentInput.CardPhoneNumberInput>
    val cardPhoneNumberError: LiveData<Int?>
}
