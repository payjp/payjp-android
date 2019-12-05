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
package jp.pay.android.coroutine

import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.PayjpTokenParam
import jp.pay.android.PayjpTokenService
import jp.pay.android.TestStubs
import jp.pay.android.anyNullable
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.ui.widget.PayjpCardFormView
import jp.pay.android.util.Tasks
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class PayjpCoroutineExtTest {

    @Mock
    private lateinit var payjpCardFormView: PayjpCardFormView
    @Mock
    private lateinit var payjpToken: PayjpTokenService
    private val params: PayjpTokenParam = PayjpTokenParam(
        number = "4242424242424242",
        expYear = "2030",
        expMonth = "12",
        cvc = "123",
        name = null,
        tenantId = null
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun createTokenSuspend_failure() {
        val error = RuntimeException("omg")
        `when`(payjpCardFormView.createToken()).thenReturn(Tasks.failure(error))
        runBlocking {
            try {
                payjpCardFormView.createTokenSuspend()
                fail("unexpected statement")
            } catch (e: Throwable) {
                assertThat(e.message, `is`(error.message))
            }
        }
    }

    @Test
    fun createTokenSuspend_success() {
        val token = TestStubs.newToken(seed = 1)
        `when`(payjpCardFormView.createToken()).thenReturn(Tasks.success(token))
        runBlocking {
            assertThat(payjpCardFormView.createTokenSuspend().id, `is`(token.id))
        }
    }

    @Test
    fun createTokenSuspendWithParam_failure() {
        val error = RuntimeException("omg")
        `when`(payjpToken.createToken(params)).thenReturn(Tasks.failure(error))
        runBlocking {
            try {
                payjpToken.createTokenSuspend(params)
                fail("unexpected statement")
            } catch (e: Throwable) {
                assertThat(e.message, `is`(error.message))
            }
        }
    }

    @Test
    fun createTokenSuspendWithParam_success() {
        val token = TestStubs.newToken(seed = 1)
        `when`(payjpToken.createToken(params)).thenReturn(Tasks.success(token))
        runBlocking {
            assertThat(payjpToken.createTokenSuspend(params).id, `is`(token.id))
        }
    }

    @Test
    fun getTokenSuspend_failure() {
        val error = RuntimeException("omg")
        `when`(payjpToken.getToken(anyString())).thenReturn(Tasks.failure(error))
        runBlocking {
            try {
                payjpToken.getTokenSuspend("1")
                fail("unexpected statement")
            } catch (e: Throwable) {
                assertThat(e.message, `is`(error.message))
            }
        }
    }

    @Test
    fun getTokenSuspend_success() {
        val token = TestStubs.newToken(seed = 1)
        `when`(payjpToken.getToken(anyString())).thenReturn(Tasks.success(token))
        runBlocking {
            assertThat(payjpToken.getTokenSuspend("1").id, `is`(token.id))
        }
    }

    @Test
    fun getAcceptedBrandsSuspend_failure() {
        val error = RuntimeException("omg")
        `when`(payjpToken.getAcceptedBrands(anyNullable())).thenReturn(Tasks.failure(error))
        runBlocking {
            try {
                payjpToken.getAcceptedBrandsSuspend(null)
                fail("unexpected statement")
            } catch (e: Throwable) {
                assertThat(e.message, `is`(error.message))
            }
        }
    }

    @Test
    fun getAcceptedBrandsSuspend_success() {
        val brands = listOf(CardBrand.VISA)
        `when`(payjpToken.getAcceptedBrands(anyNullable()))
            .thenReturn(Tasks.success(CardBrandsAcceptedResponse(brands, true)))
        runBlocking {
            assertThat(payjpToken.getAcceptedBrandsSuspend(null).brands, `is`(brands))
        }
    }
}
