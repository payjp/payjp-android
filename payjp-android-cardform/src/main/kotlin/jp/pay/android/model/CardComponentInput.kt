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
package jp.pay.android.model

internal sealed class CardComponentInput<T> {

    /**
     * raw value of input
     */
    abstract val input: String?

    /**
     * validated value or null
     */
    abstract val value: T?

    val valid: Boolean
        get() = value != null

    /**
     * The string resource id of input error message.
     * If no error, it return null.
     *
     */
    abstract val errorMessage: FormInputError?

    /**
     * Card Number
     */
    internal data class CardNumberInput(
        override val input: String?,
        override val value: String?,
        override val errorMessage: FormInputError?,
        val brand: CardBrand
    ) : CardComponentInput<String>()

    /**
     * Card Expiration
     *
     * @param input input string e.g. `01/20`
     */
    internal data class CardExpirationInput(
        override val input: String?,
        override val value: CardExpiration?,
        override val errorMessage: FormInputError?
    ) : CardComponentInput<CardExpiration>()

    /**
     * CVC
     */
    internal data class CardCvcInput(
        override val input: String?,
        override val value: String?,
        override val errorMessage: FormInputError?
    ) : CardComponentInput<String>()

    /**
     * Card Holder Name
     */
    internal data class CardHolderNameInput(
        override val input: String?,
        override val value: String?,
        override val errorMessage: FormInputError?
    ) : CardComponentInput<String>()

    /**
     * Email (as TDS Attributes)
     */
    internal data class CardEmailInput(
        override val input: String?,
        override val value: String?,
        override val errorMessage: FormInputError?
    ) : CardComponentInput<String>()

    /**
     * Phone Number (as TDS Attributes)
     */
    internal data class CardPhoneNumberInput(
        override val input: String?,
        override val value: String?,
        override val errorMessage: FormInputError?
    ) : CardComponentInput<String>()
}
