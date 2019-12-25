/*
 *
 * Copyright (c) 2020 PAY, Inc.
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
package jp.pay.android.ui

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.io.IOException
import java.lang.IllegalStateException
import jp.pay.android.PayjpTokenBackgroundHandler
import jp.pay.android.PayjpTokenService
import jp.pay.android.R
import jp.pay.android.Task
import jp.pay.android.TokenHandlerExecutor
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.TenantId
import jp.pay.android.model.ThreeDSecureStatus
import jp.pay.android.model.Token
import jp.pay.android.util.OneOffValue
import jp.pay.android.verifier.ui.PayjpVerifyCardResult

internal class CardFormScreenViewModel(
    private val tokenService: PayjpTokenService,
    private val tenantId: TenantId?,
    private val errorTranslator: ErrorTranslator,
    private val tokenHandlerExecutor: TokenHandlerExecutor?
) : ViewModel(), CardFormScreenContract.Input, CardFormScreenContract.Output, LifecycleObserver {
    override val contentViewVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    override val errorViewVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    override val loadingViewVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    override val reloadContentButtonVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    override val submitButtonVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    override val submitButtonProgressVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    override val submitButtonIsEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    override val acceptedBrands: MutableLiveData<OneOffValue<List<CardBrand>>> = MutableLiveData()
    override val errorDialogMessage: MutableLiveData<OneOffValue<CharSequence>> = MutableLiveData()
    override val errorViewText: MutableLiveData<CharSequence> = MutableLiveData()
    override val success: MutableLiveData<OneOffValue<Token>> = MutableLiveData()
    override val startVerify: MutableLiveData<OneOffValue<Token>> = MutableLiveData()
    override val snackBarMessage: MutableLiveData<OneOffValue<Int>> = MutableLiveData()
    // private property
    private var fetchAcceptedBrandsTask: Task<CardBrandsAcceptedResponse>? = null
    private var createTokenTask: Task<Token>? = null
    private var fetchTokenTask: Task<Token>? = null
    private var fetchBrandsProcessing: Boolean = false
    private var tokenizeProcessing: Boolean = false

    override fun onCleared() {
        fetchAcceptedBrandsTask?.cancel()
        fetchAcceptedBrandsTask = null
        createTokenTask?.cancel()
        createTokenTask = null
        fetchTokenTask?.cancel()
        fetchTokenTask = null
        tokenHandlerExecutor?.cancel()
    }

    override fun onValidateInput(isValid: Boolean) {
        submitButtonIsEnabled.value = isValid
    }

    override fun onCreateToken(task: Task<Token>) {
        if (tokenizeProcessing) {
            return
        }
        tokenizeProcessing = true
        enqueueTokenTask(task)
        createTokenTask = task
    }

    override fun onClickReload() {
        fetchAcceptedBrands()
    }

    override fun onCompleteCardVerify(result: PayjpVerifyCardResult) {
        if (result is PayjpVerifyCardResult.Success && !result.tokenId.isNullOrEmpty()) {
            tokenizeProcessing = true
            fetchToken(checkNotNull(result.tokenId))
        } else {
            snackBarMessage.value = OneOffValue(R.string.payjp_card_form_message_cancel_verification)
            setSubmitButtonVisible(true)
            tokenizeProcessing = false
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun fetchAcceptedBrands() {
        if (acceptedBrands.value == null && !fetchBrandsProcessing) {
            fetchBrandsProcessing = true
            loadingViewVisibility.value = View.VISIBLE
            errorViewVisibility.value = View.GONE
            contentViewVisibility.value = View.GONE
            fetchAcceptedBrandsTask = tokenService.getAcceptedBrands(tenantId)
            fetchAcceptedBrandsTask?.enqueue(object : Task.Callback<CardBrandsAcceptedResponse> {
                override fun onSuccess(data: CardBrandsAcceptedResponse) {
                    acceptedBrands.value = OneOffValue(data.brands)
                    loadingViewVisibility.value = View.GONE
                    contentViewVisibility.value = View.VISIBLE
                    errorViewVisibility.value = View.GONE
                    fetchBrandsProcessing = false
                }

                override fun onError(throwable: Throwable) {
                    errorViewText.value = errorTranslator.translate(throwable)
                    reloadContentButtonVisibility.value = when (throwable) {
                        is IOException -> View.VISIBLE
                        else -> View.GONE
                    }
                    loadingViewVisibility.value = View.GONE
                    contentViewVisibility.value = View.GONE
                    errorViewVisibility.value = View.VISIBLE
                    fetchBrandsProcessing = false
                }
            })
        }
    }

    private fun fetchToken(id: String) {
        fetchTokenTask = tokenService.getToken(id).also {
            enqueueTokenTask(it, afterVerify = true)
        }
    }

    private fun enqueueTokenTask(task: Task<Token>, afterVerify: Boolean = false) {
        setSubmitButtonVisible(false)
        task.enqueue(object : Task.Callback<Token> {
            override fun onSuccess(data: Token) {
                validateThreeDSecure(data, afterVerify)
            }

            override fun onError(throwable: Throwable) {
                showTokenError(throwable)
            }
        })
    }

    private fun validateThreeDSecure(token: Token, afterVerify: Boolean) {
        if (token.card.threeDSecureStatus == ThreeDSecureStatus.UNVERIFIED) {
            // if already tried to verify, something wrong with token.
            if (afterVerify) {
                showTokenError(
                    IllegalStateException("The verification is success, but we can't find verified card.")
                )
            } else {
                startVerify.value = OneOffValue(token)
            }
        } else {
            postTokenHandlerOrComplete(token)
        }
    }

    private fun postTokenHandlerOrComplete(token: Token) {
        if (tokenHandlerExecutor == null) {
            success.value = OneOffValue(token)
            tokenizeProcessing = false
        } else {
            tokenHandlerExecutor.post(token) { status ->
                tokenizeProcessing = false
                when (status) {
                    is PayjpTokenBackgroundHandler.CardFormStatus.Complete -> {
                        success.value = OneOffValue(token)
                    }
                    is PayjpTokenBackgroundHandler.CardFormStatus.Error -> {
                        errorDialogMessage.value = OneOffValue(status.message)
                        setSubmitButtonVisible(true)
                    }
                }
            }
        }
    }

    private fun showTokenError(throwable: Throwable) {
        val message = errorTranslator.translate(throwable)
        errorDialogMessage.value = OneOffValue(message)
        setSubmitButtonVisible(true)
        tokenizeProcessing = false
    }

    private fun setSubmitButtonVisible(visible: Boolean) {
        if (visible) {
            submitButtonProgressVisibility.value = View.GONE
            submitButtonVisibility.value = View.VISIBLE
        } else {
            submitButtonVisibility.value = View.INVISIBLE
            submitButtonProgressVisibility.value = View.VISIBLE
        }
    }

    internal class Factory(
        private val tokenService: PayjpTokenService,
        private val tenantId: TenantId?,
        private val errorTranslator: ErrorTranslator,
        private val tokenHandlerExecutor: TokenHandlerExecutor?
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CardFormScreenViewModel(
                tokenService = tokenService,
                tenantId = tenantId,
                errorTranslator = errorTranslator,
                tokenHandlerExecutor = tokenHandlerExecutor
            ) as T
        }
    }
}
