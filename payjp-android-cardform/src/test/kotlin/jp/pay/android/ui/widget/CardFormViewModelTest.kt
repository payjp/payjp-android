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
package jp.pay.android.ui.widget

import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.CardRobot
import jp.pay.android.PayjpTokenService
import jp.pay.android.TestStubs
import jp.pay.android.anyNullable
import jp.pay.android.data.PhoneNumberService
import jp.pay.android.exception.PayjpInvalidCardFormException
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.CardComponentInput
import jp.pay.android.model.CardComponentInput.CardCvcInput
import jp.pay.android.model.CardComponentInput.CardExpirationInput
import jp.pay.android.model.CardComponentInput.CardHolderNameInput
import jp.pay.android.model.CardComponentInput.CardNumberInput
import jp.pay.android.model.CardExpiration
import jp.pay.android.model.CountryCode
import jp.pay.android.model.ExtraAttribute
import jp.pay.android.model.FormInputError
import jp.pay.android.model.TenantId
import jp.pay.android.util.Tasks
import jp.pay.android.validator.CardCvcInputTransformerService
import jp.pay.android.validator.CardInputTransformer
import jp.pay.android.validator.CardNumberInputTransformerService
import jp.pay.android.validator.CardPhoneNumberInputTransformerService
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
internal class CardFormViewModelTest {

    @Mock
    private lateinit var mockTokenService: PayjpTokenService
    @Mock
    private lateinit var mockPhoneNumberService: PhoneNumberService
    @Mock
    private lateinit var cardNumberInputTransformer: CardNumberInputTransformerService
    @Mock
    private lateinit var cardExpirationInputTransformer: CardInputTransformer<CardExpirationInput>
    @Mock
    private lateinit var cardCvcInputTransformer: CardCvcInputTransformerService
    @Mock
    private lateinit var cardHolderNameInputTransformer: CardInputTransformer<CardHolderNameInput>
    @Mock
    private lateinit var cardNumberErrorObserver: Observer<in Int?>
    @Mock
    private lateinit var cardEmailInputTransformer: CardInputTransformer<CardComponentInput.CardEmailInput>
    @Mock
    private lateinit var cardPhoneNumberInputTransformer: CardPhoneNumberInputTransformerService

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    private fun createViewModel(
        tenantId: TenantId? = null,
        acceptedBrandList: List<CardBrand> = emptyList(),
        extraAttributes: List<ExtraAttribute<*>> = listOf(
            ExtraAttribute.Email(),
            ExtraAttribute.Phone("JP"),
        ),
    ) = CardFormViewModel(
        tokenService = mockTokenService,
        cardNumberInputTransformer = cardNumberInputTransformer,
        cardExpirationInputTransformer = cardExpirationInputTransformer,
        cardCvcInputTransformer = cardCvcInputTransformer,
        cardHolderNameInputTransformer = cardHolderNameInputTransformer,
        cardEmailInputTransformer = cardEmailInputTransformer,
        cardPhoneNumberInputTransformer = cardPhoneNumberInputTransformer,
        tenantId = tenantId,
        acceptedBrandsPreset = acceptedBrandList,
        phoneNumberService = mockPhoneNumberService,
        extraAttributes = extraAttributes,
    ).apply {
        cardNumberError.observeForever { }
        cardExpirationError.observeForever { }
        cardCvcError.observeForever { }
        cardHolderNameError.observeForever { }
        cardNumberBrand.observeForever { }
        cardExpiration.observeForever { }
        isValid.observeForever { }
        cardNumberValid.observeForever { }
        cardExpirationValid.observeForever { }
        cardCvcValid.observeForever { }
        errorFetchAcceptedBrands.observeForever { }
        acceptedBrands.observeForever { }
        cardEmailError.observeForever { }
        cardPhoneNumberError.observeForever { }
        cardPhoneNumberCountryCode.observeForever { }

        cardNumberError.observeForever(cardNumberErrorObserver)
    }

