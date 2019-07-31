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
package jp.pay.android.ui.widget

import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.CardRobot
import jp.pay.android.PayjpTokenService
import jp.pay.android.TestStubs
import jp.pay.android.anyNullable
import jp.pay.android.exception.PayjpInvalidCardFormException
import jp.pay.android.model.AcceptedBrandsResponse
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardCvcInput
import jp.pay.android.model.CardExpirationInput
import jp.pay.android.model.CardHolderNameInput
import jp.pay.android.model.CardNumberInput
import jp.pay.android.model.TenantId
import jp.pay.android.util.Tasks
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class CardFormViewModelTest {

    @Mock
    lateinit var mockTokenService: PayjpTokenService

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    private fun createViewModel(
        tenantId: TenantId? = null,
        holderNameEnabled: Boolean = true
    ) = CardFormViewModel(mockTokenService, tenantId, holderNameEnabled).apply {
        cardNumberInput.observeForever { }
        cardExpirationInput.observeForever { }
        cardCvcInput.observeForever { }
        cardHolderNameInput.observeForever { }
        cardHolderNameEnabled.observeForever { }
        acceptedBrands.observeForever { }
        isValid.observeForever { }
    }

    @Test
    fun fetchAcceptedBrands() {
        `when`(mockTokenService.getAcceptedBrands(anyNullable()))
            .thenReturn(
                Tasks.success(
                    AcceptedBrandsResponse(brands = listOf(CardBrand.VISA, CardBrand.MASTER_CARD), livemode = true)
                ))
        createViewModel().fetchAcceptedBrands()
        verify(mockTokenService).getAcceptedBrands(null)
    }

    @Test
    fun fetchAcceptedBrands_withTenantId() {
        `when`(mockTokenService.getAcceptedBrands(anyNullable()))
            .thenReturn(
                Tasks.success(
                    AcceptedBrandsResponse(brands = listOf(CardBrand.VISA, CardBrand.MASTER_CARD), livemode = true)
                ))
        val tenantId = TenantId("foobar")
        createViewModel(tenantId = tenantId).fetchAcceptedBrands()
        verify(mockTokenService).getAcceptedBrands(tenantId)
    }

    @Test
    fun isValid_default_false() {
        createViewModel().run {
            assertThat(isValid.value, `is`(false))
        }
    }

    @Test
    fun isValid_correct_input() {
        val robot = CardRobot()
        createViewModel().run {
            updateCardInput(
                CardNumberInput(input = robot.number, acceptedBrands = listOf(CardBrand.VISA, CardBrand.MASTER_CARD))
            )
            updateCardInput(
                CardExpirationInput(input = robot.exp, delimiter = '/')
            )
            updateCardInput(
                CardCvcInput(input = robot.cvc)
            )
            updateCardInput(
                CardHolderNameInput(input = robot.name)
            )
            assertThat(isValid.value, `is`(true))
        }
    }

    @Test
    fun isValid_incorrect_input() {
        val robot = CardRobot()
        createViewModel().run {
            updateCardInput(
                CardNumberInput(input = robot.number, acceptedBrands = listOf(CardBrand.VISA, CardBrand.MASTER_CARD))
            )
            updateCardInput(
                CardExpirationInput(input = robot.exp, delimiter = '/')
            )
            updateCardInput(
                CardCvcInput(input = "")
            )
            updateCardInput(
                CardHolderNameInput(input = robot.name)
            )
            assertThat(isValid.value, `is`(false))
        }
    }

    @Test
    fun not_required_card_holder_name_if_not_enabled() {
        val robot = CardRobot()
        createViewModel().run {
            updateCardInput(
                CardNumberInput(input = robot.number, acceptedBrands = listOf(CardBrand.VISA, CardBrand.MASTER_CARD))
            )
            updateCardInput(
                CardExpirationInput(input = robot.exp, delimiter = '/')
            )
            updateCardInput(
                CardCvcInput(input = robot.cvc)
            )
            assertThat(isValid.value, `is`(false))
            updateCardHolderNameEnabled(false)
            assertThat(isValid.value, `is`(true))
        }
    }

    @Test(expected = PayjpInvalidCardFormException::class)
    fun createCardToken_without_input() {
        createViewModel().run {
            createToken().run()
        }
    }

    @Test
    fun validateCardForm_true_with_correct_input() {
        `when`(mockTokenService.createToken(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Tasks.success(TestStubs.newToken()))
        val robot = CardRobot(
            number = "4242424242424242",
            exp = "12/30",
            cvc = "123",
            name = "TARO YAMADA"
        )
        createViewModel().run {
            updateCardInput(
                CardNumberInput(input = robot.number, acceptedBrands = listOf(CardBrand.VISA, CardBrand.MASTER_CARD))
            )
            updateCardInput(
                CardExpirationInput(input = robot.exp, delimiter = '/')
            )
            updateCardInput(
                CardCvcInput(input = robot.cvc)
            )
            updateCardInput(
                CardHolderNameInput(input = robot.name)
            )
            createToken().run()
            verify(mockTokenService).createToken(
                number = robot.number,
                expMonth = "12",
                expYear = "2030",
                cvc = robot.cvc,
                name = robot.name
            )
        }
    }
}