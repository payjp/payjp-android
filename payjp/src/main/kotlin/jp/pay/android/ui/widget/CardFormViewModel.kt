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

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import jp.pay.android.PayjpConstants
import jp.pay.android.PayjpTokenService
import jp.pay.android.Task
import jp.pay.android.exception.PayjpInvalidCardFormException
import jp.pay.android.model.AcceptedBrandsResponse
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardBrandDetectorService
import jp.pay.android.model.CardComponentInput
import jp.pay.android.model.CardCvcInput
import jp.pay.android.model.CardExpiration
import jp.pay.android.model.CardExpirationInput
import jp.pay.android.model.CardHolderNameInput
import jp.pay.android.model.CardNumberInput
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.util.RemappableMediatorLiveData
import jp.pay.android.util.Tasks
import jp.pay.android.validator.CardExpirationProcessorService
import jp.pay.android.validator.CardNumberValidatorService

internal interface CardFormViewModelOutput {
    val cardNumberError: LiveData<Int?>
    val cardExpirationError: LiveData<Int?>
    val cardCvcError: LiveData<Int?>
    val cardHolderNameError: LiveData<Int?>
    val cardHolderNameEnabled: LiveData<Boolean>
    val cardNumberBrand: LiveData<CardBrand>
    val cardExpiration: LiveData<CardExpiration?>
    val isValid: LiveData<Boolean>
}

internal interface CardFormViewModelInput {

    fun inputCardNumber(input: String)

    fun inputCardExpiration(input: String)

    fun inputCardCvc(input: String)

    fun inputCardHolderName(input: String)

    fun updateCardHolderNameEnabled(enabled: Boolean)

    fun validate()

    fun createToken(): Task<Token>
}

/**
 * ViewModel for [CardFormFragment]
 *
 * @param tokenService service to fetch token
 * @param tenantId for platform
 * @param holderNameEnabledDefault whether enable holder name input or not
 */
