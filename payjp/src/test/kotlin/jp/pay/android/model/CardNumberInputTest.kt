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

import jp.pay.android.R
import jp.pay.android.anyNullable
import jp.pay.android.model.CardBrand.JCB
import jp.pay.android.model.CardBrand.MASTER_CARD
import jp.pay.android.model.CardBrand.UNKNOWN
import jp.pay.android.model.CardBrand.VISA
import jp.pay.android.validator.CardNumberInputTransformer
import jp.pay.android.validator.CardNumberValidatorService
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.MockitoAnnotations
import org.robolectric.ParameterizedRobolectricTestRunner

internal data class CardNumberInputTestData(
    val input: String?,
    val acceptedBrands: List<CardBrand>?,
    val detectedBrand: CardBrand,
    val isLengthValid: CardNumberValidatorService.CardNumberLengthStatus,
    val isLuhnValid: Boolean,
    val brand: CardBrand,
    val value: String?,
    val errorMessage: FormInputError?
)

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class CardNumberInputTest(
    private val data: CardNumberInputTestData
) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): List<Array<out Any?>> {
            val accepteds = listOf(VISA, MASTER_CARD)

            return listOf(
                arrayOf(
                    CardNumberInputTestData(
                        input = null,
                        acceptedBrands = accepteds,
                        detectedBrand = VISA,
                        isLengthValid = CardNumberValidatorService.CardNumberLengthStatus.MATCH,
                        isLuhnValid = true,
                        brand = UNKNOWN,
                        value = null,
                        errorMessage = FormInputError(
                            R.string.payjp_card_form_error_no_number,
                            true
                        )
                    )
                ),
                arrayOf(
                    CardNumberInputTestData(
                        input = "",
                        acceptedBrands = accepteds,
                        detectedBrand = VISA,
                        isLengthValid = CardNumberValidatorService.CardNumberLengthStatus.MATCH,
                        isLuhnValid = true,
                        brand = VISA,
                        value = null,
                        errorMessage = FormInputError(
                            R.string.payjp_card_form_error_no_number,
                            true
                        )
                    )
                ),
                arrayOf(
                    CardNumberInputTestData(
                        input = "abc",
                        acceptedBrands = accepteds,
                        detectedBrand = VISA,
                        isLengthValid = CardNumberValidatorService.CardNumberLengthStatus.MATCH,
                        isLuhnValid = true,
                        brand = VISA,
                        value = null,
                        errorMessage = FormInputError(
                            R.string.payjp_card_form_error_no_number,
                            true
                        )
                    )
                ),
                arrayOf(
                    CardNumberInputTestData(
                        input = "1",
                        acceptedBrands = accepteds,
                        detectedBrand = VISA,
                        isLengthValid = CardNumberValidatorService.CardNumberLengthStatus.TOO_SHORT,
                        isLuhnValid = true,
                        brand = VISA,
                        value = null,
                        errorMessage = FormInputError(
                            R.string.payjp_card_form_error_invalid_number,
                            true
                        )
                    )
                ),
                arrayOf(
                    CardNumberInputTestData(
                        input = "1",
                        acceptedBrands = accepteds,
                        detectedBrand = VISA,
                        isLengthValid = CardNumberValidatorService.CardNumberLengthStatus.TOO_LONG,
                        isLuhnValid = true,
                        brand = VISA,
                        value = null,
                        errorMessage = FormInputError(
                            R.string.payjp_card_form_error_invalid_number,
                            false
                        )
                    )
                ),
                arrayOf(
                    CardNumberInputTestData(
                        input = "1",
                        acceptedBrands = accepteds,
                        detectedBrand = VISA,
                        isLengthValid = CardNumberValidatorService.CardNumberLengthStatus.MATCH,
                        isLuhnValid = false,
                        brand = VISA,
                        value = null,
                        errorMessage = FormInputError(
                            R.string.payjp_card_form_error_invalid_number,
                            false
                        )
                    )
                ),
                arrayOf(
                    CardNumberInputTestData(
                        input = "1",
                        acceptedBrands = accepteds,
                        detectedBrand = UNKNOWN,
                        isLengthValid = CardNumberValidatorService.CardNumberLengthStatus.MATCH,
                        isLuhnValid = true,
                        brand = UNKNOWN,
                        value = null,
                        errorMessage = FormInputError(
                            R.string.payjp_card_form_error_invalid_brand,
                            true
                        )
                    )
                ),
                arrayOf(
                    CardNumberInputTestData(
                        input = "1",
                        acceptedBrands = accepteds,
                        detectedBrand = JCB,
                        isLengthValid = CardNumberValidatorService.CardNumberLengthStatus.MATCH,
                        isLuhnValid = true,
                        brand = JCB,
                        value = null,
                        errorMessage = FormInputError(
                            R.string.payjp_card_form_error_invalid_brand,
                            false
                        )
                    )
                ),
                arrayOf(
                    CardNumberInputTestData(
                        input = "1",
                        acceptedBrands = accepteds,
                        detectedBrand = JCB,
                        isLengthValid = CardNumberValidatorService.CardNumberLengthStatus.MATCH,
                        isLuhnValid = false,
                        brand = JCB,
                        value = null,
                        errorMessage = FormInputError(
                            R.string.payjp_card_form_error_invalid_brand,
                            false
                        )
                    )
                ),
                arrayOf(
                    CardNumberInputTestData(
                        input = "1",
                        acceptedBrands = accepteds,
                        detectedBrand = VISA,
                        isLengthValid = CardNumberValidatorService.CardNumberLengthStatus.MATCH,
                        isLuhnValid = true,
                        brand = VISA,
                        value = "1",
                        errorMessage = null
                    )
                ),
                arrayOf(
                    CardNumberInputTestData(
                        input = "1 2 3 4",
                        acceptedBrands = accepteds,
                        detectedBrand = VISA,
                        isLengthValid = CardNumberValidatorService.CardNumberLengthStatus.MATCH,
                        isLuhnValid = true,
                        brand = VISA,
                        value = "1234",
                        errorMessage = null
                    )
                ),
                arrayOf(
                    CardNumberInputTestData(
                        input = "1234",
                        acceptedBrands = null,
                        detectedBrand = VISA,
                        isLengthValid = CardNumberValidatorService.CardNumberLengthStatus.MATCH,
                        isLuhnValid = true,
                        brand = VISA,
                        value = "1234",
                        errorMessage = null
                    )
                )
            )
        }
    }

    @Mock
    private lateinit var mockDetector: CardBrandDetectorService
    @Mock
    private lateinit var mockNumberValidator: CardNumberValidatorService
    private lateinit var input: CardComponentInput.CardNumberInput

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(mockDetector.detectWithDigits(anyString())).thenReturn(data.detectedBrand)
        `when`(mockNumberValidator.isCardNumberLengthValid(anyString(), anyNullable())).thenReturn(
            data.isLengthValid
        )
        `when`(mockNumberValidator.isLuhnValid(anyString())).thenReturn(data.isLuhnValid)
        val transformer = CardNumberInputTransformer(mockDetector, mockNumberValidator)
        transformer.acceptedBrands = data.acceptedBrands
        input = transformer.transform(data.input)
    }

    @Test
    fun checkValue() {
        assertThat("data: $data", input.value, `is`(data.value))
    }

    @Test
    fun checkBrand() {
        assertThat("data: $data", input.brand, `is`(data.brand))
    }

    @Test
    fun checkErrorMessage() {
        assertThat("data: $data", input.errorMessage, `is`(data.errorMessage))
    }
}