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
    /**
     * VISA
     */
    VISA("Visa"),
    /**
     * Mastercard
     */
    MASTER_CARD("MasterCard"),
    /**
     * JCB
     */
    JCB("JCB"),
    /**
     * American Express
     */
    AMEX("American Express"),
    /**
     * Diners Club
     */
    DINERS_CLUB("Diners Club"),
    /**
     * Discover
     */
    DISCOVER("Discover"),
    /**
     * Unknown brand
     */
    UNKNOWN("Unknown");

    /**
     * Moshi json adapter for CardBrand
     */
    class JsonAdapter {

        @ToJson
        fun toJson(brand: CardBrand): String = brand.rawValue

        @FromJson
        fun fromJson(brand: String): CardBrand {
            return values().filter { it != UNKNOWN }.firstOrNull { it.rawValue == brand }
                ?: throw JsonDataException("unknown brand: $brand")
        }
    }

    /**
     * card number regular expression.
     * Because we only check the lead several character,
     * the regex has no size restriction.
     */
    val numberRegex: Regex
        get() = when (this) {
            VISA -> Regex("""\A4[0-9]*\z""")
            MASTER_CARD -> Regex("""\A(?:5[1-5]|2[2-7])[0-9]*\z""")
            JCB -> Regex("""\A(?:352[8-9]|35[3-8])[0-9]*\z""")
            AMEX -> Regex("""\A3[47][0-9]*\z""")
            DINERS_CLUB -> Regex("""\A3(?:0[0-5]|[68])[0-9]*\z""")
            DISCOVER -> Regex("""\A6(?:011|5)[0-9]*\z""")
            UNKNOWN -> Regex("""""")
        }

    /**
     * valid length of the card number.
     */
    val numberLength: Int
        get() = when (this) {
            DINERS_CLUB -> 14
            AMEX -> 15
            else -> 16
        }

    /**
     * valid length of the cvc number.
     */
    val cvcLength: Int
        get() = when (this) {
            AMEX, UNKNOWN -> 4
            else -> 3
        }
}