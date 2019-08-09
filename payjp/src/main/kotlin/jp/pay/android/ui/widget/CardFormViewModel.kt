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

import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import jp.pay.android.PayjpTokenService
import jp.pay.android.Task
import jp.pay.android.exception.PayjpInvalidCardFormException
import jp.pay.android.model.AcceptedBrandsResponse
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardComponentInput
import jp.pay.android.model.CardComponentInput.CardCvcInput
import jp.pay.android.model.CardComponentInput.CardExpirationInput
import jp.pay.android.model.CardComponentInput.CardHolderNameInput
import jp.pay.android.model.CardComponentInput.CardNumberInput
import jp.pay.android.model.CardExpiration
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.util.RemappableMediatorLiveData
import jp.pay.android.util.Tasks
import jp.pay.android.validator.CardCvcInputTransformerService
import jp.pay.android.validator.CardInputTransformer
import jp.pay.android.validator.CardNumberInputTransformerService

internal interface CardFormViewModelOutput {
    val cardNumberError: LiveData<Int?>
    val cardExpirationError: LiveData<Int?>
    val cardCvcError: LiveData<Int?>
    val cardHolderNameError: LiveData<Int?>
    val cardHolderNameEnabled: LiveData<Boolean>
    val cvcImeOptions: LiveData<Int>
    val cardNumberBrand: LiveData<CardBrand>
    val cardExpiration: LiveData<CardExpiration?>
    val isValid: LiveData<Boolean>
    val cardNumberValid: LiveData<Boolean>
    val cardExpirationValid: LiveData<Boolean>
    val cardCvcValid: LiveData<Boolean>
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
 * ViewModel for [PayjpCardFormFragment]
 *
 * @param tokenService service to fetch token
 * @param tenantId for platform
 * @param holderNameEnabledDefault whether enable holder name input or not
 */
internal class CardFormViewModel(
    private val tokenService: PayjpTokenService,
    private val cardNumberInputTransformer: CardNumberInputTransformerService,
    private val cardExpirationInputTransformer: CardInputTransformer<CardExpirationInput>,
    private val cardCvcInputTransformer: CardCvcInputTransformerService,
    private val cardHolderNameInputTransformer: CardInputTransformer<CardHolderNameInput>,
    private val tenantId: TenantId?,
    holderNameEnabledDefault: Boolean
) : ViewModel(), CardFormViewModelOutput, CardFormViewModelInput, LifecycleObserver {

    override val cardNumberError: RemappableMediatorLiveData<CardNumberInput, Int?>
    override val cardExpirationError: RemappableMediatorLiveData<CardExpirationInput, Int?>
    override val cardCvcError: RemappableMediatorLiveData<CardCvcInput, Int?>
    override val cardHolderNameError: RemappableMediatorLiveData<CardHolderNameInput, Int?>
    override val cardHolderNameEnabled = MutableLiveData<Boolean>()
    override val cvcImeOptions: LiveData<Int>
    override val cardNumberBrand: LiveData<CardBrand>
    override val cardExpiration: LiveData<CardExpiration?>
    override val isValid: LiveData<Boolean>
    override val cardNumberValid: LiveData<Boolean>
    override val cardExpirationValid: LiveData<Boolean>
    override val cardCvcValid: LiveData<Boolean>

    private val cardNumberInput = MutableLiveData<CardNumberInput>()
    private val cardExpirationInput = MutableLiveData<CardExpirationInput>()
    private val cardCvcInput = MutableLiveData<CardCvcInput>()
    private val cardHolderNameInput = MutableLiveData<CardHolderNameInput>()
    private val showErrorImmediately = MutableLiveData<Boolean>()
    private var task: Task<AcceptedBrandsResponse>? = null
    private val brandObserver: Observer<CardBrand>

    init {
        cardHolderNameEnabled.value = holderNameEnabledDefault
        cvcImeOptions = cardHolderNameEnabled.map {
            if (it) {
                EditorInfo.IME_ACTION_NEXT
            } else {
                EditorInfo.IME_ACTION_DONE
            }
        }
        isValid = MediatorLiveData<Boolean>().apply {
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
        cardNumberBrand = cardNumberInput.map { it?.brand ?: CardBrand.UNKNOWN }
        cardExpiration = cardExpirationInput.map { it?.value }
        brandObserver = Observer {
            if (it != cardCvcInputTransformer.brand) {
                cardCvcInputTransformer.brand = it
                // If brand changed, revalidate cvc.
                forceValidate(cardCvcInput, cardCvcInputTransformer)
            }
        }
        cardNumberBrand.observeForever(brandObserver)
        cardNumberValid = cardNumberInput.map { it.valid }
        cardExpirationValid = cardExpirationInput.map { it.valid }
        cardCvcValid = cardCvcInput.map { it.valid }
    }

    override fun onCleared() {
        task?.cancel()
        task = null
        cardNumberBrand.removeObserver(brandObserver)
    }

    override fun inputCardNumber(input: String) =
        inputComponent(input, cardNumberInput, cardNumberInputTransformer)

    override fun inputCardExpiration(input: String) =
        inputComponent(input, cardExpirationInput, cardExpirationInputTransformer)

    override fun inputCardCvc(input: String) =
        inputComponent(input, cardCvcInput, cardCvcInputTransformer)

    override fun inputCardHolderName(input: String) =
        inputComponent(input, cardHolderNameInput, cardHolderNameInputTransformer)

    override fun updateCardHolderNameEnabled(enabled: Boolean) {
        showErrorImmediately.value = false
        this.cardHolderNameEnabled.value = enabled
    }

    override fun validate() {
        showErrorImmediately.value = true
        forceValidate(cardNumberInput, cardNumberInputTransformer)
        forceValidate(cardExpirationInput, cardExpirationInputTransformer)
        forceValidate(cardCvcInput, cardCvcInputTransformer)
        forceValidate(cardHolderNameInput, cardHolderNameInputTransformer)
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
                name = name,
                tenantId = tenantId
            )
        } else {
            Tasks.failure(
                PayjpInvalidCardFormException("Card form is not valid")
            )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun fetchAcceptedBrands() {
        if (cardNumberInputTransformer.acceptedBrands == null) {
            task = tokenService.getAcceptedBrands(tenantId)
            task?.enqueue(object : Task.Callback<AcceptedBrandsResponse> {
                override fun onSuccess(data: AcceptedBrandsResponse) {
                    cardNumberInputTransformer.acceptedBrands = data.brands
                }

                override fun onError(throwable: Throwable) {}
            })
        }
    }

    private fun checkValid() = cardNumberInput.value?.valid == true &&
        cardExpirationInput.value?.valid == true &&
        cardCvcInput.value?.valid == true &&
        (cardHolderNameEnabled.value == false || cardHolderNameInput.value?.valid == true)

    private fun retrieveError(input: CardComponentInput<*>?): Int? {
        return input?.errorMessage?.take(showErrorImmediately.value != true)
    }

    private fun <T : CardComponentInput<*>> inputComponent(
        input: String,
        data: MutableLiveData<T>,
        transformer: CardInputTransformer<T>
    ) {
        showErrorImmediately.value = false
        data.value = transformer.transform(input)
    }

    private fun <T : CardComponentInput<*>> forceValidate(
        data: MutableLiveData<T>,
        transformer: CardInputTransformer<T>
    ) {
        data.value = transformer.transform(data.value?.input.orEmpty())
    }

    /**
     * Factory class for [CardFormViewModel]
     */
    internal class Factory(
        private val tokenService: PayjpTokenService,
        private val cardNumberInputTransformer: CardNumberInputTransformerService,
        private val cardExpirationInputTransformer: CardInputTransformer<CardExpirationInput>,
        private val cardCvcInputTransformer: CardCvcInputTransformerService,
        private val cardHolderNameInputTransformer: CardInputTransformer<CardHolderNameInput>,
        private val tenantId: TenantId? = null,
        private val holderNameEnabledDefault: Boolean = true
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CardFormViewModel(
                tokenService = tokenService,
                cardNumberInputTransformer = cardNumberInputTransformer,
                cardExpirationInputTransformer = cardExpirationInputTransformer,
                cardCvcInputTransformer = cardCvcInputTransformer,
                cardHolderNameInputTransformer = cardHolderNameInputTransformer,
                tenantId = tenantId,
                holderNameEnabledDefault = holderNameEnabledDefault) as T
        }
    }
}