    @Suppress("LongParameterList")
    private fun mockCorrectInput(
        number: String = "4242424242424242",
        expiration: CardExpiration = CardExpiration("12", "2030"),
        cvc: String = "123",
        name: String = "JANE DOE",
        email: String = "test@example.com",
        phoneNumber: String = "+819012345678",
    ) {
        `when`(cardNumberInputTransformer.transform(anyString()))
            .thenReturn(CardNumberInput("4242424242424242", number, null, CardBrand.VISA))
        `when`(cardExpirationInputTransformer.transform(anyString()))
            .thenReturn(CardExpirationInput("12/30", expiration, null))
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput("123", cvc, null))
        `when`(cardHolderNameInputTransformer.transform(anyString()))
            .thenReturn(CardHolderNameInput("JANE DOE", name, null))
        `when`(cardEmailInputTransformer.transform(anyString()))
            .thenReturn(CardComponentInput.CardEmailInput("test@example.com", email, null))
        `when`(cardPhoneNumberInputTransformer.transform(anyString()))
            .thenReturn(CardComponentInput.CardPhoneNumberInput("09012345678", phoneNumber, null))
    }

    @Test
    fun acceptedBrands_injectAt_init() {
        val brands = listOf(CardBrand.VISA, CardBrand.MASTER_CARD)
        createViewModel(acceptedBrandList = brands)
        verify(cardNumberInputTransformer).acceptedBrands = brands
    }

    @Test
    fun fetchAcceptedBrands_no_brands() {
        val brands = listOf(CardBrand.VISA, CardBrand.MASTER_CARD)
        `when`(mockTokenService.getAcceptedBrands(anyNullable()))
            .thenReturn(
                Tasks.success(
                    CardBrandsAcceptedResponse(brands = brands, livemode = true)
                )
            )
        `when`(cardNumberInputTransformer.acceptedBrands).thenReturn(null)
        val viewModel = createViewModel()
        viewModel.fetchAcceptedBrands()
        verify(mockTokenService).getAcceptedBrands(null)
        verify(cardNumberInputTransformer).acceptedBrands = brands
        assertThat(viewModel.acceptedBrands.value?.peek(), `is`(brands))
        assertThat(viewModel.errorFetchAcceptedBrands.value?.peek(), `is`(nullValue()))
    }

    @Test
    fun fetchAcceptedBrands_error() {
        val error: Throwable = RuntimeException("omg")
        `when`(mockTokenService.getAcceptedBrands(anyNullable()))
            .thenReturn(Tasks.failure(error))
        val viewModel = createViewModel()
        // call `setAcceptedBrands` in init
        reset(cardNumberInputTransformer)
        `when`(cardNumberInputTransformer.acceptedBrands).thenReturn(null)
        viewModel.fetchAcceptedBrands()
        verify(cardNumberInputTransformer, never()).acceptedBrands = anyNullable()
        assertThat(viewModel.acceptedBrands.value?.peek(), `is`(nullValue()))
        assertThat(viewModel.errorFetchAcceptedBrands.value?.peek(), `is`(error))
    }

    @Test
    fun fetchAcceptedBrands_already_fetched() {
        val brands = listOf(CardBrand.VISA, CardBrand.MASTER_CARD)
        `when`(cardNumberInputTransformer.acceptedBrands).thenReturn(brands)
        createViewModel().fetchAcceptedBrands()
        verify(mockTokenService, never()).getAcceptedBrands(null)
    }

    @Test
    fun fetchAcceptedBrands_withTenantId() {
        val tenantId = TenantId("foobar")
        val brands = listOf(CardBrand.VISA, CardBrand.MASTER_CARD)
        `when`(mockTokenService.getAcceptedBrands(anyNullable()))
            .thenReturn(
                Tasks.success(
                    CardBrandsAcceptedResponse(brands = brands, livemode = true)
                )
            )
        `when`(cardNumberInputTransformer.acceptedBrands).thenReturn(null)
        createViewModel(tenantId = tenantId).fetchAcceptedBrands()
        verify(mockTokenService).getAcceptedBrands(tenantId)
        verify(cardNumberInputTransformer).acceptedBrands = brands
    }

    @Test
    fun isValid_default_false() {
        createViewModel().run {
            assertThat(isValid.value, `is`(false))
        }
    }

    @Test
    fun isValid_correct_input() {
        val robot = CardRobot.SandboxVisa
        mockCorrectInput()
        createViewModel().run {
            inputCardNumber(robot.number)
            inputCardExpiration(robot.exp)
            inputCardCvc(robot.cvc)
            inputCardHolderName(robot.name)
            inputEmail(robot.email)
            selectCountryCode(CountryCode(robot.countryRegion, robot.countryCode))
            inputPhoneNumber(robot.phoneNumber)
            assertThat(isValid.value, `is`(true))
        }
    }

    @Test
    fun isValid_incorrect_input() {
        val robot = CardRobot.SandboxVisa
        mockCorrectInput()
        reset(cardCvcInputTransformer)
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput("", null, FormInputError(0, true)))
        createViewModel().run {
            inputCardNumber(robot.number)
            inputCardExpiration(robot.exp)
            inputCardCvc(robot.cvc)
            inputCardHolderName(robot.name)
            inputEmail(robot.email)
            selectCountryCode(CountryCode(robot.countryRegion, robot.countryCode))
            inputPhoneNumber(robot.phoneNumber)
            assertThat(isValid.value, `is`(false))
        }
    }

    @Test
    fun isValid_if_email_is_not_enabled_allow_empty() {
        val robot = CardRobot.SandboxVisa
        mockCorrectInput()
        createViewModel(extraAttributes = listOf(ExtraAttribute.Phone("JP"))).run {
            inputCardNumber(robot.number)
            inputCardExpiration(robot.exp)
            inputCardCvc(robot.cvc)
            inputCardHolderName(robot.name)
            selectCountryCode(CountryCode(robot.countryRegion, robot.countryCode))
            inputPhoneNumber(robot.phoneNumber)
            assertThat(isValid.value, `is`(true))
        }
    }

    @Test
    fun isValid_if_phone_is_not_enabled_allow_empty() {
        val robot = CardRobot.SandboxVisa
        mockCorrectInput()
        createViewModel(extraAttributes = listOf(ExtraAttribute.Email())).run {
            inputCardNumber(robot.number)
            inputCardExpiration(robot.exp)
            inputCardCvc(robot.cvc)
            inputCardHolderName(robot.name)
            inputEmail(robot.email)
            assertThat(isValid.value, `is`(true))
        }
    }

    @Test
    fun isValid_if_phone_and_email_is_not_enabled_allow_empty() {
        val robot = CardRobot.SandboxVisa
        mockCorrectInput()
        createViewModel(extraAttributes = emptyList()).run {
            inputCardNumber(robot.number)
            inputCardExpiration(robot.exp)
            inputCardCvc(robot.cvc)
            inputCardHolderName(robot.name)
            assertThat(isValid.value, `is`(true))
        }
    }

    @Test
    fun isValid_if_both_phone_and_email_is_enabled_allow_phone_empty() {
        val robot = CardRobot.SandboxVisa
        mockCorrectInput()
        createViewModel(extraAttributes = listOf(ExtraAttribute.Email(), ExtraAttribute.Phone("JP"))).run {
            inputCardNumber(robot.number)
            inputCardExpiration(robot.exp)
            inputCardCvc(robot.cvc)
            inputCardHolderName(robot.name)
            inputEmail(robot.email)
            assertThat(isValid.value, `is`(true))
        }
    }

    @Test
    fun isValid_if_both_phone_and_email_is_enabled_allow_email_empty() {
        val robot = CardRobot.SandboxVisa
        mockCorrectInput()
        createViewModel(extraAttributes = listOf(ExtraAttribute.Email(), ExtraAttribute.Phone("JP"))).run {
            inputCardNumber(robot.number)
            inputCardExpiration(robot.exp)
            inputCardCvc(robot.cvc)
            inputCardHolderName(robot.name)
            selectCountryCode(CountryCode(robot.countryRegion, robot.countryCode))
            inputPhoneNumber(robot.phoneNumber)
            assertThat(isValid.value, `is`(true))
        }
    }

    @Test
    fun isValid_if_both_phone_and_email_is_enabled_not_allow_both_empty() {
        val robot = CardRobot.SandboxVisa
        mockCorrectInput()
        createViewModel(extraAttributes = listOf(ExtraAttribute.Email(), ExtraAttribute.Phone("JP"))).run {
            inputCardNumber(robot.number)
            inputCardExpiration(robot.exp)
            inputCardCvc(robot.cvc)
            inputCardHolderName(robot.name)
            assertThat(isValid.value, `is`(false))
        }
    }

    @Test
    fun isValid_if_both_phone_and_email_is_enabled_not_allow_phone_invalid() {
        val robot = CardRobot.SandboxVisa
        mockCorrectInput()
        reset(cardPhoneNumberInputTransformer)
        `when`(cardPhoneNumberInputTransformer.transform(anyString()))
            .thenReturn(
                CardComponentInput.CardPhoneNumberInput(
                    "123",
                    null,
                    FormInputError(jp.pay.android.R.string.payjp_card_form_error_invalid_phone_number, false),
                )
            )
        createViewModel(extraAttributes = listOf(ExtraAttribute.Email(), ExtraAttribute.Phone("JP"))).run {
            inputCardNumber(robot.number)
            inputCardExpiration(robot.exp)
            inputCardCvc(robot.cvc)
            inputCardHolderName(robot.name)
            inputEmail(robot.email)
            selectCountryCode(CountryCode(robot.countryRegion, robot.countryCode))
            inputPhoneNumber("123")
            assertThat(isValid.value, `is`(false))
        }
    }

    @Test
    fun cardNumberError_lazy() {
        val errorId = 0
        val formError = FormInputError(errorId, true)
        `when`(cardNumberInputTransformer.transform(anyString()))
            .thenReturn(CardNumberInput(null, null, formError, CardBrand.VISA))
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput(null, null, formError))
        createViewModel().run {
            inputCardNumber("")
            assertThat(cardNumberError.value, nullValue())
        }
    }

    @Test
    fun cardNumberError_not_lazy() {
        val errorId = 0
        val formError = FormInputError(errorId, false)
        `when`(cardNumberInputTransformer.transform(anyString()))
            .thenReturn(CardNumberInput(null, null, formError, CardBrand.VISA))
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput(null, null, formError))
        createViewModel().run {
            inputCardNumber("")
            assertThat(cardNumberError.value, `is`(errorId))
        }
    }

    @Test
    fun cardNumberError_distinct() {
        val errorId = 0
        val formError = FormInputError(errorId, false)
        // return error twice for input twice
        `when`(cardNumberInputTransformer.transform(anyString()))
            .thenReturn(CardNumberInput(null, null, formError, CardBrand.VISA))
            .thenReturn(CardNumberInput(null, null, formError, CardBrand.VISA))
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput(null, null, formError))
        // input twice
        createViewModel().run {
            inputCardNumber("")
            inputCardNumber("")
        }
        // call once
        verify(cardNumberErrorObserver).onChanged(0)
    }

    @Test
    fun cardExpirationError_lazy() {
        val errorId = 0
        val formError = FormInputError(errorId, true)
        `when`(cardExpirationInputTransformer.transform(anyString()))
            .thenReturn(CardExpirationInput(null, null, formError))
        createViewModel().run {
            inputCardExpiration("")
            assertThat(cardExpirationError.value, nullValue())
        }
    }

    @Test
    fun cardExpirationError_not_lazy() {
        val errorId = 0
        val formError = FormInputError(errorId, false)
        `when`(cardExpirationInputTransformer.transform(anyString()))
            .thenReturn(CardExpirationInput(null, null, formError))
        createViewModel().run {
            inputCardExpiration("")
            assertThat(cardExpirationError.value, `is`(errorId))
        }
    }

    @Test
    fun cardCvcError_lazy() {
        val errorId = 0
        val formError = FormInputError(errorId, true)
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput(null, null, formError))
        createViewModel().run {
            inputCardCvc("")
            assertThat(cardCvcError.value, nullValue())
        }
    }

    @Test
    fun cardCvcError_not_lazy() {
        val errorId = 0
        val formError = FormInputError(errorId, false)
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput(null, null, formError))
        createViewModel().run {
            inputCardCvc("")
            assertThat(cardCvcError.value, `is`(errorId))
        }
    }

    @Test
    fun cardHolderNameError_lazy() {
        val errorId = 0
        val formError = FormInputError(errorId, true)
        `when`(cardHolderNameInputTransformer.transform(anyString()))
            .thenReturn(CardHolderNameInput(null, null, formError))
        createViewModel().run {
            inputCardHolderName("")
            assertThat(cardHolderNameError.value, nullValue())
        }
    }

    @Test
    fun cardHolderNameError_not_lazy() {
        val errorId = 0
        val formError = FormInputError(errorId, false)
        `when`(cardHolderNameInputTransformer.transform(anyString()))
            .thenReturn(CardHolderNameInput(null, null, formError))
        createViewModel().run {
            inputCardHolderName("")
            assertThat(cardHolderNameError.value, `is`(errorId))
        }
    }

    @Test
    fun cardNumberValid() {
        `when`(cardNumberInputTransformer.transform(anyString()))
            .thenReturn(CardNumberInput(null, "1234", null, CardBrand.VISA))
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput(null, null, FormInputError(0, true)))
        createViewModel().run {
            inputCardNumber("")
            assertThat(cardNumberValid.value, `is`(true))
        }
    }

    @Test
    fun cardExpirationValid() {
        `when`(cardExpirationInputTransformer.transform(anyString()))
            .thenReturn(CardExpirationInput(null, CardExpiration("12", "2030"), null))
        createViewModel().run {
            inputCardExpiration("")
            assertThat(cardExpirationValid.value, `is`(true))
        }
    }

    @Test
    fun cardCvcValid() {
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput(null, "123", null))
        createViewModel().run {
            inputCardCvc("")
            assertThat(cardCvcValid.value, `is`(true))
        }
    }

    @Test
    fun validate_indicate_error_without_input() {
        val errorId = 1
        val formError = FormInputError(errorId, true)
        `when`(cardNumberInputTransformer.transform(anyString()))
            .thenReturn(CardNumberInput(null, null, formError, CardBrand.VISA))
        `when`(cardExpirationInputTransformer.transform(anyString()))
            .thenReturn(CardExpirationInput(null, null, formError))
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput(null, null, formError))
        `when`(cardHolderNameInputTransformer.transform(anyString()))
            .thenReturn(CardHolderNameInput(null, null, formError))
        createViewModel().run {
            assertThat(cardNumberError.value, nullValue())
            assertThat(cardExpirationError.value, nullValue())
            assertThat(cardCvcError.value, nullValue())
            assertThat(cardHolderNameError.value, nullValue())
            validate()
            assertThat(cardNumberError.value, `is`(errorId))
            assertThat(cardExpirationError.value, `is`(errorId))
            assertThat(cardCvcError.value, `is`(errorId))
            assertThat(cardHolderNameError.value, `is`(errorId))
        }
    }

    @Test
    fun cardNumberBrand() {
        `when`(cardNumberInputTransformer.transform(anyString()))
            .thenReturn(CardNumberInput(null, "4242", null, CardBrand.VISA))
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput(null, "123", null))
        createViewModel().run {
            inputCardNumber("4242")
            assertThat(cardNumberBrand.value, `is`(CardBrand.VISA))
        }
    }

    @Test
    fun cardNumberBrand_validate_cvc_when_brand_changed() {
        `when`(cardNumberInputTransformer.transform(anyString()))
            .thenReturn(CardNumberInput("4242", "4242", null, CardBrand.VISA))
            .thenReturn(CardNumberInput("3714", "3714", null, CardBrand.AMEX))
        `when`(cardCvcInputTransformer.brand)
            .thenReturn(CardBrand.UNKNOWN)
            .thenReturn(CardBrand.VISA)
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput(null, "123", null))
        createViewModel().run {
            inputCardNumber("4242")
            inputCardNumber("3714")
            verify(cardCvcInputTransformer, times(2)).transform(anyString())
        }
    }

    @Test
    fun cardNumberBrand_not_validate_cvc_when_brand_not_changed() {
        `when`(cardNumberInputTransformer.transform(anyString()))
            .thenReturn(CardNumberInput(null, "4242", null, CardBrand.VISA))
        `when`(cardCvcInputTransformer.brand)
            .thenReturn(CardBrand.UNKNOWN)
            .thenReturn(CardBrand.VISA)
        `when`(cardCvcInputTransformer.transform(anyString()))
            .thenReturn(CardCvcInput(null, "123", null))
        createViewModel().run {
            inputCardNumber("4242")
            inputCardNumber("4242")
            verify(cardNumberInputTransformer, times(2)).transform(anyString())
            verify(cardCvcInputTransformer).transform(anyString())
        }
    }

    @Test
    fun cardExpiration() {
        val expiration = CardExpiration("12", "2030")
        `when`(cardExpirationInputTransformer.transform(anyString()))
            .thenReturn(CardExpirationInput(null, expiration, null))
        createViewModel().run {
            inputCardExpiration("12/30")
            assertThat(cardExpiration.value, `is`(expiration))
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
        `when`(
            mockTokenService.createToken(
                number = anyString(),
                cvc = anyString(),
                expMonth = anyString(),
                expYear = anyString(),
                name = anyString(),
                tenantId = anyNullable(),
                email = anyString(),
                phone = anyString(),
                threeDSecure = anyBoolean(),
            )
        )
            .thenReturn(Tasks.success(TestStubs.newToken()))
        val robot = CardRobot.SandboxVisa
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
            inputEmail(robot.email)
            selectCountryCode(CountryCode(robot.countryRegion, robot.countryCode))
            inputPhoneNumber(robot.phoneNumber)
            createToken().run()
            verify(mockTokenService).createToken(
                number = "4242424242424242",
                expMonth = "12",
                expYear = "2030",
                cvc = "123",
                name = "JANE DOE",
                tenantId = null,
                email = "test@example.com",
                phone = "+819012345678",
                threeDSecure = false,
            )
        }
    }

    @Test
    fun validateCardForm_true_with_correct_input_and_token() {
        `when`(
            mockTokenService.createToken(
                number = anyString(),
                cvc = anyString(),
                expMonth = anyString(),
                expYear = anyString(),
                name = anyString(),
                tenantId = anyNullable(),
                email = anyString(),
                phone = anyString(),
                threeDSecure = anyBoolean(),
            )
        )
            .thenReturn(Tasks.success(TestStubs.newToken()))
        val robot = CardRobot.SandboxVisa
        mockCorrectInput(
            "4242424242424242",
            CardExpiration("12", "2030"),
            "123",
            "JANE DOE"
        )
        val tenantId = TenantId("id")
        createViewModel(tenantId = tenantId).run {
            inputCardNumber(robot.number)
            inputCardExpiration(robot.exp)
            inputCardCvc(robot.cvc)
            inputCardHolderName(robot.name)
            inputEmail(robot.email)
            selectCountryCode(CountryCode(robot.countryRegion, robot.countryCode))
            inputPhoneNumber(robot.phoneNumber)
            createToken().run()
            verify(mockTokenService).createToken(
                number = "4242424242424242",
                expMonth = "12",
                expYear = "2030",
                cvc = "123",
                name = "JANE DOE",
                tenantId = tenantId,
                email = "test@example.com",
                phone = "+819012345678",
                threeDSecure = false,
            )
        }
    }
}
