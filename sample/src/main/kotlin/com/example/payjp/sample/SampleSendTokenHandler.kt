/*
 *
 * Copyright (c) 2021 PAY, Inc.
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
package com.example.payjp.sample

import android.util.Log
import com.squareup.moshi.Moshi
import jp.pay.android.PayjpTokenBackgroundHandler
import jp.pay.android.model.Token
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

class SampleSendTokenHandler(
    private val backendUrl: String,
    private val okHttpClient: OkHttpClient
) : PayjpTokenBackgroundHandler {

    private val backendService: SampleBackendService by lazy {
        Retrofit.Builder()
            .baseUrl(backendUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .build()
            .create(SampleBackendService::class.java)
    }

    override fun handleTokenInBackground(token: Token): PayjpTokenBackgroundHandler.CardFormStatus {
        return runBlocking {
            if (backendUrl.isEmpty()) {
                val message =
                    """
                `BACKEND_URL` is not found yet.
                You can send token(${token.id}) to your own server to make Customer etc.
                    """.trimIndent()
                Log.w("SampleSendTokenHandler", message)
                return@runBlocking PayjpTokenBackgroundHandler.CardFormStatus.Complete()
            }
            try {
                // send token to server
                Log.i("SampleSendTokenHandler", "try to send token ${Thread.currentThread()}")
                backendService.saveCard(token.id)
                Log.i("SampleSendTokenHandler", "success")
                PayjpTokenBackgroundHandler.CardFormStatus.Complete()
            } catch (e: IOException) {
                Log.w("SampleSendTokenHandler", "network error", e)
                // Network error will be here
                PayjpTokenBackgroundHandler.CardFormStatus.Error("ネットワークに問題がありました")
            } catch (e: HttpException) {
                Log.w("SampleSendTokenHandler", "http error", e)
                // Parse response and reveal the error...
                PayjpTokenBackgroundHandler.CardFormStatus.Error("予期しない問題が発生しました")
            } catch (t: Throwable) {
                Log.w("SampleSendTokenHandler", "unknown error", t)
                // Unknown errors come here
                PayjpTokenBackgroundHandler.CardFormStatus.Error("予期しない問題が発生しました")
            }
        }
    }
}
