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
import jp.pay.android.PayjpTokenService
import jp.pay.android.Task
import jp.pay.android.exception.PayjpInvalidCardFormException
import jp.pay.android.model.AcceptedBrandsResponse
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardComponentInput
import jp.pay.android.model.CardCvcInput
import jp.pay.android.model.CardExpirationInput
import jp.pay.android.model.CardHolderNameInput
import jp.pay.android.model.CardNumberInput
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.util.Tasks

internal interface CardFormViewModelOutput {
    val cardNumberInput: LiveData<CardNumberInput>
    val cardExpirationInput: LiveData<CardExpirationInput>
    val cardCvcInput: LiveData<CardCvcInput>
    val cardHolderNameInput: LiveData<CardHolderNameInput>
    val cardHolderNameEnabled: LiveData<Boolean>
    val acceptedBrands: LiveData<List<CardBrand>>
    val isValid: LiveData<Boolean>
}

internal interface CardFormViewModelInput {

    fun updateCardInput(input: CardComponentInput<out Any>)

    fun updateCardHolderNameEnabled(enabled: Boolean)

    fun createToken(): Task<Token>
}

internal class CardFormViewModel(
    private val tokenService: PayjpTokenService,
    private val tenantId: TenantId?,
    holderNameEnabledDefault: Boolean
) : ViewModel(), CardFormViewModelOutput, CardFormViewModelInput, LifecycleObserver {

    override val cardNumberInput = MutableLiveData<CardNumberInput>()
    override val cardExpirationInput = MutableLiveData<CardExpirationInput>()
    override val cardCvcInput = MutableLiveData<CardCvcInput>()
    override val cardHolderNameInput = MutableLiveData<CardHolderNameInput>()
    override val cardHolderNameEnabled = MutableLiveData<Boolean>()
    override val acceptedBrands = MutableLiveData<List<CardBrand>>()
    override val isValid: LiveData<Boolean>

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
    }

    override fun onCleared() {
        task?.cancel()
        task = null
    }

    override fun updateCardInput(input: CardComponentInput<out Any>) {
        val data = when (input) {
            is CardNumberInput -> cardNumberInput
            is CardExpirationInput -> cardExpirationInput
            is CardCvcInput -> cardCvcInput
            is CardHolderNameInput -> cardHolderNameInput
            else -> throw IllegalStateException("unknown input")
        }
        data.value = input
    }

    override fun updateCardHolderNameEnabled(enabled: Boolean) {
        this.cardHolderNameEnabled.value = enabled
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
        if (acceptedBrands.value != null) {
            task = tokenService.getAcceptedBrands(tenantId)
            task?.enqueue(object : Task.Callback<AcceptedBrandsResponse> {
                override fun onSuccess(data: AcceptedBrandsResponse) {
                    acceptedBrands.postValue(data.brands)
                }

                override fun onError(throwable: Throwable) {}
            })
        }
    }

    class Factory(
        private val tokenService: PayjpTokenService,
        private val tenantId: TenantId? = null,
        private val holderNameEnabledDefault: Boolean = true
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CardFormViewModel(tokenService, tenantId, holderNameEnabledDefault) as T
        }
    }
}