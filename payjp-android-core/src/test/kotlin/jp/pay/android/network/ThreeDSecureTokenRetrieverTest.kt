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
package jp.pay.android.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squareup.moshi.JsonDataException
import jp.pay.android.PayjpConstants
import jp.pay.android.model.PayjpResourceObject
import jp.pay.android.model.ThreeDSecureToken
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThreeDSecureTokenRetrieverTest {

    private val moshi = TokenApiClientFactory.moshi
    private val mediaType = MediaType.parse("application/json charset=utf-8")
    private val tdsTokenObject = PayjpResourceObject(name = "three_d_secure_token", id = "tds_foo")
    private val tdsTokenObjectJson = moshi.adapter(PayjpResourceObject::class.java).toJson(tdsTokenObject)
    private val baseUrl = PayjpConstants.API_ENDPOINT

    private fun createRetriever() = ThreeDSecureTokenRetriever(
        baseUrl = baseUrl,
        moshi = moshi
    )

    private fun createRequest(
        url: String,
        applyFunc: Request.Builder.() -> Unit = {
        }
    ): Request = Request.Builder()
        .url(url)
        .post(RequestBody.create(mediaType, "foo=bar"))
        .apply(applyFunc)
        .build()

    private fun createResponse(
        request: Request,
        content: String,
        code: Int
    ): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .message("")
        .body(ResponseBody.create(mediaType, content))
        .code(code)
        .build()

    @Test
    fun retrieve_token_if_response_is_tds_token() {
        val retriever = createRetriever()
        val request = createRequest(url = "${baseUrl}tokens")
        val response = createResponse(request, tdsTokenObjectJson, 200)
        val result = retriever.retrieve(
            request = request,
            response = response
        )
        assertThat(result, `is`(ThreeDSecureToken(id = tdsTokenObject.id)))
    }

    @Test
    fun retrieve_nothing_if_path_is_unknown() {
        val retriever = createRetriever()
        val request = createRequest(url = "${baseUrl}tokens/any")
        val response = createResponse(request, tdsTokenObjectJson, 200)
        val result = retriever.retrieve(
            request = request,
            response = response
        )
        assertThat(result, nullValue())
    }

    @Test
    fun retrieve_nothing_if_method_is_not_post() {
        val retriever = createRetriever()
        val request = createRequest(
            url = "${baseUrl}tokens",
            applyFunc = { get() }
        )
        val response = createResponse(request, tdsTokenObjectJson, 200)
        val result = retriever.retrieve(
            request = request,
            response = response
        )
        assertThat(result, nullValue())
    }

    @Test
    fun retrieve_nothing_if_redirect_response() {
        val retriever = createRetriever()
        val request = createRequest(url = "${baseUrl}tokens")
        val response = createResponse(request, tdsTokenObjectJson, 303)
        val result = retriever.retrieve(
            request = request,
            response = response
        )
        assertThat(result, nullValue())
    }

    @Test
    fun retrieve_nothing_if_response_is_not_tds_token() {
        val retriever = createRetriever()
        val request = createRequest(url = "${baseUrl}tokens")
        val json =
            """
{ "object": "other_object", "id": "other_id" }
            """.trimIndent()
        val response = createResponse(request, json, 200)
        val result = retriever.retrieve(
            request = request,
            response = response
        )
        assertThat(result, nullValue())
    }

    @Test(expected = JsonDataException::class)
    fun throw_exception_if_response_is_unknown() {
        val retriever = createRetriever()
        val request = createRequest(url = "${baseUrl}tokens")
        val json =
            """
{ "unknown_key": "unknown_value" }
            """.trimIndent()
        val response = createResponse(request, json, 200)
        retriever.retrieve(
            request = request,
            response = response
        )
    }
}
