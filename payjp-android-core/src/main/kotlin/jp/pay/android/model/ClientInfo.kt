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
package jp.pay.android.model

import android.os.Build
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import jp.pay.android.BuildConfig
import java.lang.StringBuilder

@JsonClass(generateAdapter = true)
class ClientInfo internal constructor(
    @Json(name = "bindings_name") val bindingsName: String,
    @Json(name = "bindings_version") val bindingsVersion: String,
    @Json(name = "bindings_plugin") val bindingsPlugin: String?,
    @Json(name = "card_form_type") val cardFormType: String?,
    val uname: String,
    val platform: String,
    val publisher: String
) {
    fun getBindingInfo(): String = StringBuilder().apply {
        append(bindingsName)
        append("/")
        append(bindingsVersion)
        if (!bindingsPlugin.isNullOrEmpty()) {
            append("@")
            append(bindingsPlugin)
        }
    }.toString()

    fun toBuilder(): Builder = Builder(
        bindingsName = bindingsName,
        bindingsVersion = bindingsVersion,
        uname = uname,
        platform = platform,
    ).apply {
        setPlugin(bindingsPlugin)
        setPublisher(publisher)
        setCardFormType(cardFormType)
    }

    class Builder(
        private val bindingsName: String = "jp.pay.android",
        private val bindingsVersion: String = BuildConfig.VERSION_NAME,
        private val uname: String = "Android/${Build.VERSION.RELEASE}; ${Build.DEVICE}; ${Build.BRAND}; ${Build.MODEL}",
        private val platform: String = "android",
    ) {
        private var publisher: String = "payjp"
        private var bindingsPlugin: String? = null
        private var cardFormType: String? = null

        fun build(): ClientInfo = ClientInfo(
            bindingsName = bindingsName,
            bindingsVersion = bindingsVersion,
            cardFormType = cardFormType,
            uname = uname,
            platform = platform,
            publisher = publisher,
            bindingsPlugin = bindingsPlugin
        )

        fun setPublisher(publisher: String) = apply {
            this.publisher = publisher
        }

        fun setPlugin(plugin: String?) = apply {
            this.bindingsPlugin = plugin
        }

        fun setCardFormType(cardFormType: String?) = apply {
            this.cardFormType = cardFormType
        }
    }
}
