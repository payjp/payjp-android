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

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Base64
import androidx.fragment.app.Fragment
import java.nio.charset.Charset
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.network.TokenApiClientFactory.createApiClient
import jp.pay.android.plugin.CardScannerResolver
import jp.pay.android.ui.PayjpCardFormActivity
import jp.pay.android.ui.PayjpCardFormResultCallback

/**
 * Entry point for payjp-android
 *
 * We recommend use as singleton instance.
 *
 * @param configuration configuration
 * @param payjpApi api
 * @constructor create new Payjp instance.
 */
class Payjp internal constructor(
    private val configuration: PayjpConfiguration,
    private val payjpApi: PayjpApi,
    private val tokenHandlerExecutor: TokenHandlerExecutor? = null
) : PayjpTokenService {

    /**
     * Constructor for configuration.
     *
     * @param configuration create with configuration
     */
    constructor(configuration: PayjpConfiguration) : this(
        configuration = configuration,
        payjpApi = createApiClient(
            baseUrl = PayjpConstants.API_ENDPOINT,
            debuggable = configuration.debugEnabled,
            callbackExecutor = MainThreadExecutor,
            locale = configuration.locale
        ),
        tokenHandlerExecutor = configuration.tokenBackgroundHandler?.let { handler ->
            TokenHandlerExecutorImpl(
                handler = handler,
                backgroundExecutor = Executors.newSingleThreadExecutor { r ->
                    Thread(r, "payjp-android").apply {
                        priority = Thread.MIN_PRIORITY
                    }
                },
                callbackExecutor = MainThreadExecutor
            )
        }
    ) {
        CardScannerResolver.cardScannerPlugin = configuration.cardScannerPlugin
    }

    /**
     * Constructor for publicKey
     *
     * @param publicKey PAY.JP public key.
     */
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
         * @param publicKey public API Key. You must **NOT** use secret key.
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

        /**
         * Start card form screen from Activity.
         *
         * @param activity activity
         * @param requestCode requestCode. The default is [PayjpCardFormActivity.DEFAULT_CARD_FORM_REQUEST_CODE]
         * @param tenant tenant (only for platformer)
         */
        @JvmStatic
        @JvmOverloads
        fun startCardForm(activity: Activity, requestCode: Int? = null, tenant: TenantId? = null) =
            PayjpCardFormActivity.start(activity = activity, requestCode = requestCode, tenant = tenant)

        /**
         * Start card form screen from Fragment.
         *
         * @param fragment fragment
         * @param requestCode requestCode. The default is [PayjpCardFormActivity.DEFAULT_CARD_FORM_REQUEST_CODE]
         * @param tenant tenant (only for platformer)
         */
        @JvmStatic
        @JvmOverloads
        fun startCardForm(fragment: Fragment, requestCode: Int? = null, tenant: TenantId? = null) =
            PayjpCardFormActivity.start(fragment = fragment, requestCode = requestCode, tenant = tenant)

        /**
         * Handle the result from the activity which is started by [Payjp.startCardForm].
         * Use this at [Activity.onActivityResult] in the activity that you call [Payjp.startCardForm].
         *
         * @param data the intent from [Activity.onActivityResult].
         * @param callback you can get card token if it is success.
         */
        @JvmStatic
        fun handleCardFormResult(data: Intent?, callback: PayjpCardFormResultCallback) =
            PayjpCardFormActivity.onActivityResult(data = data, callback = callback)
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
    override fun getAcceptedBrands(tenantId: TenantId?): Task<CardBrandsAcceptedResponse> {
        return payjpApi.getAcceptedBrands(authorization, tenantId?.id)
    }

    /**
     * Get token background handler executor.
     * If any handler did not set, return null.
     *
     * @return background handler executor
     */
    internal fun getTokenHandlerExecutor(): TokenHandlerExecutor? = tokenHandlerExecutor

    private object MainThreadExecutor : Executor {
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
