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
package jp.pay.android.util

import jp.pay.android.Task

object Tasks {

    fun <T> failure(e: Throwable): Task<T> {
        return object : Task<T> {
            private var executed = false

            override fun run(): T {
                executed = true
                throw e
            }

            override fun enqueue(callback: Task.Callback<T>) {
                executed = true
                callback.onError(e)
            }

            override fun isExecuted(): Boolean = executed

            override fun cancel() {}

            override fun isCanceled(): Boolean = false
        }
    }

    fun <T> success(result: T): Task<T> {
        return object : Task<T> {
            private var executed = false

            override fun run(): T {
                executed = true
                return result
            }

            override fun enqueue(callback: Task.Callback<T>) {
                executed = true
                callback.onSuccess(result)
            }

            override fun isExecuted(): Boolean = executed

            override fun isCanceled(): Boolean = false

            override fun cancel() {}
        }
    }
}