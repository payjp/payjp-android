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

import java.util.Locale
import java.util.concurrent.Executor
import jp.pay.android.model.ClientInfo
import jp.pay.android.plugin.CardScannerPlugin
import jp.pay.android.util.MainThreadExecutor

/**
 * Configuration for Payjp
 *
 * use `Configuration.Builder`
 *
 * @param publicKey public key `pk_xxxxxxxxxxxxxxxxx`
 * @param debugEnabled is debug enabled or not
 * @param callbackExecutor executor to run callback.
 * @param locale locale of request header
 * @param cardScannerPlugin optional scanner plugin.
 */
class PayjpConfiguration private constructor(
    val publicKey: String,
    val debugEnabled: Boolean,
    val locale: Locale,
    val callbackExecutor: Executor,
    val clientInfo: ClientInfo,
    val cardScannerPlugin: CardScannerPlugin?,
    val tokenBackgroundHandler: PayjpTokenBackgroundHandler?,
    val tdsRedirectName: String?
) {

    fun tokenConfiguration(): PayjpTokenConfiguration = PayjpTokenConfiguration(
        publicKey = publicKey,
        debugEnabled = debugEnabled,
        locale = locale,
        callbackExecutor = callbackExecutor,
        clientInfo = clientInfo
    )

    /**
     * Builder
     *
     * @param publicKey Public API Key
     */
    class Builder(private val publicKey: String) {

        /**
         * debug enabled or not
         * (usually `BuildConfig.DEBUG` is. )
         */
        private var debugEnabled: Boolean = false

        private var locale: Locale = Locale.getDefault()

        private var cardScannerPlugin: CardScannerPlugin? = null

        private var tokenBackgroundHandler: PayjpTokenBackgroundHandler? = null

        private var executor: Executor = MainThreadExecutor

        private var clientInfo: ClientInfo = ClientInfo.Builder().build()

        private var tdsRedirectName: String? = null

        /**
         * set debugEnabled
         *
         * @param enabled if true log for debug.
         */
        fun setDebugEnabled(enabled: Boolean) = apply { debugEnabled = enabled }

        /**
         * set locale (`en` or `ja`)
         * the default value is [Locale.getDefault].
         *
         * @param locale Locale
         */
        fun setLocale(locale: Locale) = apply { this.locale = locale }

        /**
         * set card scanner plugin
         * you also need to add `payjp-android-cardio`.
         *
         * @param plugin plugin
         */
        fun setCardScannerPlugin(plugin: CardScannerPlugin?) = apply { cardScannerPlugin = plugin }

        fun setTokenBackgroundHandler(handler: PayjpTokenBackgroundHandler?) = apply {
            tokenBackgroundHandler = handler
        }

        fun setCallbackExecutor(executor: Executor) = apply {
            this.executor = executor
        }

        fun setClientInfo(clientInfo: ClientInfo) = apply {
            this.clientInfo = clientInfo
        }

        fun setThreeDSecureRedirectName(name: String?) = apply {
            this.tdsRedirectName = name
        }

        /**
         * Build configuration.
         *
         * @return configuration
         */
        fun build() = PayjpConfiguration(
            publicKey = publicKey,
            debugEnabled = debugEnabled,
            locale = locale,
            callbackExecutor = executor,
            clientInfo = clientInfo,
            cardScannerPlugin = cardScannerPlugin,
            tokenBackgroundHandler = tokenBackgroundHandler,
            tdsRedirectName = tdsRedirectName
        )
    }
}
