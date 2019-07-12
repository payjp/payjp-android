/*
 *
 * Copyright (c) 2018 PAY, Inc.
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

import jp.pay.android.exception.PayjpApiException
import jp.pay.android.network.createApiClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.Executor

/**
 * [PayjpToken]
 */
@RunWith(RobolectricTestRunner::class)
class PayjpTokenTest {

    private val mockWebServer = MockWebServer()
    internal inner class CurrentThreadExecutor : Executor {
        override fun execute(r: Runnable) {
            r.run()
        }
    }

    @Before
    fun setUp() {
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // pk_test_0383a1b8f91e8a6e3ea0e2a9
    // Basic cGtfdGVzdF8wMzgzYTFiOGY5MWU4YTZlM2VhMGUyYTk6
    private val configuration =
            PayjpTokenConfiguration.Builder(publicKey = "pk_test_0383a1b8f91e8a6e3ea0e2a9").build()

    @Test
    fun init_callTwice() {
        val target1 = PayjpToken.init(configuration)
        val target2 = PayjpToken.init(configuration)
        assertEquals(target1, target2)
    }

    @Test
    fun createToken_ok() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TOKEN_OK))

        PayjpToken(configuration = configuration,
                tokenApi = createApiClient(baseUrl = mockWebServer.url("/").toString(),
                        callbackExecutor = CurrentThreadExecutor()))
                .createToken(number = "4242424242424242",
                        cvc = "123", expMonth = "02", expYear = "2020", name = "TARO YAMADA")
                .run()
                .let { token ->
                    assertEquals("tok_5ca06b51685e001723a2c3b4aeb4", token.id)
                    assertEquals("car_e3ccd4e0959f45e7c75bacc4be90", token.card.id)
                }

        mockWebServer.takeRequest()
                .let { request ->
                    assertEquals("POST", request.method)
                    assertEquals("/tokens", request.path)
                    assertEquals("Basic cGtfdGVzdF8wMzgzYTFiOGY5MWU4YTZlM2VhMGUyYTk6",
                            request.getHeader("Authorization"))
                    assertEquals("card%5Bnumber%5D=4242424242424242" +
                            "&card%5Bcvc%5D=123" +
                            "&card%5Bexp_month%5D=02" +
                            "&card%5Bexp_year%5D=2020" +
                            "&card%5Bname%5D=TARO%20YAMADA",
                            request.body.readString(Charset.forName("utf-8")))
                }
    }

    @Test
    fun createToken_ok_without_name() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TOKEN_OK))

        PayjpToken(configuration = configuration,
                tokenApi = createApiClient(baseUrl = mockWebServer.url("/").toString(),
                        callbackExecutor = CurrentThreadExecutor()))
                .createToken(number = "4242424242424242",
                        cvc = "123", expMonth = "02", expYear = "2020")
                .run()

        assertEquals("card%5Bnumber%5D=4242424242424242" +
                "&card%5Bcvc%5D=123" +
                "&card%5Bexp_month%5D=02" +
                "&card%5Bexp_year%5D=2020",
                mockWebServer.takeRequest().body.readString(Charset.forName("utf-8")))
    }

    @Test
    fun createToken_auth_error() {
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setBody(ERROR_AUTH))

        val task = PayjpToken(configuration = configuration,
                tokenApi = createApiClient(baseUrl = mockWebServer.url("/").toString(),
                        callbackExecutor = CurrentThreadExecutor()))
                .createToken(number = "4242424242424242",
                        cvc = "123", expMonth = "02", expYear = "2020", name = "TARO YAMADA")

        try {
            task.run()
            fail()
        } catch (e: PayjpApiException) {
            assertEquals("Invalid API Key: {0}", e.message)
            assertEquals(HttpException::class.java, e.cause::class.java)
            assertEquals(401, e.httpStatusCode)
            assertEquals("auth_error", e.apiError.type)
            assertEquals(null, e.apiError.code)
            assertEquals(ERROR_AUTH, e.source)
        }
    }

    @Test
    fun createToken_card_error() {
        mockWebServer.enqueue(MockResponse().setResponseCode(402).setBody(ERROR_CARD_DECLINED))

        val task = PayjpToken(configuration = configuration,
                tokenApi = createApiClient(baseUrl = mockWebServer.url("/").toString(),
                        callbackExecutor = CurrentThreadExecutor()))
                .createToken(number = "4242424242424242",
                        cvc = "123", expMonth = "02", expYear = "2020", name = "TARO YAMADA")

        try {
            task.run()
            fail()
        } catch (e: PayjpApiException) {
            assertEquals("Card declined", e.message)
            assertEquals(HttpException::class.java, e.cause::class.java)
            assertEquals(402, e.httpStatusCode)
            assertEquals("card_error", e.apiError.type)
            assertEquals("card_declined", e.apiError.code)
            assertEquals(ERROR_CARD_DECLINED, e.source)
        }
    }

    @Test
    fun createToken_server_error() {
        mockWebServer.enqueue(MockResponse().setResponseCode(501))

        val task = PayjpToken(configuration = configuration,
                tokenApi = createApiClient(baseUrl = mockWebServer.url("/").toString(),
                        callbackExecutor = CurrentThreadExecutor()))
                .createToken(number = "4242424242424242",
                        cvc = "123", expMonth = "02", expYear = "2020", name = "TARO YAMADA")

        try {
            task.run()
            fail()
        } catch (e: IOException) {
        }
    }

    @Test
    fun getToken_ok() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TOKEN_OK))

        PayjpToken(configuration = configuration,
                tokenApi = createApiClient(baseUrl = mockWebServer.url("/").toString(),
                        callbackExecutor = CurrentThreadExecutor()))
                .getToken("tok_5ca06b51685e001723a2c3b4aeb4")
                .run()
                .let { token ->
                    assertEquals("tok_5ca06b51685e001723a2c3b4aeb4", token.id)
                    assertEquals("car_e3ccd4e0959f45e7c75bacc4be90", token.card.id)
                }

        mockWebServer.takeRequest()
                .let { request ->
                    assertEquals("GET", request.method)
                    assertEquals("/tokens/tok_5ca06b51685e001723a2c3b4aeb4", request.path)
                    assertEquals("Basic cGtfdGVzdF8wMzgzYTFiOGY5MWU4YTZlM2VhMGUyYTk6",
                            request.getHeader("Authorization"))
                }
    }

    @Test
    fun getToken_auth_error() {
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setBody(ERROR_AUTH))

        val task = PayjpToken(configuration = configuration,
                tokenApi = createApiClient(baseUrl = mockWebServer.url("/").toString(),
                        callbackExecutor = CurrentThreadExecutor()))
                .getToken("tok_5ca06b51685e001723a2c3b4aeb4")

        try {
            task.run()
            fail()
        } catch (e: PayjpApiException) {
            assertEquals("Invalid API Key: {0}", e.message)
            assertEquals(HttpException::class.java, e.cause::class.java)
            assertEquals(401, e.httpStatusCode)
            assertEquals("auth_error", e.apiError.type)
            assertEquals(null, e.apiError.code)
            assertEquals(ERROR_AUTH, e.source)
        }
    }

    @Test
    fun getToken_client_error() {
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody(ERROR_INVALID_ID))

        val task = PayjpToken(configuration = configuration,
                tokenApi = createApiClient(baseUrl = mockWebServer.url("/").toString(),
                        callbackExecutor = CurrentThreadExecutor()))
                .getToken("tok_587af2665fdced4742e5fbb3ecfcaa")

        try {
            task.run()
            fail()
        } catch (e: PayjpApiException) {
            assertEquals("No such token: tok_587af2665fdced4742e5fbb3ecfcaa", e.message)
            assertEquals(HttpException::class.java, e.cause::class.java)
            assertEquals(404, e.httpStatusCode)
            assertEquals("client_error", e.apiError.type)
            assertEquals("invalid_id", e.apiError.code)
            assertEquals(ERROR_INVALID_ID, e.source)
        }
    }

    @Test
    fun getToken_server_error() {
        mockWebServer.enqueue(MockResponse().setResponseCode(501))

        val task = PayjpToken(configuration = configuration,
                tokenApi = createApiClient(baseUrl = mockWebServer.url("/").toString(),
                        callbackExecutor = CurrentThreadExecutor()))
                .getToken("tok_5ca06b51685e001723a2c3b4aeb4")

        try {
            task.run()
            fail()
        } catch (e: IOException) {
        }
    }

    companion object {

        const val TOKEN_OK = """
{
  "card": {
    "address_city": null,
    "address_line1": null,
    "address_line2": null,
    "address_state": null,
    "address_zip": null,
    "address_zip_check": "unchecked",
    "brand": "Visa",
    "country": null,
    "created": 1442290383,
    "customer": null,
    "cvc_check": "passed",
    "exp_month": 2,
    "exp_year": 2020,
    "fingerprint": "e1d8225886e3a7211127df751c86787f",
    "id": "car_e3ccd4e0959f45e7c75bacc4be90",
    "livemode": false,
    "metadata": {},
    "last4": "4242",
    "name": null,
    "object": "card"
  },
  "created": 1442290383,
  "id": "tok_5ca06b51685e001723a2c3b4aeb4",
  "livemode": false,
  "object": "token",
  "used": false
}
        """

        const val ERROR_AUTH = """
{
  "error": {
    "message": "Invalid API Key: {0}",
    "status": 401,
    "type": "auth_error"
  }
}
"""

        const val ERROR_CARD_DECLINED = """
{
  "error": {
    "code": "card_declined",
    "message": "Card declined",
    "status": 402,
    "type": "card_error"
  }
}
"""

        const val ERROR_INVALID_ID = """
{
  "error": {
    "code": "invalid_id",
    "message": "No such token: tok_587af2665fdced4742e5fbb3ecfcaa",
    "param": "id",
    "status": 404,
    "type": "client_error"
  }
}
"""
    }
}