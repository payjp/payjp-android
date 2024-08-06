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

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import jp.pay.android.PayjpTokenService
import jp.pay.android.Task
import jp.pay.android.data.PhoneNumberService
import jp.pay.android.exception.PayjpInvalidCardFormException
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.CardComponentInput
import jp.pay.android.model.CardComponentInput.CardCvcInput
import jp.pay.android.model.CardComponentInput.CardExpirationInput
import jp.pay.android.model.CardComponentInput.CardHolderNameInput
import jp.pay.android.model.CardComponentInput.CardNumberInput
import jp.pay.android.model.CardComponentInput.CardPhoneNumberInput
import jp.pay.android.model.CardExpiration
import jp.pay.android.model.CountryCode
import jp.pay.android.model.TdsAttribute
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.util.OneOffValue
import jp.pay.android.util.Tasks
import jp.pay.android.util.nonNull
import jp.pay.android.validator.CardCvcInputTransformerService
import jp.pay.android.validator.CardInputTransformer
import jp.pay.android.validator.CardNumberInputTransformerService
import jp.pay.android.validator.CardPhoneNumberInputTransformerService

/**
 * ViewModel for [PayjpCardFormFragment]
 *
 * @param tokenService service to fetch token
 * @param tenantId for platform
 * @param holderNameEnabledDefault whether enable holder name input or not
 */
