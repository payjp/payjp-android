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
package jp.pay.android.ui

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.pay.android.PayjpTokenBackgroundHandler
import jp.pay.android.PayjpTokenService
import jp.pay.android.Task
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.util.OneOffValue

internal class CardFormScreenViewModel(
    private val tokenService: PayjpTokenService,
    private val tenantId: TenantId?
) : ViewModel(), CardFormScreenContract.Input, CardFormScreenContract.Output, LifecycleObserver {
    override val contentViewVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    override val errorViewVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    override val loadingViewVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    override val submitButtonVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    override val submitButtonProgressVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    override val submitButtonIsEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    override val acceptedBrands: MutableLiveData<OneOffValue<List<CardBrand>>> = MutableLiveData()
    override val errorMessage: MutableLiveData<OneOffValue<CharSequence>> = MutableLiveData()
    override val success: MutableLiveData<OneOffValue<Token>> = MutableLiveData()
    // private property
    private var fetchAcceptedBrandsTask: Task<CardBrandsAcceptedResponse>? = null
    private var createTokenTask: Task<Token>? = null
    private var fetchBrandsProcessing: MutableLiveData<Boolean> = MutableLiveData(false)
    private var tokenizeProcessing: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun onCleared() {
        fetchAcceptedBrandsTask?.cancel()
        fetchAcceptedBrandsTask = null
        createTokenTask?.cancel()
        createTokenTask = null
        tokenService.getTokenHandlerExecutor()?.cancel()
    }

    override fun onValidateInput(isValid: Boolean) {
        submitButtonIsEnabled.value = isValid
    }

    override fun onCreateToken(task: Task<Token>) {
        if (tokenizeProcessing.value == true) {
            return
        }
        tokenizeProcessing.value = true
        submitButtonVisibility.value = View.INVISIBLE
        submitButtonProgressVisibility.value = View.VISIBLE
        task.enqueue(object : Task.Callback<Token> {
            override fun onSuccess(data: Token) {
                onSuccessCreateToken(data)
            }

            override fun onError(throwable: Throwable) {
                // TODO: エラーメッセージ
                errorMessage.value = OneOffValue("問題が発生しました")
                submitButtonProgressVisibility.value = View.GONE
                submitButtonVisibility.value = View.VISIBLE
                tokenizeProcessing.value = false
            }
        })
        createTokenTask = task
    }

    override fun onClickReload() {
        fetchAcceptedBrands()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun fetchAcceptedBrands() {
        if (acceptedBrands.value == null && fetchBrandsProcessing.value == false) {
            fetchBrandsProcessing.value = true
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
                    fetchBrandsProcessing.value = false
                }

                override fun onError(throwable: Throwable) {
                    loadingViewVisibility.value = View.GONE
                    contentViewVisibility.value = View.GONE
                    errorViewVisibility.value = View.VISIBLE
                    fetchBrandsProcessing.value = false
                }
            })
        }
    }

    private fun onSuccessCreateToken(token: Token) {
        val executor = tokenService.getTokenHandlerExecutor()
        if (executor == null) {
            submitButtonProgressVisibility.value = View.GONE
            success.value = OneOffValue(token)
            tokenizeProcessing.value = false
        } else {
            executor.post(token) { status ->
                submitButtonProgressVisibility.value = View.GONE
                tokenizeProcessing.value = false
                when (status) {
                    is PayjpTokenBackgroundHandler.CardFormStatus.Complete -> {
                        success.value = OneOffValue(token)
                    }
                    is PayjpTokenBackgroundHandler.CardFormStatus.Error -> {
                        errorMessage.value = OneOffValue(status.message)
                        submitButtonVisibility.value = View.VISIBLE
                    }
                }
            }
        }
    }

    internal class Factory(
        private val tokenService: PayjpTokenService,
        private val tenantId: TenantId?
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CardFormScreenViewModel(
                tokenService = tokenService,
                tenantId = tenantId
            ) as T
        }
    }
}
