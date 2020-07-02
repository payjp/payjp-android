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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squareup.moshi.Moshi
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.startsWith
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClientInfoTest {

    @Test
    fun defaultValue() {
        val clientInfo = ClientInfo.Builder().build()
        assertThat(clientInfo.bindingsName, `is`("jp.pay.android"))
        assertThat(clientInfo.uname, startsWith("Android/"))
        assertThat(clientInfo.platform, `is`("android"))
        assertThat(clientInfo.publisher, `is`("payjp"))
    }

    @Test
    fun plugin() {
        val clientInfo = ClientInfo.Builder()
            .setPublisher("kitagawa")
            .setPlugin("jp.pay.kitagawa/1.0.0")
            .build()
        assertThat(clientInfo.publisher, `is`("kitagawa"))
        assertThat(clientInfo.bindingsPlugin, `is`("jp.pay.kitagawa/1.0.0"))
    }

    @Test
    fun bindingInfo() {
        val clientInfo = ClientInfo(
            bindingsName = "jp.pay.android",
            bindingsVersion = "1.1.0",
            bindingsPlugin = null,
            uname = "Android/10",
            platform = "android",
            publisher = "payjp"
        )
        assertThat(clientInfo.getBindingInfo(), `is`("jp.pay.android/1.1.0"))
    }

    @Test
    fun bindingInfo_withPlugin() {
        val clientInfo = ClientInfo(
            bindingsName = "jp.pay.android",
            bindingsVersion = "1.1.0",
            bindingsPlugin = "jp.pay.kitagawa/1.0.0",
            uname = "Android/10",
            platform = "android",
            publisher = "payjp"
        )
        assertThat(clientInfo.getBindingInfo(), `is`("jp.pay.android/1.1.0@jp.pay.kitagawa/1.0.0"))
    }

    @Test
    fun toJson() {
        val clientInfo = ClientInfo(
            bindingsName = "jp.pay.android",
            bindingsVersion = "1.1.0",
            bindingsPlugin = "jp.pay.kitagawa/1.0.0",
            uname = "Android/10",
            platform = "android",
            publisher = "payjp"
        )
        val json = Moshi.Builder().build().adapter<ClientInfo>(ClientInfo::class.java)
            .toJson(clientInfo)
        assertThat(
            json,
            `is`(
                """
{
  "bindings_name": "jp.pay.android",
  "bindings_version": "1.1.0",
  "bindings_plugin": "jp.pay.kitagawa/1.0.0",
  "uname": "Android/10",
  "platform": "android",
  "publisher": "payjp"
}
                """.trimIndent()
                    .replace(" ", "")
                    .replace("\n", "")
            )
        )
    }
}
