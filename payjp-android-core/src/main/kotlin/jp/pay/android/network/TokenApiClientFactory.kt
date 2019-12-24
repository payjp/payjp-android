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
import java.util.concurrent.Executor
import jp.pay.android.PayjpApi
import jp.pay.android.PayjpLogger
import jp.pay.android.model.BundleJsonAdapter
import jp.pay.android.model.CardBrand
import jp.pay.android.model.ClientInfo
import jp.pay.android.model.DateUnixTimeJsonAdapter
import jp.pay.android.model.ThreeDSecureStatus
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * ApiClient factory
 */
internal object TokenApiClientFactory {
    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(CardBrand.JsonAdapter())
            .add(DateUnixTimeJsonAdapter())
            .add(BundleJsonAdapter())
            .add(ThreeDSecureStatus.JsonAdapter())
            .build()
    }

    fun createOkHttp(locale: Locale, debuggable: Boolean, clientInfo: ClientInfo): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(CustomHeaderInterceptor(locale, clientInfo, moshi))
            .apply {
                if (debuggable) {
                    addNetworkInterceptor(HttpLoggingInterceptor()
                        .apply { this.level = HttpLoggingInterceptor.Level.HEADERS })
                }
            }
            .let {
                OkHttpTlsHelper.enableTls12OnPreLollipop(it, PayjpLogger.get(debuggable))
            }
            .dispatcher(Dispatcher(NetworkExecutorFactory.create()))
            .build()

    fun createApiClient(
        baseUrl: String,
        debuggable: Boolean = false,
        callbackExecutor: Executor,
        locale: Locale,
        clientInfo: ClientInfo
    ): PayjpApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(createOkHttp(locale, debuggable, clientInfo))
        .addCallAdapterFactory(ResultCallAdapterFactory(moshi, callbackExecutor))
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(PayjpApi::class.java)
}
