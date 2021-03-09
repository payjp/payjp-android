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
@file:JvmName("CardBrandExtension")

package jp.pay.android.ui.extension

import androidx.annotation.DrawableRes
import jp.pay.android.R
import jp.pay.android.model.CardBrand

/**
 * Card brand logo
 * If the brand is unknown, returns default card icon.
 *
 * @return drawable resource id of brand logo.
 * @see [CardBrand]
 */
val CardBrand.logoResourceId: Int
    @DrawableRes
    get() = when (this) {
        CardBrand.VISA -> R.drawable.logo_visa
        CardBrand.MASTER_CARD -> R.drawable.logo_mastercard
        CardBrand.JCB -> R.drawable.logo_jcb
        CardBrand.AMEX -> R.drawable.logo_amex
        CardBrand.DINERS_CLUB -> R.drawable.logo_diners
        CardBrand.DISCOVER -> R.drawable.logo_discover
        CardBrand.UNKNOWN -> R.drawable.ic_card
    }

val CardBrand.displayLogoResourceId: Int?
    @DrawableRes
    get() = when (this) {
        CardBrand.VISA -> R.drawable.ic_card_display_brand_visa
        CardBrand.MASTER_CARD -> R.drawable.ic_card_display_brand_mastercard
        CardBrand.JCB -> R.drawable.ic_card_display_brand_jcb
        CardBrand.AMEX -> R.drawable.ic_card_display_brand_amex
        CardBrand.DINERS_CLUB -> R.drawable.ic_card_display_brand_diners
        CardBrand.DISCOVER -> R.drawable.ic_card_display_brand_discover
        CardBrand.UNKNOWN -> null
    }

/**
 * Icon of security code (a.k.a. cvc, cvv)
 *
 * @return drawable resource id of icon.
 */
val CardBrand.cvcIconResourceId: Int
    @DrawableRes
    get() = when (this) {
        CardBrand.AMEX -> R.drawable.ic_cvc_front
        else -> R.drawable.ic_cvc_back
    }

val CardBrand.numberFormat: IntArray
    get() = when (this) {
        CardBrand.DINERS_CLUB -> intArrayOf(4, 6, 4)
        CardBrand.AMEX -> intArrayOf(4, 6, 5)
        else -> intArrayOf(4, 4, 4, 4)
    }

val CardBrand.numberDelimiterPositions: IntArray
    get() {
        val format = numberFormat
        return format.foldIndexed(intArrayOf()) { index, acc, i ->
            when (index) {
                0 -> acc.plus(i)
                in 1 until format.lastIndex -> acc.plus(acc.last() + i)
                else -> acc
            }
        }
    }

fun CardBrand.lastMaskedPan(maskChar: Char, delimiter: Char, src: CharSequence, lastSize: Int = 4): String {
    require(lastSize > 0) { "lastSize must be larger than zero." }
    val format = numberFormat
    val allCount = format.sum()
    require(lastSize < allCount) { "lastSize must be less than pan size." }
    val pan = src.filter(Character::isDigit)
    if (pan.length != allCount) {
        return fullMaskedPan(maskChar, delimiter)
    }
    val delimiterPosition = numberDelimiterPositions
    val sb = StringBuilder()
    pan.forEachIndexed { index, c ->
        if (allCount - lastSize > index) {
            sb.append(maskChar)
        } else {
            sb.append(c)
        }
        if (delimiterPosition.contains(index + 1)) {
            sb.append(delimiter)
        }
    }
    return sb.toString()
}

fun CardBrand.fullMaskedPan(maskChar: Char, delimiter: Char): String =
    numberFormat.foldIndexed("") { index, acc, i ->
        var r = acc + maskChar.toString().repeat(i)
        if (index != numberFormat.lastIndex) {
            r += delimiter
        }
        r
    }
