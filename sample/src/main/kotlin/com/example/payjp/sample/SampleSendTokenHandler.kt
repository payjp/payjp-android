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
package com.example.payjp.sample

import android.util.Log
import java.io.IOException
import jp.pay.android.PayjpTokenBackgroundHandler
import jp.pay.android.model.Token
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException

class SampleSendTokenHandler(
    private val backendService: SampleBackendService
) : PayjpTokenBackgroundHandler {

    override fun handleTokenInBackground(token: Token): PayjpTokenBackgroundHandler.CardFormStatus {
        return runBlocking {
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
