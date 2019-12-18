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
