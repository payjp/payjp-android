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
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import java.io.IOException
import jp.pay.android.PayjpTokenBackgroundHandler
import jp.pay.android.PayjpTokenService
import jp.pay.android.R
import jp.pay.android.Task
import jp.pay.android.TokenHandlerExecutor
import jp.pay.android.exception.PayjpThreeDSecureRequiredException
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.TenantId
import jp.pay.android.model.ThreeDSecureToken
import jp.pay.android.model.Token
import jp.pay.android.util.delegateLiveData
import jp.pay.android.verifier.ui.PayjpVerifyCardResult

internal class CardFormScreenViewModel(
    private val handle: SavedStateHandle,
    private val tokenService: PayjpTokenService,
    private val tenantId: TenantId?,
    private val errorTranslator: ErrorTranslator,
    private val tokenHandlerExecutor: TokenHandlerExecutor?
) : ViewModel(), CardFormScreenContract.Input, CardFormScreenContract.Output, LifecycleObserver {
    override val contentViewVisibility: MutableLiveData<Int> by handle.delegateLiveData(initialValue = View.GONE)
    override val errorViewVisibility: MutableLiveData<Int> by handle.delegateLiveData(initialValue = View.GONE)
    override val loadingViewVisibility: MutableLiveData<Int> by handle.delegateLiveData(initialValue = View.VISIBLE)
    override val reloadContentButtonVisibility: MutableLiveData<Int> by handle.delegateLiveData(initialValue = View.VISIBLE)
    override val submitButtonVisibility: MutableLiveData<Int> by handle.delegateLiveData(initialValue = View.VISIBLE)
    override val submitButtonProgressVisibility: MutableLiveData<Int> by handle.delegateLiveData(initialValue = View.GONE)
    override val submitButtonIsEnabled: MutableLiveData<Boolean> by handle.delegateLiveData(initialValue = false)
    override val acceptedBrands: MutableLiveData<ArrayList<CardBrand>> by handle.delegateLiveData()
    override val addCardFormCommand: MutableLiveData<ArrayList<CardBrand>> by handle.delegateLiveData()
    override val errorDialogMessage: MutableLiveData<CharSequence> by handle.delegateLiveData()
    override val errorViewText: MutableLiveData<CharSequence> by handle.delegateLiveData()
    override val success: MutableLiveData<Token> by handle.delegateLiveData()
    override val startVerifyCommand: MutableLiveData<ThreeDSecureToken> by handle.delegateLiveData()
    override val snackBarMessage: MutableLiveData<Int> by handle.delegateLiveData()
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
        if (result.isSuccess()) {
            tokenizeProcessing = true
            setSubmitButtonVisible(false)
            createTokenWithTdsToken(result.retrieveThreeDSecureToken())
        } else {
            snackBarMessage.value = R.string.payjp_card_form_message_cancel_verification
            setSubmitButtonVisible(true)
            this.tokenizeProcessing = false
        }
    }

    override fun onAddedCardForm() {
        addCardFormCommand.value = null
    }

    override fun onStartedVerify() {
        startVerifyCommand.value = null
    }

    override fun onDisplayedErrorMessage() {
        errorDialogMessage.value = null
    }

    override fun onDisplaySnackBarMessage() {
        snackBarMessage.value = null
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
                    ArrayList<CardBrand>(data.brands).let { brands ->
                        acceptedBrands.value = brands
                        addCardFormCommand.value = brands
                    }
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

    private fun createTokenWithTdsToken(token: ThreeDSecureToken) {
        fetchTokenTask = tokenService.createToken(token).also {
            enqueueTokenTask(it)
        }
    }

    private fun enqueueTokenTask(task: Task<Token>) {
        setSubmitButtonVisible(false)
        task.enqueue(object : Task.Callback<Token> {
            override fun onSuccess(data: Token) {
                postTokenHandlerOrComplete(data)
            }

            override fun onError(throwable: Throwable) {
                when (throwable) {
                    is PayjpThreeDSecureRequiredException -> {
                        startVerifyCommand.value = throwable.token
                    }
                    else -> showTokenError(throwable)
                }
            }
        })
    }

    private fun postTokenHandlerOrComplete(token: Token) {
        if (tokenHandlerExecutor == null) {
            success.value = token
            tokenizeProcessing = false
        } else {
            tokenHandlerExecutor.post(token) { status ->
                tokenizeProcessing = false
                when (status) {
                    is PayjpTokenBackgroundHandler.CardFormStatus.Complete -> {
                        success.value = token
                    }
                    is PayjpTokenBackgroundHandler.CardFormStatus.Error -> {
                        errorDialogMessage.value = status.message
                        setSubmitButtonVisible(true)
                    }
                }
            }
        }
    }

    private fun showTokenError(throwable: Throwable) {
        val message = errorTranslator.translate(throwable)
        errorDialogMessage.value = message
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
        owner: SavedStateRegistryOwner,
        private val tokenService: PayjpTokenService,
        private val tenantId: TenantId?,
        private val errorTranslator: ErrorTranslator,
        private val tokenHandlerExecutor: TokenHandlerExecutor?
    ) : AbstractSavedStateViewModelFactory(owner, null) {

        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            @Suppress("UNCHECKED_CAST")
            return CardFormScreenViewModel(
                handle = handle,
                tokenService = tokenService,
                tenantId = tenantId,
                errorTranslator = errorTranslator,
                tokenHandlerExecutor = tokenHandlerExecutor
            ) as T
        }
    }
}
