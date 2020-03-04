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

import android.util.Log

sealed class PayjpLogger {

    companion object {
        fun get(debugEnabled: Boolean): PayjpLogger = if (debugEnabled) Debug else None
    }

    open fun debuggable(): Boolean = false

    open fun v(message: String?) {}

    open fun d(message: String?) {}

    open fun i(message: String?) {}

    open fun w(message: String?) {}

    open fun w(message: String?, throwable: Throwable?) {}

    open fun e(message: String?) {}

    open fun e(message: String?, throwable: Throwable?) {}

    object None : PayjpLogger()

    object Debug : PayjpLogger() {
        private const val TAG: String = "payjp-android"

        override fun debuggable(): Boolean = true

        override fun v(message: String?) {
            Log.v(TAG, message)
        }

        override fun d(message: String?) {
            Log.d(TAG, message)
        }

        override fun i(message: String?) {
            Log.i(TAG, message)
        }

        override fun w(message: String?) {
            Log.w(TAG, message)
        }

        override fun w(message: String?, throwable: Throwable?) {
            Log.w(TAG, message, throwable)
        }

        override fun e(message: String?) {
            Log.e(TAG, message)
        }

        override fun e(message: String?, throwable: Throwable?) {
            Log.e(TAG, message, throwable)
        }
    }
}
