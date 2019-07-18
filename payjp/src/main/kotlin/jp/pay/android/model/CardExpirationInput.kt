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

import android.text.TextUtils

/**
 * Card expiration input
 *
 * @param input input string e.g. `01/20`
 * @param delimiter e.g. `/` in `01/20`
 */
internal data class CardExpirationInput(
    val input: String?,
    val delimiter: Char
) : CardComponentInput<CardExpiration> {

    override val value: CardExpiration? = validate()

    private fun validate(): CardExpiration? = input?.split(delimiter)?.takeIf {
        // TODO validation
        it.size == 2 && it.all { part -> part.length == 2 && TextUtils.isDigitsOnly(part) }
    }?.let {
        val month = it[0]
        val year = "20" + it[1]
        CardExpiration(month, year)
    }
}