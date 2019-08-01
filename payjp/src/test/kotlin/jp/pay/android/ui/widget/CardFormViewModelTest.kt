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
import jp.pay.android.model.CardComponentInput.CardCvcInput
import jp.pay.android.model.CardComponentInput.CardExpirationInput
import jp.pay.android.model.CardComponentInput.CardHolderNameInput
import jp.pay.android.model.CardComponentInput.CardNumberInput
import jp.pay.android.model.CardExpiration
import jp.pay.android.model.FormInputError
import jp.pay.android.model.TenantId
import jp.pay.android.util.Tasks
import jp.pay.android.validator.CardInputTransformer
import jp.pay.android.validator.CardNumberInputTransformerServise
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
internal class CardFormViewModelTest {

    @Mock
    private lateinit var mockTokenService: PayjpTokenService
    @Mock
    private lateinit var cardNumberInputTransformer: CardNumberInputTransformerServise
    @Mock
    private lateinit var cardExpirationInputTransformer: CardInputTransformer<CardExpirationInput>
    @Mock
    private lateinit var cardCvcInputTransformer: CardInputTransformer<CardCvcInput>
    @Mock
    private lateinit var cardHolderNameInputTransformer: CardInputTransformer<CardHolderNameInput>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        `when`(mockTokenService.getAcceptedBrands(anyNullable()))
            .thenReturn(
                Tasks.success(
                    AcceptedBrandsResponse(brands = listOf(CardBrand.VISA, CardBrand.MASTER_CARD), livemode = true)
                ))
    }

    private fun createViewModel(
        tenantId: TenantId? = null,
        holderNameEnabled: Boolean = true
    ) = CardFormViewModel(
        tokenService = mockTokenService,
        cardNumberInputTransformer = cardNumberInputTransformer,
        cardExpirationInputTransformer = cardExpirationInputTransformer,
        cardCvcInputTransformer = cardCvcInputTransformer,
        cardHolderNameInputTransformer = cardHolderNameInputTransformer,
        tenantId = tenantId,
        holderNameEnabledDefault = holderNameEnabled
    ).apply {
        cardNumberError.observeForever { }
        cardExpirationError.observeForever { }
        cardCvcError.observeForever { }
        cardHolderNameError.observeForever { }
        cardHolderNameEnabled.observeForever { }
        cardNumberBrand.observeForever { }
        cardExpiration.observeForever { }
        isValid.observeForever { }
    }

    private fun mockCorrectInput(
        number: String = "4242424242424242",
        expiration: CardExpiration = CardExpiration("12", "2030"),
        cvc: String = "123",
        name: String = "JANE DOE"
    ) {
        `when`(cardNumberInputTransformer.transform(anyString()))
            .thenReturn(CardNumberInput("4242424242424242", number, null, CardBrand.VISA))
        `when`(cardExpirationInputTransformer.transform(anyString()))
            .thenReturn(CardExpirationInput("12/30", expiration, null))
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput("123", cvc, null))
        `when`(cardHolderNameInputTransformer.transform(anyString()))
            .thenReturn(CardHolderNameInput("JANE DOE", name, null))
    }

    @Test
    fun fetchAcceptedBrands() {
        createViewModel().fetchAcceptedBrands()
        verify(mockTokenService).getAcceptedBrands(null)
    }

    @Test
    fun fetchAcceptedBrands_withTenantId() {
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
        mockCorrectInput()
        createViewModel().run {
            inputCardNumber(robot.number)
            inputCardExpiration(robot.exp)
            inputCardCvc(robot.cvc)
            inputCardHolderName(robot.name)
            assertThat(isValid.value, `is`(true))
        }
    }

    @Test
    fun isValid_incorrect_input() {
        val robot = CardRobot()
        mockCorrectInput()
        reset(cardCvcInputTransformer)
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput("", null, FormInputError(0, true)))
        createViewModel().run {
            inputCardNumber(robot.number)
            inputCardExpiration(robot.exp)
            inputCardCvc(robot.cvc)
            inputCardHolderName(robot.name)
            assertThat(isValid.value, `is`(false))
        }
    }

    @Test
    fun not_required_card_holder_name_if_not_enabled() {
        val robot = CardRobot()
        createViewModel().run {
            inputCardNumber(robot.number)
            inputCardExpiration(robot.exp)
            inputCardCvc(robot.cvc)
            assertThat(isValid.value, `is`(false))
            updateCardHolderNameEnabled(false)
            assertThat(isValid.value, `is`(true))
        }
    }

    @Test
    fun cardNumberError() {
        // TODO
    }

    @Test
    fun cardExpirationError() {
        // TODO
    }

    @Test
    fun cardCvcError() {
        // TODO
    }

    @Test
    fun cardHolderNameError() {
        // TODO
    }

    @Test
    fun validate_indicate_error_without_input() {
        // TODO
    }

    @Test
    fun cardNumberBrand() {
        // TODO
    }

    @Test
    fun cardExpiration() {
        // TODO
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
        val robot = CardRobot()
        mockCorrectInput(
            "4242424242424242",
            CardExpiration("12", "2030"),
            "123",
            "JANE DOE"
        )
        createViewModel().run {
            inputCardNumber(robot.number)
            inputCardExpiration(robot.exp)
            inputCardCvc(robot.cvc)
            inputCardHolderName(robot.name)
            createToken().run()
            verify(mockTokenService).createToken(
                number = "4242424242424242",
                expMonth = "12",
                expYear = "2030",
                cvc = "123",
                name = "JANE DOE"
            )
        }
    }
}