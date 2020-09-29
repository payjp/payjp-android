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
package jp.pay.android

import android.os.Handler
import android.os.Looper
import jp.pay.android.PayjpTokenOperationObserverService.TokenRequestStatusListener

internal object PayjpTokenOperationObserver : PayjpTokenOperationObserverInternal {

    private const val MS_THROTTLE_DURATION = 2000L
    private val handler = Handler(Looper.getMainLooper())
    private val makeAcceptable = Runnable { status = PayjpTokenOperationStatus.ACCEPTABLE }
    private val makeRunning = Runnable { status = PayjpTokenOperationStatus.RUNNING }
    private val makeThrottled = Runnable { status = PayjpTokenOperationStatus.THROTTLED }
    @Volatile override var status: PayjpTokenOperationStatus = PayjpTokenOperationStatus.ACCEPTABLE
        private set(value) {
            val onChange = field != value
            field = value
            if (onChange) {
                handler.post {
                    listeners.forEach { it.onChangedStatus(value) }
                }
            }
        }
    private val listeners = mutableListOf<TokenRequestStatusListener>()

    override fun startRequest() {
        handler.removeCallbacks(makeAcceptable)
        handler.removeCallbacks(makeThrottled)
        handler.post(makeRunning)
    }

    override fun completeRequest() {
        handler.post(makeThrottled)
        handler.postDelayed(makeAcceptable, MS_THROTTLE_DURATION)
    }

    override fun addListener(listener: TokenRequestStatusListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: TokenRequestStatusListener) {
        listeners.remove(listener)
    }

    override fun removeAllListeners() {
        listeners.clear()
    }
}
