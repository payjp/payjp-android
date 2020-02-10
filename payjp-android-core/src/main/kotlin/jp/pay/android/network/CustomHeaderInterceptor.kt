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
package jp.pay.android.network

import com.squareup.moshi.Moshi
import java.util.Locale
import jp.pay.android.model.ClientInfo
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Request interceptor
 */
internal class CustomHeaderInterceptor(
    private val locale: Locale,
    client: ClientInfo,
    moshi: Moshi
) : Interceptor {

    private val userAgent = UserAgent.create(client)
    private val clientInfoJson = moshi.adapter<ClientInfo>(ClientInfo::class.java).toJson(client)

    override fun intercept(chain: Interceptor.Chain?): Response {
        val newRequest = chain!!.request().newBuilder()
            .header("user-agent", userAgent)
            .header("locale", locale.language)
            .header("X-Payjp-Client-User-Agent", clientInfoJson)
            .build()

        return chain.proceed(newRequest)
    }
}
