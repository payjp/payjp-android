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
package jp.pay.android.network

import com.squareup.moshi.Moshi
import java.io.IOException
import java.lang.RuntimeException
import java.util.concurrent.Executor
import jp.pay.android.Task
import jp.pay.android.exception.PayjpApiException
import jp.pay.android.model.ErrorEnvelope
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

/**
 * Custom call for wrapping response.
 *
 * @param T success type
 * @param delegate delegate call
 */
internal class ResultCall<T>(
    private val moshi: Moshi,
    private val callbackExecutor: Executor,
    private val delegate: Call<T>
) : Call<T>, Task<T> {

    private fun generateHttpError(response: Response<*>): Exception {
        return response.errorBody()?.string()
            ?.let { source ->
                moshi.adapter(ErrorEnvelope::class.java).fromJson(source)
                    ?.let {
                        PayjpApiException(
                            it.error.message, HttpException(response),
                            response.code(), it.error, source
                        )
                    }
            }
            ?: RuntimeException("unknown response", HttpException(response))
    }

    override fun run(): T = execute().body()!!

    override fun enqueue(callback: Task.Callback<T>) {
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                callback.onSuccess(response.body()!!)
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onError(t)
            }
        })
    }

    override fun execute(): Response<T> {
        return delegate.execute()
            .let { it.takeIf { it.isSuccessful } ?: throw generateHttpError(it) }
    }

    override fun enqueue(callback: Callback<T>) {
        delegate.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {

                callbackExecutor.execute {
                    when {
                        delegate.isCanceled -> callback.onFailure(
                            this@ResultCall,
                            IOException("Canceled")
                        )
                        response.isSuccessful -> callback.onResponse(this@ResultCall, response)
                        else -> callback.onFailure(this@ResultCall, generateHttpError(response))
                    }
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callbackExecutor.execute {
                    callback.onFailure(this@ResultCall, t)
                }
            }
        })
    }

    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun cancel() = delegate.cancel()

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun clone(): Call<T> = ResultCall(moshi, callbackExecutor, delegate)

    override fun request(): Request = delegate.request()
}
