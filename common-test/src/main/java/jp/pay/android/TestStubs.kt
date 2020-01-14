/*
 *
 * Copyright (c) 2020 PAY, Inc.
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
package jp.pay.android

import java.util.Date
import jp.pay.android.model.Card
import jp.pay.android.model.CardBrand
import jp.pay.android.model.Token

object TestStubs {

    fun newCard(
        seed: Int = 0,
        id: String = "id_$seed",
        name: String = "name $seed",
        last4: String = "1234",
        brand: CardBrand = CardBrand.VISA,
        expirationMonth: Int = 12,
        expirationYear: Int = 2020,
        fingerprint: String = "fingerprint_$seed",
        livemode: Boolean = true,
        created: Date = Date(seed.toLong())
    ): Card = Card(
        id = id,
        name = name,
        last4 = last4,
        brand = brand,
        expirationMonth = expirationMonth,
        expirationYear = expirationYear,
        fingerprint = fingerprint,
        livemode = livemode,
        created = created
    )

    fun newToken(
        seed: Int = 0,
        id: String = "id_$seed",
        card: Card = newCard(seed),
        livemode: Boolean = card.livemode,
        used: Boolean = false,
        created: Date = Date(seed.toLong())
    ): Token = Token(
        id = id,
        card = card,
        livemode = livemode,
        used = used,
        created = created
    )
}