@Suppress("LongParameterList", "TooManyFunctions")
internal class CardFormViewModel(
    private val tokenService: PayjpTokenService,
    private val cardNumberInputTransformer: CardNumberInputTransformerService,
    private val cardExpirationInputTransformer: CardInputTransformer<CardExpirationInput>,
    private val cardCvcInputTransformer: CardCvcInputTransformerService,
    private val cardHolderNameInputTransformer: CardInputTransformer<CardHolderNameInput>,
    private val cardEmailInputTransformer: CardInputTransformer<CardComponentInput.CardEmailInput>,
    private val cardPhoneNumberInputTransformer: CardPhoneNumberInputTransformerService,
    private val tenantId: TenantId?,
    acceptedBrandsPreset: List<CardBrand>,
    private val phoneNumberService: PhoneNumberService,
    private val tdsAttributes: List<TdsAttribute<*>>,
) : ViewModel(), CardFormViewModelOutput, CardFormViewModelInput, DefaultLifecycleObserver {

    override val cardNumberInput = MutableLiveData<CardNumberInput>()
    override val cardNumberError: LiveData<Int?>
    override val cardExpirationInput = MutableLiveData<CardExpirationInput>()
    override val cardExpirationError: LiveData<Int?>
    override val cardCvcError: LiveData<Int?>
    override val cardHolderNameInput = MutableLiveData<CardHolderNameInput>()
    override val cardHolderNameError: LiveData<Int?>
    override val cardNumberBrand: LiveData<CardBrand>
    override val cardExpiration: LiveData<CardExpiration?>
    override val isValid: LiveData<Boolean>
    override val cardNumberValid: LiveData<Boolean>
    override val cardExpirationValid: LiveData<Boolean>
    override val cardCvcInput = MutableLiveData<CardCvcInput>()
    override val cardCvcValid: LiveData<Boolean>
    override val errorFetchAcceptedBrands: MutableLiveData<OneOffValue<Throwable>> = MutableLiveData()
    override val acceptedBrands: MutableLiveData<OneOffValue<List<CardBrand>>> = MutableLiveData()
    override val showErrorImmediately = MutableLiveData<Boolean>()
    override val currentPrimaryInput: MutableLiveData<CardFormElementType> = MutableLiveData()
    override val cardEmailEnabled: Boolean
    override val cardEmailInput = MutableLiveData<CardComponentInput.CardEmailInput>()
    override val cardEmailError: LiveData<Int?>
    override val cardPhoneNumberEnabled: Boolean
    override val cardPhoneNumberCountryCode: MutableLiveData<CountryCode> = MutableLiveData()
    override val cardPhoneNumberInput: MutableLiveData<CardPhoneNumberInput> = MutableLiveData()
    override val cardPhoneNumberError: LiveData<Int?>
    private var task: Task<CardBrandsAcceptedResponse>? = null
    private val brandObserver: Observer<CardBrand>
    private val countryCodeObserver: Observer<CountryCode>

    init {
        cardNumberInputTransformer.acceptedBrands = acceptedBrandsPreset
        isValid = MediatorLiveData<Boolean>().apply {
            addSource(cardNumberInput) { value = checkValid() }
            addSource(cardExpirationInput) { value = checkValid() }
            addSource(cardCvcInput) { value = checkValid() }
            addSource(cardHolderNameInput) { value = checkValid() }
            addSource(cardEmailInput) { value = checkValid() }
            addSource(cardPhoneNumberInput) { value = checkValid() }
        }
        showErrorImmediately.value = false
        cardNumberError = cardNumberInput.map(this::retrieveError).distinctUntilChanged()
        cardExpirationError = cardExpirationInput.map(this::retrieveError).distinctUntilChanged()
        cardCvcError = cardCvcInput.map(this::retrieveError).distinctUntilChanged()
        cardHolderNameError = cardHolderNameInput.map(this::retrieveError).distinctUntilChanged()
        cardNumberBrand = cardNumberInput.map { it?.brand ?: CardBrand.UNKNOWN }
        cardExpiration = cardExpirationInput.map { it?.value }
        brandObserver = Observer {
            if (it != cardCvcInputTransformer.brand) {
                cardCvcInputTransformer.brand = it
                // If brand changed, revalidate cvc.
                forceValidate(cardCvcInput, cardCvcInputTransformer)
            }
        }
        cardNumberBrand.nonNull().observeForever(brandObserver)
        cardNumberValid = cardNumberInput.map { it.valid }
        cardExpirationValid = cardExpirationInput.map { it.valid }
        cardCvcValid = cardCvcInput.map { it.valid }
        currentPrimaryInput.value = CardFormElementType.Number
        // TDS Attributes settings
        // Email
        cardEmailEnabled = tdsAttributes.any { it is TdsAttribute.Email }
        cardEmailError = cardEmailInput.map(this::retrieveError).distinctUntilChanged()
        tdsAttributes.filterIsInstance<TdsAttribute.Email>().firstOrNull()?.preset?.let {
            cardEmailInput.value = cardEmailInputTransformer.transform(it)
        }
        // Phone Number
        cardPhoneNumberEnabled = tdsAttributes.any { it is TdsAttribute.Phone }
        tdsAttributes.filterIsInstance<TdsAttribute.Phone>().firstOrNull()?.preset?.let { (region, number) ->
            cardPhoneNumberInput.value = cardPhoneNumberInputTransformer.injectPreset(region, number)
            cardPhoneNumberInputTransformer.currentCountryCode?.let { selectCountryCode(it) }
        }
        cardPhoneNumberCountryCode.value = cardPhoneNumberCountryCode.value ?: phoneNumberService.defaultCountryCode()
        cardPhoneNumberError = cardPhoneNumberInput.map(this::retrieveError).distinctUntilChanged()
        countryCodeObserver = Observer {
            if (it != cardPhoneNumberInputTransformer.currentCountryCode) {
                // If country code changed, revalidate phone number.
                cardPhoneNumberInputTransformer.currentCountryCode = it
                forceValidate(cardPhoneNumberInput, cardPhoneNumberInputTransformer)
            }
        }
        cardPhoneNumberCountryCode.nonNull().observeForever(countryCodeObserver)
    }

    override fun onCleared() {
        task?.cancel()
        task = null
        cardNumberBrand.removeObserver(brandObserver)
        cardPhoneNumberCountryCode.removeObserver(countryCodeObserver)
    }

    override fun inputCardNumber(input: String) =
        inputComponent(input, cardNumberInput, cardNumberInputTransformer)

    override fun inputCardExpiration(input: String) =
        inputComponent(input, cardExpirationInput, cardExpirationInputTransformer)

    override fun inputCardCvc(input: String) =
        inputComponent(input, cardCvcInput, cardCvcInputTransformer)

    override fun inputCardHolderName(input: String) =
        inputComponent(input, cardHolderNameInput, cardHolderNameInputTransformer)

    override fun inputEmail(input: String) {
        inputComponent(input, cardEmailInput, cardEmailInputTransformer)
    }

    override fun selectCountryCode(countryCode: CountryCode) {
        cardPhoneNumberCountryCode.value = countryCode
    }

    override fun inputPhoneNumber(input: String) {
        inputComponent(input, cardPhoneNumberInput, cardPhoneNumberInputTransformer)
    }

    override fun validate() {
        showErrorImmediately.value = true
        forceValidate(cardNumberInput, cardNumberInputTransformer)
        forceValidate(cardExpirationInput, cardExpirationInputTransformer)
        forceValidate(cardCvcInput, cardCvcInputTransformer)
        forceValidate(cardHolderNameInput, cardHolderNameInputTransformer)
        forceValidate(cardEmailInput, cardEmailInputTransformer)
        forceValidate(cardPhoneNumberInput, cardPhoneNumberInputTransformer)
    }

    override fun createToken(): Task<Token> {
        return if (isValid.value == true) {
            tokenService.createToken(
                number = checkNotNull(cardNumberInput.value?.value),
                expMonth = checkNotNull(cardExpirationInput.value?.value).month,
                expYear = checkNotNull(cardExpirationInput.value?.value).year,
                cvc = checkNotNull(cardCvcInput.value?.value),
                name = cardHolderNameInput.value?.value,
                tenantId = tenantId,
                email = cardEmailInput.value?.value,
                phone = cardPhoneNumberInput.value?.value,
            )
        } else {
            Tasks.failure(
                PayjpInvalidCardFormException("Card form is not valid")
            )
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        fetchAcceptedBrands()
    }

    fun fetchAcceptedBrands() {
        if (cardNumberInputTransformer.acceptedBrands == null) {
            task = tokenService.getAcceptedBrands(tenantId)
            task?.enqueue(
                object : Task.Callback<CardBrandsAcceptedResponse> {
                    override fun onSuccess(data: CardBrandsAcceptedResponse) {
                        cardNumberInputTransformer.acceptedBrands = data.brands
                        acceptedBrands.value = OneOffValue(data.brands)
                    }

                    override fun onError(throwable: Throwable) {
                        errorFetchAcceptedBrands.value = OneOffValue(throwable)
                    }
                }
            )
        }
    }

    private fun checkValid() = cardNumberInput.value?.valid == true &&
        cardExpirationInput.value?.valid == true &&
        cardCvcInput.value?.valid == true &&
        cardHolderNameInput.value?.valid == true &&
        (!cardEmailEnabled || cardEmailInput.value?.valid == true) &&
        (!cardPhoneNumberEnabled || cardPhoneNumberInput.value?.valid == true)

    private fun retrieveError(input: CardComponentInput<*>?): Int? {
        return input?.errorMessage?.take(showErrorImmediately.value != true)
    }

    private fun <T : CardComponentInput<*>> inputComponent(
        input: String,
        data: MutableLiveData<T>,
        transformer: CardInputTransformer<T>
    ) {
        showErrorImmediately.value = false
        val before = data.value
        val i = transformer.transform(input)
        data.value = i
        if (i.valid && before != i && i !is CardHolderNameInput) {
            currentPrimaryInput.value = getPrimaryInput()
        }
    }

    private fun <T : CardComponentInput<*>> forceValidate(
        data: MutableLiveData<T>,
        transformer: CardInputTransformer<T>
    ) {
        data.value = transformer.transform(data.value?.input.orEmpty())
    }

    private fun getPrimaryInput(): CardFormElementType? =
        listOf(
            cardNumberInput.value to CardFormElementType.Number,
            cardExpirationInput.value to CardFormElementType.Expiration,
            cardCvcInput.value to CardFormElementType.Cvc,
            cardHolderNameInput.value to CardFormElementType.HolderName,
            cardEmailInput.value to CardFormElementType.Email,
            cardPhoneNumberInput.value to CardFormElementType.PhoneNumber,
        ).firstOrNull { it.first?.valid?.not() ?: true }?.second

    /**
     * Factory class for [CardFormViewModel]
     */
    @Suppress("LongParameterList")
    internal class Factory(
        private val tokenService: PayjpTokenService,
        private val cardNumberInputTransformer: CardNumberInputTransformerService,
        private val cardExpirationInputTransformer: CardInputTransformer<CardExpirationInput>,
        private val cardCvcInputTransformer: CardCvcInputTransformerService,
        private val cardHolderNameInputTransformer: CardInputTransformer<CardHolderNameInput>,
        private val cardEmailInputTransformer: CardInputTransformer<CardComponentInput.CardEmailInput>,
        private val cardPhoneNumberInputTransformer: CardPhoneNumberInputTransformerService,
        private val tenantId: TenantId? = null,
        private val acceptedBrands: List<CardBrand>,
        private val phoneNumberService: PhoneNumberService,
        private val tdsAttributes: List<TdsAttribute<*>>,
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CardFormViewModel(
                tokenService = tokenService,
                cardNumberInputTransformer = cardNumberInputTransformer,
                cardExpirationInputTransformer = cardExpirationInputTransformer,
                cardCvcInputTransformer = cardCvcInputTransformer,
                cardHolderNameInputTransformer = cardHolderNameInputTransformer,
                cardEmailInputTransformer = cardEmailInputTransformer,
                cardPhoneNumberInputTransformer = cardPhoneNumberInputTransformer,
                tenantId = tenantId,
                acceptedBrandsPreset = acceptedBrands,
                phoneNumberService = phoneNumberService,
                tdsAttributes = tdsAttributes,
            ) as T
        }
    }
}
