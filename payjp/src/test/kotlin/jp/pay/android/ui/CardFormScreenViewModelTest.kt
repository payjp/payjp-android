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
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.net.SocketTimeoutException
import jp.pay.android.PayjpTokenBackgroundHandler
import jp.pay.android.PayjpTokenHandlerExecutor
import jp.pay.android.PayjpTokenService
import jp.pay.android.TestStubs
import jp.pay.android.anyNullable
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.util.OneOffValue
import jp.pay.android.util.Tasks
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
    private lateinit var mockTokenHandlerExecutor: PayjpTokenHandlerExecutor
    @Mock
    private lateinit var mockErrorTranslator: ErrorTranslator

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    private fun createViewModel(
        tenantId: TenantId? = null
    ) = CardFormScreenViewModel(
        tokenService = mockTokenService,
        tenantId = tenantId,
        errorTranslator = mockErrorTranslator

    ).apply {
        contentViewVisibility.observeForever { }
        errorViewVisibility.observeForever { }
        loadingViewVisibility.observeForever { }
        reloadContentButtonVisibility.observeForever { }
        submitButtonVisibility.observeForever { }
        submitButtonProgressVisibility.observeForever { }
        submitButtonIsEnabled.observeForever { }
        acceptedBrands.observeForever { }
        errorDialogMessage.observeForever { }
        errorViewText.observeForever { }
        success.observeForever { }
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
        viewModel.acceptedBrands.value = OneOffValue(listOf(CardBrand.VISA))
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

        val viewModel = createViewModel(tenantId)
        assertThat(viewModel.acceptedBrands.value, nullValue())
        viewModel.fetchAcceptedBrands()
        verify(mockTokenService).getAcceptedBrands(tenantId)
        viewModel.run {
            assertThat(acceptedBrands.value?.peek(), `is`(brands))
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
            assertThat(acceptedBrands.value?.peek(), nullValue())
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
        `when`(mockTokenService.getTokenHandlerExecutor())
            .thenReturn(mockTokenHandlerExecutor)
        val viewModel = createViewModel()
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
        `when`(mockTokenService.getTokenHandlerExecutor())
            .thenReturn(mockTokenHandlerExecutor)
        `when`(mockErrorTranslator.translate(error))
            .thenReturn(message)
        val viewModel = createViewModel()

        viewModel.onCreateToken(Tasks.failure(error))
        viewModel.run {
            assertThat(submitButtonVisibility.value, `is`(View.VISIBLE))
            assertThat(submitButtonProgressVisibility.value, `is`(View.GONE))
            assertThat(errorDialogMessage.value?.peek(), `is`(message as CharSequence))
        }
        verify(mockTokenHandlerExecutor, never()).post(anyNullable(), anyNullable())
        verify(mockErrorTranslator).translate(error)
    }

    @Test
    fun skip_executor_post_if_executor_isNull() {
        `when`(mockTokenService.getTokenHandlerExecutor())
            .thenReturn(null)

        val viewModel = createViewModel()
        val token = TestStubs.newToken()

        viewModel.onCreateToken(Tasks.success(token))
        viewModel.run {
            assertThat(submitButtonVisibility.value, `is`(View.INVISIBLE))
            assertThat(submitButtonProgressVisibility.value, `is`(View.GONE))
            assertThat(success.value?.peek(), `is`(token))
        }
    }

    @Test
    fun success_executor_post() {
        val handlerExecutor = RecordingHandlerExecutor()
        `when`(mockTokenService.getTokenHandlerExecutor())
            .thenReturn(handlerExecutor)
        val viewModel = createViewModel()
        val token = TestStubs.newToken()

        viewModel.onCreateToken(Tasks.success(token))
        assertThat(handlerExecutor.token, `is`(token))
        handlerExecutor.callback?.invoke(PayjpTokenBackgroundHandler.CardFormStatus.Complete())
        assertThat(viewModel.success.value?.peek(), `is`(token))
    }

    @Test
    fun failure_executor_post() {
        val handlerExecutor = RecordingHandlerExecutor()
        `when`(mockTokenService.getTokenHandlerExecutor())
            .thenReturn(handlerExecutor)
        val viewModel = createViewModel()
        val token = TestStubs.newToken()
        val message = "error message"

        viewModel.onCreateToken(Tasks.success(token))
        assertThat(handlerExecutor.token, `is`(token))
        handlerExecutor.callback?.invoke(PayjpTokenBackgroundHandler.CardFormStatus.Error(message))
        viewModel.run {
            assertThat(success.value?.peek(), nullValue())
            assertThat(errorDialogMessage.value?.peek(), `is`(message as CharSequence))
            assertThat(submitButtonVisibility.value, `is`(View.VISIBLE))
        }
    }

    class RecordingHandlerExecutor : PayjpTokenHandlerExecutor {

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
