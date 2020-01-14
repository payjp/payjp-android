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

import java.io.IOException

/**
 * Simple request invocation interface.
 * It is mostly the same as `Retrofit.Call`.
 */
interface Task<out T> {

    /**
     * Execute synchronously and return the result.
     *
     * @throws IOException if a problem occurred in execution.
     * @return T result
     */
    @Throws(IOException::class)
    fun run(): T

    /**
     * Run task and notify callback of result or error.
     *
     * @param callback
     */
    fun enqueue(callback: Callback<T>)

    /**
     * True if [run] or [enqueue] was executed.
     */
    fun isExecuted(): Boolean

    /**
     * Cancel the task.
     */
    fun cancel()

    /**
     * True if [cancel] was called.
     */
    fun isCanceled(): Boolean

    /**
     * Callback
     */
    interface Callback<in T> {

        /**
         * Success
         *
         * @param data result of task
         */
        fun onSuccess(data: T)

        /**
         * Error
         *
         * @param throwable error in task
         */
        fun onError(throwable: Throwable)
    }
}
