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

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson

/**
 * CardBrand
 */
enum class CardBrand(val rawValue: String) {
    VISA("Visa"),
    MASTER_CARD("MasterCard"),
    JCB("JCB"),
    AMEX("American Express"),
    DINERS_CLUB("Diners Club"),
    DISCOVER("Discover"),
    UNKNOWN("Unknown");

    class JsonAdapter {

        @ToJson fun toJson(brand: CardBrand): String = brand.rawValue

        @FromJson fun fromJson(brand: String): CardBrand {
            return values().filter { it != UNKNOWN }.firstOrNull { it.rawValue == brand }
                    ?: throw JsonDataException("unknown brand: $brand")
        }
    }
}

val CardBrand.numberRegex: Regex
    get() = when (this) {
        CardBrand.VISA -> Regex("""\A4[0-9]*\z""")
        CardBrand.MASTER_CARD -> Regex("""\A(?:5[1-5]|2[2-7])[0-9]*\z""")
        CardBrand.JCB -> Regex("""\A35[0-9]*\z""")
        CardBrand.AMEX -> Regex("""\A3[47][0-9]*\z""")
        CardBrand.DINERS_CLUB -> Regex("""\A3[0689][0-9]*\z""")
        CardBrand.DISCOVER -> Regex("""\A6[0245][0-9]*\z""")
        CardBrand.UNKNOWN -> Regex("""""")
    }