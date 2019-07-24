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

import jp.pay.android.validator.CardNumberValidatorService
import org.hamcrest.Matchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class CardNumberInputTest(
    private val input: String?,
    private val acceptedBrands: List<CardBrand>?,
    private val detectedBrand: CardBrand,
    private val numberIsValid: Boolean,
    private val brand: CardBrand,
    private val value: String?
) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            return listOf(
                arrayOf(null, listOf(CardBrand.VISA), CardBrand.VISA, true, CardBrand.UNKNOWN, null),
                arrayOf("1234", listOf(CardBrand.VISA), CardBrand.VISA, true, CardBrand.VISA, "1234"),
                arrayOf(" 1 2 3 4 abc", listOf(CardBrand.VISA), CardBrand.VISA, true, CardBrand.VISA, "1234"),
                arrayOf("1234", listOf(CardBrand.VISA), CardBrand.VISA, false, CardBrand.VISA, null),
                arrayOf("1234", listOf(CardBrand.MASTER_CARD), CardBrand.VISA, true, CardBrand.VISA, null),
                arrayOf("1234", null, CardBrand.VISA, true, CardBrand.VISA, "1234")
            )
        }
    }

    @Mock
    private lateinit var mockDetector: CardBrandDetectable
    @Mock
    private lateinit var mockNumberValidator: CardNumberValidatorService

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(mockDetector.detectWithDigits(anyString())).thenReturn(detectedBrand)
        `when`(mockNumberValidator.isValidCardNumber(anyString())).thenReturn(numberIsValid)
    }

    @Test
    fun formatInput() {
        val data = CardNumberInput(
            input = input,
            acceptedBrands = acceptedBrands,
            brandDetector = mockDetector,
            cardNumberValidator = mockNumberValidator
        )
        assertThat(data.brand, `is`(brand))
        assertThat("data: $data", data.value, `is`(value))
    }
}