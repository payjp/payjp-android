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

import java.util.Locale
import jp.pay.android.plugin.CardScannerPlugin

/**
 * Configuration for Payjp
 *
 * use `Configuration.Builder`
 *
 * @param publicKey public key `pk_xxxxxxxxxxxxxxxxx`
 * @param debugEnabled is debug enabled or not
 * @param locale locale of request header
 * @param cardScannerPlugin optional scanner plugin.
 */
class PayjpConfiguration private constructor(
    val publicKey: String,
    val debugEnabled: Boolean,
    val locale: Locale,
    val cardScannerPlugin: CardScannerPlugin?
) {

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

        /**
         * Build configuration.
         *
         * @return configuration
         */
        fun build() = PayjpConfiguration(publicKey, debugEnabled, locale, cardScannerPlugin)
    }
}
