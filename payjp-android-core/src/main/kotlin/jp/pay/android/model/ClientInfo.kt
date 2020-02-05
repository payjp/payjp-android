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
import java.lang.StringBuilder
import jp.pay.android.BuildConfig

@JsonClass(generateAdapter = true)
class ClientInfo internal constructor(
    @Json(name = "binding_name") val bindingName: String,
    @Json(name = "binding_version") val bindingVersion: String,
    @Json(name = "plugin_name") val bindingPlugin: String?,
    val uname: String,
    val platform: String,
    val publisher: String
) {
    fun getBindingInfo(): String = StringBuilder().apply {
        append(bindingName)
        append("/")
        append(bindingVersion)
        if (!bindingPlugin.isNullOrEmpty()) {
            append("@")
            append(bindingPlugin)
        }
    }.toString()

    class Builder {
        private val bindingName: String = "jp.pay.android"
        private val bindingVersion: String = BuildConfig.VERSION_NAME
        private val uname: String = "Android/${Build.VERSION.RELEASE}; ${Build.DEVICE}; ${Build.BRAND}; ${Build.MODEL}"
        private val platform: String = "android"
        private var publisher: String = "payjp"
        private var bindingPlugin: String? = null

        fun build(): ClientInfo = ClientInfo(
            bindingName = bindingName,
            bindingVersion = bindingVersion,
            uname = uname,
            platform = platform,
            publisher = publisher,
            bindingPlugin = bindingPlugin
        )

        fun setPublisher(publisher: String) = apply {
            this.publisher = publisher
        }

        fun setPlugin(plugin: String?) = apply {
            this.bindingPlugin = plugin
        }
    }
}
