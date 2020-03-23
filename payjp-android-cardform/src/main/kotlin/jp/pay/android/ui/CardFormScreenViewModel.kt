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
import androidx.annotation.VisibleForTesting
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
import jp.pay.android.exception.PayjpRequiredTdsException
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.TenantId
import jp.pay.android.model.ThreeDSecureId
import jp.pay.android.model.Token
import jp.pay.android.util.OneOffValue
import jp.pay.android.verifier.ui.PayjpVerifyCardResult

internal class CardFormScreenViewModel(
    private val savedStateHandle: SavedStateHandle,
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
    override val startVerify: MutableLiveData<OneOffValue<ThreeDSecureId>> = MutableLiveData()
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
        val tdsId = savedStateHandle.get<String>(STATE_KEY_TDS_ID)
        if (result is PayjpVerifyCardResult.Success && tdsId != null) {
            tokenizeProcessing = true
            setSubmitButtonVisible(false)
            createTokenWithTdsId(ThreeDSecureId(identifier = tdsId))
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

    private fun createTokenWithTdsId(id: ThreeDSecureId) {
        fetchTokenTask = tokenService.createToken(id).also {
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
                    is PayjpRequiredTdsException -> {
                        startVerify.value = OneOffValue(throwable.tdsId)
                        savedStateHandle.set(STATE_KEY_TDS_ID, throwable.tdsId.identifier)
                    }
                    else -> showTokenError(throwable)
                }
            }
        })
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

    companion object {
        @VisibleForTesting
        const val STATE_KEY_TDS_ID = "tds_id"
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
                savedStateHandle = handle,
                tokenService = tokenService,
                tenantId = tenantId,
                errorTranslator = errorTranslator,
                tokenHandlerExecutor = tokenHandlerExecutor
            ) as T
        }
    }
}
