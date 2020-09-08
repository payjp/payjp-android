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

internal object PayjpCreateTokenObserver : PayjpCreateTokenObserverService {

    private const val MS_THROTTLE_DURATION = 10000L // FIXME: change to real duration
    private val handler = Handler(Looper.getMainLooper())
    private val delayedReset = Runnable(this::reset)
    @Volatile override var status: PayjpCreateTokenStatus = PayjpCreateTokenStatus.ACCEPTABLE
        private set(value) {
            val onChange = field != value
            field = value
            if (onChange) {
                handler.post {
                    listeners.forEach { it.onChangedStatus(value) }
                }
            }
        }
    private val listeners = mutableListOf<PayjpCreateTokenObserverService.TokenRequestStatusListener>()

    override fun startRequest() {
        handler.removeCallbacks(delayedReset)
        status = PayjpCreateTokenStatus.RUNNING
    }

    override fun completeRequest() {
        status = PayjpCreateTokenStatus.THROTTLED
        handler.postDelayed(delayedReset, MS_THROTTLE_DURATION)
    }

    override fun addListener(listener: PayjpCreateTokenObserverService.TokenRequestStatusListener) {
        listeners.add(listener)
    }

    private fun reset() {
        status = PayjpCreateTokenStatus.ACCEPTABLE
    }
}