internal class CardFormViewModel(
    private val tokenService: PayjpTokenService,
    private val brandDetector: CardBrandDetectorService,
    private val cardNumberValidator: CardNumberValidatorService,
    private val cardExpirationProcessor: CardExpirationProcessorService,
    private val tenantId: TenantId?,
    private val cardExpirationDelimiter: Char,
    holderNameEnabledDefault: Boolean
) : ViewModel(), CardFormViewModelOutput, CardFormViewModelInput, LifecycleObserver {

    override val cardNumberError: RemappableMediatorLiveData<CardNumberInput, Int?>
    override val cardExpirationError: RemappableMediatorLiveData<CardExpirationInput, Int?>
    override val cardCvcError: RemappableMediatorLiveData<CardCvcInput, Int?>
    override val cardHolderNameError: RemappableMediatorLiveData<CardHolderNameInput, Int?>
    override val cardHolderNameEnabled = MutableLiveData<Boolean>()
    override val cardNumberBrand = MutableLiveData<CardBrand>()
    override val cardExpiration: LiveData<CardExpiration?>
    override val isValid: LiveData<Boolean>

    private val cardNumberInput = MutableLiveData<CardNumberInput>()
    private val cardExpirationInput = MutableLiveData<CardExpirationInput>()
    private val cardCvcInput = MutableLiveData<CardCvcInput>()
    private val cardHolderNameInput = MutableLiveData<CardHolderNameInput>()
    private val acceptedBrands = MutableLiveData<List<CardBrand>>()
    private val showErrorImmediately = MutableLiveData<Boolean>()
    private var task: Task<AcceptedBrandsResponse>? = null

    init {
        cardHolderNameEnabled.value = holderNameEnabledDefault
        isValid = MediatorLiveData<Boolean>().apply {
            fun checkValid() = cardNumberInput.value?.valid == true &&
                cardExpirationInput.value?.valid == true &&
                cardCvcInput.value?.valid == true &&
                (cardHolderNameEnabled.value == false || cardHolderNameInput.value?.valid == true)
            addSource(cardNumberInput) { value = checkValid() }
            addSource(cardExpirationInput) { value = checkValid() }
            addSource(cardCvcInput) { value = checkValid() }
            addSource(cardHolderNameInput) { value = checkValid() }
            addSource(cardHolderNameEnabled) { value = checkValid() }
        }
        showErrorImmediately.value = false
        cardNumberError = RemappableMediatorLiveData(cardNumberInput, this::retrieveError)
        cardExpirationError = RemappableMediatorLiveData(cardExpirationInput, this::retrieveError)
        cardCvcError = RemappableMediatorLiveData(cardCvcInput, this::retrieveError)
        cardHolderNameError = RemappableMediatorLiveData(cardHolderNameInput, this::retrieveError)
        cardExpiration = cardExpirationInput.map { it?.value }
    }

    override fun onCleared() {
        task?.cancel()
        task = null
    }

    override fun inputCardNumber(input: String) {
        showErrorImmediately.value = false
        cardNumberInput.value = createCardNumberInput(input)
    }

    override fun inputCardExpiration(input: String) {
        showErrorImmediately.value = false
        cardExpirationInput.value = createCardExpirationInput(input)
    }

    override fun inputCardCvc(input: String) {
        showErrorImmediately.value = false
        cardCvcInput.value = createCardCvcInput(input)
    }

    override fun inputCardHolderName(input: String) {
        showErrorImmediately.value = false
        cardHolderNameInput.value = createCardHolderNameInput(input)
    }

    override fun updateCardHolderNameEnabled(enabled: Boolean) {
        showErrorImmediately.value = false
        this.cardHolderNameEnabled.value = enabled
    }

    override fun validate() {
        showErrorImmediately.value = true
        // if no input, input empty string
        fun <T: CardComponentInput<*>> forceValidate(
            input: MutableLiveData<T>,
            error: RemappableMediatorLiveData<T, Int?>,
            inputFactory: (i: String) -> T
        ) {
            if (input.value == null) {
                input.value = inputFactory("")
            } else {
                error.remap()
            }
        }
        forceValidate(cardNumberInput, cardNumberError, this::createCardNumberInput)
        forceValidate(cardExpirationInput, cardExpirationError, this::createCardExpirationInput)
        forceValidate(cardCvcInput, cardCvcError, this::createCardCvcInput)
        forceValidate(cardHolderNameInput, cardHolderNameError, this::createCardHolderNameInput)
    }

    override fun createToken(): Task<Token> {
        return if (isValid.value == true) {
            val name = if (cardHolderNameEnabled.value == true) {
                cardHolderNameInput.value?.value
            } else {
                null
            }
            tokenService.createToken(
                number = checkNotNull(cardNumberInput.value?.value),
                expMonth = checkNotNull(cardExpirationInput.value?.value).month,
                expYear = checkNotNull(cardExpirationInput.value?.value).year,
                cvc = checkNotNull(cardCvcInput.value?.value),
                name = name
            )
        } else {
            Tasks.failure(
                PayjpInvalidCardFormException("Card form is not valid")
            )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun fetchAcceptedBrands() {
        if (acceptedBrands.value == null) {
            task = tokenService.getAcceptedBrands(tenantId)
            task?.enqueue(object : Task.Callback<AcceptedBrandsResponse> {
                override fun onSuccess(data: AcceptedBrandsResponse) {
                    acceptedBrands.postValue(data.brands)
                }

                override fun onError(throwable: Throwable) {}
            })
        }
    }

    private fun retrieveError(input: CardComponentInput<*>?): Int? {
        return input?.errorMessage?.take(showErrorImmediately.value != true)
    }

    private fun createCardNumberInput(input: String) = CardNumberInput(
        input = input,
        acceptedBrands = acceptedBrands.value,
        brandDetector = brandDetector,
        cardNumberValidator = cardNumberValidator)

    private fun createCardExpirationInput(input: String) = CardExpirationInput(
        input = input,
        delimiter = cardExpirationDelimiter,
        processor = cardExpirationProcessor)

    private fun createCardCvcInput(input: String) = CardCvcInput(input)

    private fun createCardHolderNameInput(input: String) = CardHolderNameInput(input)

    /**
     * Factory class for [CardFormViewModel]
     */
    internal class Factory(
        private val tokenService: PayjpTokenService,
        private val brandDetector: CardBrandDetectorService,
        private val cardNumberValidator: CardNumberValidatorService,
        private val cardExpirationProcessor: CardExpirationProcessorService,
        private val tenantId: TenantId? = null,
        private val cardExpirationDelimiter: Char = PayjpConstants.CARD_FORM_DELIMITER_EXPIRATION,
        private val holderNameEnabledDefault: Boolean = true
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CardFormViewModel(
                tokenService = tokenService,
                brandDetector = brandDetector,
                cardNumberValidator = cardNumberValidator,
                cardExpirationProcessor = cardExpirationProcessor,
                tenantId = tenantId,
                cardExpirationDelimiter = cardExpirationDelimiter,
                holderNameEnabledDefault = holderNameEnabledDefault) as T
        }
    }
}