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
package jp.pay.android.ui.extension

import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.model.CardBrand
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardBrandExtensionTest {

    @Test
    fun fullMaskedPan() {
        assertThat(
            CardBrand.VISA.fullMaskedPan('X', ' '),
            `is`("XXXX XXXX XXXX XXXX")
        )
        assertThat(
            CardBrand.MASTER_CARD.fullMaskedPan('*', '-'),
            `is`("****-****-****-****")
        )
        assertThat(
            CardBrand.JCB.fullMaskedPan('X', ' '),
            `is`("XXXX XXXX XXXX XXXX")
        )
        assertThat(
            CardBrand.DISCOVER.fullMaskedPan('X', ' '),
            `is`("XXXX XXXX XXXX XXXX")
        )
        assertThat(
            CardBrand.AMEX.fullMaskedPan('X', ' '),
            `is`("XXXX XXXXXX XXXXX")
        )
        assertThat(
            CardBrand.DINERS_CLUB.fullMaskedPan('X', ' '),
            `is`("XXXX XXXXXX XXXX")
        )
        assertThat(
            CardBrand.UNKNOWN.fullMaskedPan('X', ' '),
            `is`("XXXX XXXX XXXX XXXX")
        )
    }

    @Test
    fun lastMaskedPan_visa() {
        assertThat(
            CardBrand.VISA.lastMaskedPan('*', ' ', "4242 4242 4242 4242", 1),
            `is`("**** **** **** ***2")
        )
        assertThat(
            CardBrand.VISA.lastMaskedPan('*', ' ', "4242 4242 4242 4242", 4),
            `is`("**** **** **** 4242")
        )
        assertThat(
            CardBrand.VISA.lastMaskedPan('*', ' ', "4242 4242 4242 4242", 5),
            `is`("**** **** ***2 4242")
        )
    }

    @Test
    fun lastMaskedPan_amex() {
        assertThat(
            CardBrand.AMEX.lastMaskedPan('*', ' ', "378282246310005", 1),
            `is`("**** ****** ****5")
        )
        assertThat(
            CardBrand.AMEX.lastMaskedPan('*', ' ', "378282246310005", 4),
            `is`("**** ****** *0005")
        )
    }

    @Test
    fun lastMaskedPan_format() {
        assertThat(
            CardBrand.VISA.lastMaskedPan('*', ' ', "4242 4242 4242", 4),
            `is`("**** **** **** ****")
        )
        assertThat(
            CardBrand.VISA.lastMaskedPan('X', '-', "4242 4242 4242 4242", 4),
            `is`("XXXX-XXXX-XXXX-4242")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun lastMaskedPan_zero() {
        CardBrand.VISA.lastMaskedPan('*', ' ', "4242 4242 4242 4242", 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun lastMaskedPan_too_large() {
        CardBrand.VISA.lastMaskedPan('*', ' ', "4242 4242 4242 4242", 18)
    }
}
