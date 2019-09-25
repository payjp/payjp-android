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
package jp.pay.android

import android.os.Handler
import android.os.Looper
import android.util.Base64
import jp.pay.android.model.AcceptedBrandsResponse
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.network.createApiClient
import jp.pay.android.plugin.CardScannerResolver
import java.nio.charset.Charset
import java.util.concurrent.Executor

/**
 * Entry point for payjp-android
 *
 * We recommend use as singleton instance.
 */
class Payjp internal constructor(
    private val configuration: PayjpConfiguration,
    private val payjpApi: PayjpApi
) : PayjpTokenService {

    constructor(configuration: PayjpConfiguration) : this(
        configuration = configuration,
        payjpApi = createApiClient(
            baseUrl = PayjpConstants.API_ENDPOINT,
            debuggable = configuration.debugEnabled,
            callbackExecutor = MainThreadExecutor(),
            locale = configuration.locale
        )
    ) {
        CardScannerResolver.cardScannerPlugin = configuration.cardScannerPlugin
    }

    constructor(publicKey: String) : this(PayjpConfiguration.Builder(publicKey).build())

    companion object {
        private val factory = SingletonFactory<Payjp>()

        /**
         * Get singleton instance.
         * You must call `Payjp#init(String publicKey)`
         * or `Payjp#init(PayjpConfiguration configuration)`
         * that create singleton instance if needed, before use it.
         * You can also manage instance yourself with initialized by constructor.
         *
         * ```
         * Payjp(PayjpConfiguration)
         * ```
         */
        @JvmStatic
        fun getInstance(): Payjp = factory.get()

        /**
         * Initialize managed-singleton instance.
         *
         * @param publicKey public API Key
         */
        @JvmStatic
        fun init(publicKey: String): Payjp {
            return init(PayjpConfiguration.Builder(publicKey).build())
        }

        /**
         * Initialize managed-singleton instance.
         *
         * @param configuration configuration for [Payjp]
         */
        @JvmStatic
        fun init(configuration: PayjpConfiguration): Payjp {
            return factory.init { Payjp(configuration) }
        }
    }

    private val authorization: String

    init {
        authorization = createAuthorization(configuration.publicKey)
    }

    override fun createToken(param: PayjpTokenParam): Task<Token> {
        return payjpApi.createToken(
            authorization = authorization,
            number = param.number,
            cvc = param.cvc,
            expMonth = param.expMonth,
            expYear = param.expYear,
            name = param.name,
            tenant = param.tenantId?.id
        )
    }

    /**
     * Obtain token from token id.
     *
     */
    override fun getToken(id: String): Task<Token> {
        return payjpApi.getToken(authorization, id)
    }

    /**
     * Get accepted brands with tenant id (for platform)
     *
     * @param tenantId tenant id (only for platformer)
     * @return task of accepted brands
     */
    override fun getAcceptedBrands(tenantId: TenantId?): Task<AcceptedBrandsResponse> {
        return payjpApi.getAcceptedBrands(authorization, tenantId?.id)
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
