/*
 *
 * Copyright (c) 2017 PAY.JP (https://pay.jp)
 * Copyright (c) 2017 BASE, Inc. (https://binc.jp/)
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
import android.util.Base64
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import jp.pay.android.model.CardBrand
import jp.pay.android.model.DateUnixTimeJsonAdapter
import jp.pay.android.model.Token
import jp.pay.android.network.NetworkExecutorFactory
import jp.pay.android.network.ResultCallAdapterFactory
import jp.pay.android.network.UaRequestInterceptor
import jp.pay.android.network.enableTls12OnPreLollipop
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.nio.charset.Charset
import java.util.concurrent.Executor

/**
 * Entry point for payjp-android
 *
 * We recommend use as singleton instance.
 */
class PayjpToken(private val configuration: PayjpTokenConfiguration) {

    constructor(publicKey: String) : this(PayjpTokenConfiguration.Builder(publicKey).build())

    companion object {
        private val factory = PayjpTokenFactory()

        /**
         * Get singleton instance.
         * You must call `PayjpToken#init(String publicKey)`
         * or `PayjpToken#init(PayjpTokenConfiguration configuration)`
         * that create singleton instance if needed, before use it.
         * You can also manage instance yourself with initialized by constructor.
         *
         * ```
         * PayjpToken(PayjpTokenConfiguration)
         * ```
         */
        @JvmStatic
        fun getInstance(): PayjpToken = factory.get()

        /**
         * Initialize managed-singleton instance.
         *
         * @param publicKey public API Key
         */
        @JvmStatic
        fun init(publicKey: String): PayjpToken {
            return init(PayjpTokenConfiguration.Builder(publicKey).build())
        }

        /**
         * Initialize managed-singleton instance.
         *
         * @param configuration configuration for [PayjpToken]
         */
        @JvmStatic
        fun init(configuration: PayjpTokenConfiguration): PayjpToken {
            return factory.init(configuration)
        }
    }

    private val tokenApi: TokenApi
    private val authorization: String
    private val debugEnabled: Boolean

    init {
        authorization = createAuthorization(configuration.publicKey)
        debugEnabled = configuration.debugEnabled
        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(UaRequestInterceptor())
                .apply {
                    if (configuration.debugEnabled) {
                        addNetworkInterceptor(HttpLoggingInterceptor()
                                .apply { this.level = HttpLoggingInterceptor.Level.HEADERS })
                    }
                }
                .enableTls12OnPreLollipop()
                .dispatcher(Dispatcher(NetworkExecutorFactory.create()))
                .build()
        val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(CardBrand.JsonAdapter())
                .add(DateUnixTimeJsonAdapter())
                .build()
        tokenApi = Retrofit.Builder()
                .baseUrl(PayjpConstants.API_ENDPOINT)
                .client(okHttpClient)
                .addCallAdapterFactory(ResultCallAdapterFactory(moshi, MainThreadExecutor()))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(TokenApi::class.java)
    }

    /**
     * Create token from card.
     *
     */
    fun createToken(number: String,
                    cvc: String,
                    expMonth: String,
                    expYear: String): Task<Token> {
        return tokenApi.createToken(authorization, number, cvc, expMonth, expYear)
    }

    /**
     * Obtain token from token id.
     *
     */
    fun getToken(id: String): Task<Token> {
        return tokenApi.getToken(authorization, id)
    }

    private class MainThreadExecutor : Executor {
        private val handler = Handler(Looper.getMainLooper())

        override fun execute(r: Runnable) {
            handler.post(r)
        }
    }

    private fun createAuthorization(publicKey: String) =
            "$publicKey:".toByteArray(Charset.forName("UTF-8"))
            .let { data -> Base64.encodeToString(data, Base64.NO_WRAP) }
            .let { credential -> "Basic $credential" }
}
