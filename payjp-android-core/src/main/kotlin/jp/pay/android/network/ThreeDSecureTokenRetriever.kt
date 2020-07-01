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
package jp.pay.android.network

import com.squareup.moshi.Moshi
import jp.pay.android.model.PayjpResourceObject
import jp.pay.android.model.ThreeDSecureToken
import okhttp3.Request
import okhttp3.Response

/**
 * Retrieve [ThreeDSecureToken] from response.
 * 3DS token will return when the client request Token if required.
 * The response is redirect to 3DS endpoint with 3DS token in the body.
 *
 * @param baseUrl api baseUrl
 * @param moshi Response json adapter
 */
internal class ThreeDSecureTokenRetriever(
    private val baseUrl: String,
    private val moshi: Moshi
) {

    /**
     * Take [ThreeDSecureToken] from response.
     *
     * @param request request
     * @param response response
     * @return return 3DS Token if found, return null if not found.
     */
    fun retrieve(request: Request, response: Response): ThreeDSecureToken? = response
        .takeIf {
            request.url().toString() == "${baseUrl}tokens" &&
                request.method() == "POST" &&
                it.isSuccessful
        }
        // set limit 1MiB; but token response never become too large size.
        ?.peekBody(1024 * 1024)
        ?.let { body ->
            moshi.adapter(PayjpResourceObject::class.java).fromJson(body.string())
                ?.takeIf { it.name == "three_d_secure_token" }
                ?.let { ThreeDSecureToken(id = it.id) }
        }
}
