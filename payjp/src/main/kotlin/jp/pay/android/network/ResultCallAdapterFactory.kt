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
import com.squareup.moshi.Types.getRawType
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.Executor

/**
 * ResultCallAdapterFactory
 */
internal class ResultCallAdapterFactory(
    private val moshi: Moshi,
    private val callbackExecutor: Executor
) : CallAdapter.Factory() {

    private fun getResultResponseType(returnType: Type): Type {
        if (returnType !is ParameterizedType) {
            throw IllegalArgumentException(
                    "ResultCall return type must be parameterized as ResultCall<Foo>")
        }
        return getParameterUpperBound(0, returnType)
    }

    override fun get(returnType: Type, annotations: Array<out Annotation>?, retrofit: Retrofit): CallAdapter<*, *>? {
        if (getRawType(returnType) != ResultCall::class.java) {
            return null
        }
        val responseType = getResultResponseType(returnType)

        return object : CallAdapter<Any, Call<*>> {
            override fun responseType(): Type = responseType

            override fun adapt(call: Call<Any>): Call<Any> {
                return ResultCall(moshi, callbackExecutor, call)
            }
        }
    }
}