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
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.net.SocketTimeoutException
import jp.pay.android.PayjpTokenBackgroundHandler
import jp.pay.android.PayjpTokenService
import jp.pay.android.R
import jp.pay.android.TestStubs
import jp.pay.android.TokenHandlerExecutor
import jp.pay.android.anyNullable
import jp.pay.android.exception.PayjpRequiredTdsException
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.TenantId
import jp.pay.android.model.ThreeDSecureId
import jp.pay.android.model.Token
import jp.pay.android.util.Tasks
import jp.pay.android.verifier.ui.PayjpVerifyCardResult
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class CardFormScreenViewModelTest {

    @Mock
    private lateinit var mockTokenService: PayjpTokenService
    @Mock
    private lateinit var mockTokenHandlerExecutor: TokenHandlerExecutor
    @Mock
    private lateinit var mockErrorTranslator: ErrorTranslator

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        tenantId: TenantId? = null,
        tokenHandlerExecutor: TokenHandlerExecutor? = null
    ) = CardFormScreenViewModel(
        handle = savedStateHandle,
        tokenService = mockTokenService,
        tenantId = tenantId,
        errorTranslator = mockErrorTranslator,
        tokenHandlerExecutor = tokenHandlerExecutor
    ).apply {
        contentViewVisibility.observeForever { }
        errorViewVisibility.observeForever { }
        loadingViewVisibility.observeForever { }
        reloadContentButtonVisibility.observeForever { }
        submitButtonVisibility.observeForever { }
        submitButtonProgressVisibility.observeForever { }
        submitButtonIsEnabled.observeForever { }
        acceptedBrands.observeForever { }
        addCardFormCommand.observeForever { }
        errorDialogMessage.observeForever { }
        errorViewText.observeForever { }
        success.observeForever { }
        startVerifyCommand.observeForever { }
        snackBarMessage.observeForever { }
    }

    @Test
    fun defaultUIState() {
        val viewModel = createViewModel()
        viewModel.run {
            assertThat(contentViewVisibility.value, `is`(View.GONE))
            assertThat(errorViewVisibility.value, `is`(View.GONE))
            assertThat(loadingViewVisibility.value, `is`(View.VISIBLE))
            assertThat(submitButtonVisibility.value, `is`(View.VISIBLE))
            assertThat(submitButtonProgressVisibility.value, `is`(View.GONE))
            assertThat(submitButtonIsEnabled.value, `is`(false))
        }
    }

    @Test
    fun skip_fetchAcceptedBrands_if_acceptedBrands_hasValue() {
        val viewModel = createViewModel()
        viewModel.acceptedBrands.value = arrayListOf(CardBrand.VISA)
        viewModel.fetchAcceptedBrands()
        verify(mockTokenService, never()).getAcceptedBrands(anyNullable())
    }

    @Test
    fun success_fetchAcceptedBrands_apply_acceptedBrands() {
        val brands = listOf(CardBrand.VISA, CardBrand.MASTER_CARD)
        val response = CardBrandsAcceptedResponse(brands, true)
        val tenantId = TenantId("ten_123")
        `when`(mockTokenService.getAcceptedBrands(anyNullable()))
            .thenReturn(Tasks.success(response))

        val viewModel = createViewModel(tenantId = tenantId)
        assertThat(viewModel.acceptedBrands.value, nullValue())
        viewModel.fetchAcceptedBrands()
        verify(mockTokenService).getAcceptedBrands(tenantId)
        viewModel.run {
            assertThat(acceptedBrands.value, `is`(brands))
            assertThat(addCardFormCommand.value, `is`(brands))
            assertThat(loadingViewVisibility.value, `is`(View.GONE))
            assertThat(errorViewVisibility.value, `is`(View.GONE))
            assertThat(contentViewVisibility.value, `is`(View.VISIBLE))
        }
    }

    @Test
    fun failure_fetchAcceptedBrands_never_apply_acceptedBrands() {
        val error = RuntimeException("omg")
        val message = "問題が発生しました"
        `when`(mockTokenService.getAcceptedBrands(anyNullable()))
            .thenReturn(Tasks.failure(error))
        `when`(mockErrorTranslator.translate(error))
            .thenReturn(message)

        val viewModel = createViewModel()
        assertThat(viewModel.acceptedBrands.value, nullValue())
        viewModel.fetchAcceptedBrands()
        verify(mockTokenService).getAcceptedBrands(null)
        viewModel.run {
            assertThat(errorViewText.value, `is`(message as CharSequence))
            assertThat(acceptedBrands.value, nullValue())
            assertThat(loadingViewVisibility.value, `is`(View.GONE))
            assertThat(errorViewVisibility.value, `is`(View.VISIBLE))
            assertThat(contentViewVisibility.value, `is`(View.GONE))
            assertThat(reloadContentButtonVisibility.value, `is`(View.GONE))
        }
    }

    @Test
    fun failure_fetchAcceptedBrands_show_reload_if_network_error() {
        val error = SocketTimeoutException("omg")
        val message = "問題が発生しました"
        `when`(mockTokenService.getAcceptedBrands(anyNullable()))
            .thenReturn(Tasks.failure(error))
        `when`(mockErrorTranslator.translate(error))
            .thenReturn(message)

        val viewModel = createViewModel()
        viewModel.fetchAcceptedBrands()
        viewModel.run {
            assertThat(reloadContentButtonVisibility.value, `is`(View.VISIBLE))
        }
    }

    @Test
    fun clear_addCardFormCommands_after_command() {
        val brands = listOf(CardBrand.VISA, CardBrand.MASTER_CARD)
        val response = CardBrandsAcceptedResponse(brands, true)
        val tenantId = TenantId("ten_123")
        `when`(mockTokenService.getAcceptedBrands(anyNullable()))
            .thenReturn(Tasks.success(response))

        val viewModel = createViewModel(tenantId = tenantId)
        viewModel.fetchAcceptedBrands()
        verify(mockTokenService).getAcceptedBrands(tenantId)
        viewModel.onAddedCardForm()
        viewModel.run {
            assertThat(acceptedBrands.value, `is`(brands))
            assertThat(addCardFormCommand.value, nullValue())
        }
    }

    @Test
    fun onValidateInput_change_submitButtonIsEnabled() {
        createViewModel().run {
            assertThat(submitButtonIsEnabled.value, `is`(false))
            onValidateInput(true)
            assertThat(submitButtonIsEnabled.value, `is`(true))
            onValidateInput(false)
            assertThat(submitButtonIsEnabled.value, `is`(false))
        }
    }

    @Test
    fun success_onCreateToken_derive_executor_post() {
        val viewModel = createViewModel(tokenHandlerExecutor = mockTokenHandlerExecutor)
        val token = TestStubs.newToken()

        viewModel.onCreateToken(Tasks.success(token))
        assertThat(viewModel.submitButtonVisibility.value, `is`(View.INVISIBLE))
        assertThat(viewModel.submitButtonProgressVisibility.value, `is`(View.VISIBLE))
        verify(mockTokenHandlerExecutor).post(anyNullable(), anyNullable())
    }

    @Test
    fun failure_onCreateToken_never_derive_executor_post() {
        val error = RuntimeException("omg")
        val message = "問題が発生しました"
        `when`(mockErrorTranslator.translate(error))
            .thenReturn(message)
        val viewModel = createViewModel(tokenHandlerExecutor = mockTokenHandlerExecutor)

        viewModel.onCreateToken(Tasks.failure(error))
        viewModel.run {
            assertThat(submitButtonVisibility.value, `is`(View.VISIBLE))
            assertThat(submitButtonProgressVisibility.value, `is`(View.GONE))
            assertThat(errorDialogMessage.value, `is`(message as CharSequence))
        }
        verify(mockTokenHandlerExecutor, never()).post(anyNullable(), anyNullable())
        verify(mockErrorTranslator).translate(error)
    }

    @Test
    fun skip_executor_post_if_executor_isNull() {
        val viewModel = createViewModel(tokenHandlerExecutor = null)
        val token = TestStubs.newToken()

        viewModel.onCreateToken(Tasks.success(token))
        viewModel.run {
            assertThat(submitButtonVisibility.value, `is`(View.INVISIBLE))
            assertThat(submitButtonProgressVisibility.value, `is`(View.VISIBLE))
            assertThat(success.value, `is`(token))
        }
    }

    @Test
    fun success_executor_post() {
        val handlerExecutor = RecordingHandlerExecutor()
        val viewModel = createViewModel(tokenHandlerExecutor = handlerExecutor)
        val token = TestStubs.newToken()

        viewModel.onCreateToken(Tasks.success(token))
        assertThat(handlerExecutor.token, `is`(token))
        handlerExecutor.callback?.invoke(PayjpTokenBackgroundHandler.CardFormStatus.Complete())
        assertThat(viewModel.success.value, `is`(token))
    }

    @Test
    fun failure_executor_post() {
        val handlerExecutor = RecordingHandlerExecutor()
        val viewModel = createViewModel(tokenHandlerExecutor = handlerExecutor)
        val token = TestStubs.newToken()
        val message = "error message"

        viewModel.onCreateToken(Tasks.success(token))
        assertThat(handlerExecutor.token, `is`(token))
        handlerExecutor.callback?.invoke(PayjpTokenBackgroundHandler.CardFormStatus.Error(message))
        viewModel.run {
            assertThat(success.value, nullValue())
            assertThat(errorDialogMessage.value, `is`(message as CharSequence))
            assertThat(submitButtonVisibility.value, `is`(View.VISIBLE))
        }
    }

    @Test
    fun clear_errorMessage_after_displayed() {
        val error = RuntimeException("omg")
        val message = "問題が発生しました"
        `when`(mockErrorTranslator.translate(error))
            .thenReturn(message)
        val viewModel = createViewModel(tokenHandlerExecutor = mockTokenHandlerExecutor)

        viewModel.onCreateToken(Tasks.failure(error))
        assertThat(viewModel.errorDialogMessage.value, `is`(message as CharSequence))
        viewModel.onDisplayedErrorMessage()
        assertThat(viewModel.errorDialogMessage.value, nullValue())
    }

    @Test
    fun start_verify_if_tds_required() {
        val viewModel = createViewModel(tokenHandlerExecutor = mockTokenHandlerExecutor)
        val tdsId = ThreeDSecureId(identifier = "tds_xxx")
        val tdsRequired = PayjpRequiredTdsException(tdsId = tdsId)
        viewModel.onCreateToken(Tasks.failure(tdsRequired))
        viewModel.run {
            assertThat(success.value, nullValue())
            assertThat(startVerifyCommand.value, `is`(tdsId))
            assertThat(submitButtonVisibility.value, `is`(View.INVISIBLE))
            assertThat(submitButtonProgressVisibility.value, `is`(View.VISIBLE))
        }
        verify(mockTokenHandlerExecutor, never()).post(anyNullable(), anyNullable())
    }

    @Test
    fun clear_start_verify_command_after_command() {
        val viewModel = createViewModel(tokenHandlerExecutor = mockTokenHandlerExecutor)
        val tdsId = ThreeDSecureId(identifier = "tds_xxx")
        val tdsRequired = PayjpRequiredTdsException(tdsId = tdsId)
        viewModel.onCreateToken(Tasks.failure(tdsRequired))
        assertThat(viewModel.startVerifyCommand.value, `is`(tdsId))
        viewModel.onStartedVerify()
        assertThat(viewModel.startVerifyCommand.value, nullValue())
    }

    @Test
    fun onCompleteCardVerify_canceled() {
        val viewModel = createViewModel(tokenHandlerExecutor = mockTokenHandlerExecutor)
        val tdsId = ThreeDSecureId(identifier = "tds_xxx")
        val tdsRequired = PayjpRequiredTdsException(tdsId = tdsId)
        viewModel.onCreateToken(Tasks.failure(tdsRequired))
        viewModel.onCompleteCardVerify(PayjpVerifyCardResult.Canceled)

        viewModel.run {
            assertThat(submitButtonVisibility.value, `is`(View.VISIBLE))
            assertThat(submitButtonProgressVisibility.value, `is`(View.GONE))
            assertThat(
                snackBarMessage.value,
                `is`(R.string.payjp_card_form_message_cancel_verification)
            )
        }
        verify(mockTokenService, never()).getToken(anyNullable())
    }

    @Test
    fun clear_snackBarMessage_after_displayed() {
        val viewModel = createViewModel(tokenHandlerExecutor = mockTokenHandlerExecutor)
        val tdsId = ThreeDSecureId(identifier = "tds_xxx")
        val tdsRequired = PayjpRequiredTdsException(tdsId = tdsId)
        viewModel.onCreateToken(Tasks.failure(tdsRequired))
        viewModel.onCompleteCardVerify(PayjpVerifyCardResult.Canceled)

        assertThat(
            viewModel.snackBarMessage.value,
            `is`(R.string.payjp_card_form_message_cancel_verification)
        )
        viewModel.onDisplaySnackBarMessage()
        assertThat(viewModel.snackBarMessage.value, nullValue())
    }

    @Test
    fun onCompleteCardVerify_success_createToken_success() {
        val tdsId = ThreeDSecureId(identifier = "tds_xxx")
        val tdsRequired = PayjpRequiredTdsException(tdsId = tdsId)
        val tokenId = "tok_xxx"
        val card = TestStubs.newCard()
        val token = TestStubs.newToken(id = tokenId, card = card)

        `when`(mockTokenService.createToken(tdsId)).thenReturn(Tasks.success(token))
        val handlerExecutor = RecordingHandlerExecutor()
        val viewModel = createViewModel(tokenHandlerExecutor = handlerExecutor)

        viewModel.onCreateToken(Tasks.failure(tdsRequired))
        viewModel.onCompleteCardVerify(PayjpVerifyCardResult.Success)

        viewModel.run {
            assertThat(submitButtonVisibility.value, `is`(View.INVISIBLE))
            assertThat(submitButtonProgressVisibility.value, `is`(View.VISIBLE))
        }
        verify(mockTokenService).createToken(tdsId)

        handlerExecutor.callback?.invoke(PayjpTokenBackgroundHandler.CardFormStatus.Complete())

        viewModel.run {
            assertThat(success.value, `is`(token))
        }
    }

    @Test
    fun onCompleteCardVerify_success_createToken_failure() {
        val tdsId = ThreeDSecureId(identifier = "tds_xxx")
        val tdsRequired = PayjpRequiredTdsException(tdsId = tdsId)
        val error = RuntimeException("omg")
        val message = "message"
        `when`(mockTokenService.createToken(tdsId)).thenReturn(Tasks.failure(error))
        `when`(mockErrorTranslator.translate(error)).thenReturn(message)
        val viewModel = createViewModel(tokenHandlerExecutor = mockTokenHandlerExecutor)

        viewModel.onCreateToken(Tasks.failure(tdsRequired))
        viewModel.onCompleteCardVerify(PayjpVerifyCardResult.Success)

        viewModel.run {
            assertThat(submitButtonVisibility.value, `is`(View.VISIBLE))
            assertThat(submitButtonProgressVisibility.value, `is`(View.GONE))
            assertThat(errorDialogMessage.value, `is`(message as CharSequence))
            assertThat(success.value, nullValue())
        }
        verify(mockTokenService).createToken(tdsId)
        verify(mockTokenHandlerExecutor, never()).post(anyNullable(), anyNullable())
    }

    @Test
    fun onCompleteCardVerify_success_tdsId_from_state() {
        val tdsId = ThreeDSecureId(identifier = "tds_xxx")
        val tokenId = "tok_xxx"
        val card = TestStubs.newCard()
        val token = TestStubs.newToken(id = tokenId, card = card)
        val savedStateHandle = SavedStateHandle()
        savedStateHandle.set(CardFormScreenViewModel::pendingTdsId.name, tdsId)

        `when`(mockTokenService.createToken(tdsId)).thenReturn(Tasks.success(token))
        val handlerExecutor = RecordingHandlerExecutor()
        val viewModel = createViewModel(
            savedStateHandle = savedStateHandle,
            tokenHandlerExecutor = handlerExecutor
        )
        viewModel.onCompleteCardVerify(PayjpVerifyCardResult.Success)

        viewModel.run {
            assertThat(submitButtonVisibility.value, `is`(View.INVISIBLE))
            assertThat(submitButtonProgressVisibility.value, `is`(View.VISIBLE))
        }
        verify(mockTokenService).createToken(tdsId)

        handlerExecutor.callback?.invoke(PayjpTokenBackgroundHandler.CardFormStatus.Complete())

        viewModel.run {
            assertThat(success.value, `is`(token))
        }
    }

    class RecordingHandlerExecutor : TokenHandlerExecutor {

        var token: Token? = null
        var callback: ((status: PayjpTokenBackgroundHandler.CardFormStatus) -> Unit)? = null

        override fun post(
            token: Token,
            callback: (status: PayjpTokenBackgroundHandler.CardFormStatus) -> Unit
        ) {
            this.token = token
            this.callback = callback
        }

        override fun cancel() {
            throw UnsupportedOperationException()
        }
    }
}
