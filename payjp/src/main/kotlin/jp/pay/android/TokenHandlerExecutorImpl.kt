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
package jp.pay.android

import android.util.Log
import java.util.concurrent.CancellationException
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicReference
import jp.pay.android.PayjpTokenBackgroundHandler.CardFormStatus
import jp.pay.android.model.Token

/**
 * Default implementation of [TokenHandlerExecutor]
 *
 * @param handler handler set in [jp.pay.android.PayjpConfiguration]
 * @param backgroundExecutor executor which run handler
 * @param futureExecutor executor which run [Future.get] which backgroundExecutor submitted.
 * @param callbackExecutor executor which run callback
 */
internal class TokenHandlerExecutorImpl(
    private val handler: PayjpTokenBackgroundHandler,
    private val backgroundExecutor: ExecutorService,
    private val futureExecutor: Executor,
    private val callbackExecutor: Executor,
    private val debugEnabled: Boolean = false
) : TokenHandlerExecutor {

    private var pendingCallback: AtomicReference<(CardFormStatus) -> Unit> = AtomicReference()
    private var currentFuture: Future<Unit>? = null

    override fun post(
        token: Token,
        callback: (status: CardFormStatus) -> Unit
    ) {
        pendingCallback.set(callback)
        currentFuture = backgroundExecutor.submit<Unit> {
            val status = handler.handleTokenInBackground(token)
            callbackExecutor.execute {
                pendingCallback.get()?.invoke(status)
                pendingCallback.set(null)
            }
        }
        futureExecutor.execute {
            try {
                currentFuture?.takeIf { !it.isCancelled }?.get()
            } catch (e: Exception) {
                when (e) {
                    is CancellationException, is InterruptedException -> {
                        if (debugEnabled) {
                            Log.d(PayjpConstants.TAG_FOR_LOG, "task was canceled.")
                        }
                    }
                    else -> throw e
                }
            }
        }
    }

    override fun cancel() {
        currentFuture?.takeIf { !it.isDone && !it.isCancelled }?.cancel(true)
        pendingCallback.set(null)
    }
}
