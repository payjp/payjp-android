/*
 *
 * Copyright (c) 2021 PAY, Inc.
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
package jp.pay.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.PayjpTokenBackgroundHandler.CardFormStatus
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class TokenHandlerExecutorImplTest {

    @Mock
    private lateinit var mockHandler: PayjpTokenBackgroundHandler

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun post_invoke_handler_status_complete() {
        val complete = CardFormStatus.Complete()
        `when`(mockHandler.handleTokenInBackground(anyNullable()))
            .thenReturn(complete)
        val handlerExecutor = TokenHandlerExecutorImpl(
            handler = mockHandler,
            backgroundExecutor = CurrentThreadExecutor(),
            futureExecutor = CurrentThreadExecutor(),
            callbackExecutor = CurrentThreadExecutor()
        )
        val token = TestStubs.newToken()
        val latch = CountDownLatch(1)
        handlerExecutor.post(token) {
            assertThat(it, instanceOf(CardFormStatus.Complete::class.java))
            latch.countDown()
        }
        latch.await(500, TimeUnit.MILLISECONDS)
        verify(mockHandler).handleTokenInBackground(token)
    }

    @Test
    fun post_invoke_handler_status_error() {
        val errorMessage = "omg"
        val error = CardFormStatus.Error(errorMessage)
        `when`(mockHandler.handleTokenInBackground(anyNullable()))
            .thenReturn(error)
        val handlerExecutor = TokenHandlerExecutorImpl(
            handler = mockHandler,
            backgroundExecutor = CurrentThreadExecutor(),
            futureExecutor = CurrentThreadExecutor(),
            callbackExecutor = CurrentThreadExecutor()
        )
        val token = TestStubs.newToken()
        val latch = CountDownLatch(1)
        handlerExecutor.post(token) {
            assertThat(it, instanceOf(CardFormStatus.Error::class.java))
            assertThat((it as? CardFormStatus.Error)?.message, `is`(errorMessage as CharSequence))
            latch.countDown()
        }
        latch.await(500, TimeUnit.MILLISECONDS)
        verify(mockHandler).handleTokenInBackground(token)
    }

    @Test
    fun cancel_never_invoke_handler() {
        val complete = CardFormStatus.Complete()
        `when`(mockHandler.handleTokenInBackground(anyNullable()))
            .thenReturn(complete)
        val backgroundExecutor = ResumeCurrentThreadExecutor()
        val handlerExecutor = TokenHandlerExecutorImpl(
            handler = mockHandler,
            backgroundExecutor = backgroundExecutor,
            futureExecutor = backgroundExecutor,
            callbackExecutor = CurrentThreadExecutor()
        )
        val token = TestStubs.newToken()
        handlerExecutor.post(token) {
            fail("The callback should not be invoked after cancelled.")
        }
        handlerExecutor.cancel()
        backgroundExecutor.resume()
        verify(mockHandler, never()).handleTokenInBackground(token)
    }

    @Test
    fun cancel_never_invoke_callback() {
        val complete = CardFormStatus.Complete()
        `when`(mockHandler.handleTokenInBackground(anyNullable()))
            .thenReturn(complete)
        val backgroundExecutor = ResumeCurrentThreadExecutor()
        val handlerExecutor = TokenHandlerExecutorImpl(
            handler = mockHandler,
            backgroundExecutor = CurrentThreadExecutor(),
            futureExecutor = CurrentThreadExecutor(),
            callbackExecutor = backgroundExecutor
        )
        val token = TestStubs.newToken()
        handlerExecutor.post(token) {
            fail("The callback should not be invoked after cancelled.")
        }
        handlerExecutor.cancel()
        backgroundExecutor.resume()
        verify(mockHandler).handleTokenInBackground(token)
    }

    /**
     * Executor run in current thread.
     */
    class CurrentThreadExecutor : AbstractExecutorService() {
        @Volatile private var terminated = false

        override fun execute(r: Runnable) {
            r.run()
        }

        override fun isTerminated(): Boolean = terminated

        override fun shutdown() {
            terminated = true
        }

        override fun shutdownNow(): MutableList<Runnable> {
            return mutableListOf()
        }

        override fun isShutdown(): Boolean = terminated

        override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
            shutdown()
            return terminated
        }
    }

    class ResumeCurrentThreadExecutor : AbstractExecutorService() {
        var pending: Runnable? = null

        override fun execute(r: Runnable) {
            pending = r
        }

        fun resume() {
            pending?.run()
        }

        @Volatile private var terminated = false

        override fun isTerminated(): Boolean = terminated

        override fun shutdown() {
            terminated = true
        }

        override fun shutdownNow(): MutableList<Runnable> {
            return mutableListOf()
        }

        override fun isShutdown(): Boolean = terminated

        override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
            shutdown()
            return terminated
        }
    }
}
