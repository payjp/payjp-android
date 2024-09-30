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
package jp.pay.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.exception.PayjpApiException
import jp.pay.android.exception.PayjpCardException
import jp.pay.android.exception.PayjpRateLimitException
import jp.pay.android.fixtures.ACCEPTED_BRANDS_EMPTY
import jp.pay.android.fixtures.ACCEPTED_BRANDS_FULL
import jp.pay.android.fixtures.ERROR_AUTH
import jp.pay.android.fixtures.ERROR_CARD_DECLINED
import jp.pay.android.fixtures.ERROR_INVALID_ID
import jp.pay.android.fixtures.ERROR_OVER_CAPACITY
import jp.pay.android.fixtures.TOKEN_OK
import jp.pay.android.model.CardBrand
import jp.pay.android.model.ClientInfo
import jp.pay.android.model.TenantId
import jp.pay.android.model.TokenId
import jp.pay.android.network.TokenApiClientFactory.createApiClient
import jp.pay.android.network.TokenApiClientFactory.createHeaderInterceptor
import jp.pay.android.network.TokenApiClientFactory.createOkHttp
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import java.io.IOException
import java.nio.charset.Charset
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * [PayjpToken]
 */
@RunWith(AndroidJUnit4::class)
class PayjpTokenTest {

    private val mockWebServer = MockWebServer()

    internal class CurrentThreadExecutor : Executor {
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
    private val configuration = PayjpTokenConfiguration(
        publicKey = "pk_test_0383a1b8f91e8a6e3ea0e2a9",
        debugEnabled = true,
        locale = Locale.getDefault(),
        callbackExecutor = Executors.newSingleThreadExecutor(),
        clientInfo = ClientInfo.Builder().build()
    )

    private fun createTokenService(): PayjpTokenService =
        PayjpToken(
            configuration = configuration,
            interceptor = createInterceptor(),
            payjpApi = createApi()
        )

    private fun createApi(): PayjpApi {
        val baseUrl = mockWebServer.url("/").toString()
        return createApiClient(
            baseUrl = baseUrl,
            okHttpClient = createOkHttp(
                baseUrl = baseUrl,
                debuggable = false,
                interceptor = createInterceptor()
            ),
            callbackExecutor = CurrentThreadExecutor()
        )
    }

    private fun createInterceptor() = createHeaderInterceptor(
        locale = Locale.US,
        clientInfo = ClientInfo.Builder().build()
    )

    @Test
    fun createToken_ok() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TOKEN_OK))

        createTokenService()
            .createToken(
                number = "4242424242424242",
                cvc = "123",
                expMonth = "02",
                expYear = "2020",
                name = "TARO YAMADA"
            )
            .run()
            .let { token ->
                assertEquals("tok_5ca06b51685e001723a2c3b4aeb4", token.id)
                assertEquals("car_e3ccd4e0959f45e7c75bacc4be90", token.card.id)
            }

        mockWebServer.takeRequest()
            .let { request ->
                assertEquals("POST", request.method)
                assertEquals("/tokens", request.path)
                assertEquals(
                    "Basic cGtfdGVzdF8wMzgzYTFiOGY5MWU4YTZlM2VhMGUyYTk6",
                    request.getHeader("Authorization")
                )
                assertEquals("en", request.getHeader("Locale"))
                assertEquals(
                    """
                        card%5Bnumber%5D=4242424242424242
                        &card%5Bcvc%5D=123
                        &card%5Bexp_month%5D=02
                        &card%5Bexp_year%5D=2020
                        &card%5Bname%5D=TARO%20YAMADA
                        &three_d_secure=false
                    """.trimIndent().replace("\n", ""),
                    request.body.readString(Charset.forName("utf-8"))
                )
            }
    }

    @Test
    fun createToken_ok_without_name() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TOKEN_OK))

        createTokenService()
            .createToken(
                number = "4242424242424242",
                cvc = "123",
                expMonth = "02",
                expYear = "2020"
            )
            .run()

        assertEquals(
            """
                card%5Bnumber%5D=4242424242424242
                &card%5Bcvc%5D=123
                &card%5Bexp_month%5D=02
                &card%5Bexp_year%5D=2020
                &three_d_secure=false
            """.trimIndent().replace("\n", ""),
            mockWebServer.takeRequest().body.readString(Charset.forName("utf-8"))
        )
    }

    @Test
    fun createToken_ok_with_tenant() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TOKEN_OK))

        createTokenService()
            .createToken(
                number = "4242424242424242",
                cvc = "123",
                expMonth = "02",
                expYear = "2020",
                tenantId = TenantId("tenant_id")
            )
            .run()

        assertEquals(
            """
                card%5Bnumber%5D=4242424242424242
                &card%5Bcvc%5D=123
                &card%5Bexp_month%5D=02
                &card%5Bexp_year%5D=2020
                &tenant=tenant_id
                &three_d_secure=false
            """.trimIndent().replace("\n", ""),
            mockWebServer.takeRequest().body.readString(Charset.forName("utf-8"))
        )
    }

    @Test
    fun createToken_ok_with_email_and_phone() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TOKEN_OK))

        createTokenService()
            .createToken(
                number = "4242424242424242",
                cvc = "123",
                expMonth = "02",
                expYear = "2020",
                email = "test@example.com",
                phone = "+819012345678"
            )
            .run()

        assertEquals(
            """
                card%5Bnumber%5D=4242424242424242
                &card%5Bcvc%5D=123
                &card%5Bexp_month%5D=02
                &card%5Bexp_year%5D=2020
                &card%5Bemail%5D=test%40example.com
                &card%5Bphone%5D=%2B819012345678
                &three_d_secure=false
            """.trimIndent().replace("\n", ""),
            mockWebServer.takeRequest().body.readString(Charset.forName("utf-8"))
        )
    }

    @Test
    fun createToken_with_three_d_secure() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TOKEN_OK))

        createTokenService()
            .createToken(
                number = "4242424242424242",
                cvc = "123",
                expMonth = "02",
                expYear = "2020",
                name = "TARO YAMADA",
                threeDSecure = true,
            )
            .run()

        assertEquals(
            """
                card%5Bnumber%5D=4242424242424242
                &card%5Bcvc%5D=123
                &card%5Bexp_month%5D=02
                &card%5Bexp_year%5D=2020
                &card%5Bname%5D=TARO%20YAMADA
                &three_d_secure=true
            """.trimIndent().replace("\n", ""),
            mockWebServer.takeRequest().body.readString(Charset.forName("utf-8"))
        )
    }

    @Test
    fun createToken_auth_error() {
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setBody(ERROR_AUTH))

        val task = createTokenService()
            .createToken(
                number = "4242424242424242",
                cvc = "123",
                expMonth = "02",
                expYear = "2020",
                name = "TARO YAMADA"
            )

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

        val task = createTokenService()
            .createToken(
                number = "4242424242424242",
                cvc = "123",
                expMonth = "02",
                expYear = "2020",
                name = "TARO YAMADA"
            )

        try {
            task.run()
            fail()
        } catch (e: PayjpCardException) {
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

        val task = createTokenService()
            .createToken(
                number = "4242424242424242",
                cvc = "123",
                expMonth = "02",
                expYear = "2020",
                name = "TARO YAMADA"
            )

        try {
            task.run()
            fail()
        } catch (_: IOException) {
        }
    }

    @Test
    fun createToken_over_capacity_error() {
        mockWebServer.enqueue(MockResponse().setResponseCode(429).setBody(ERROR_OVER_CAPACITY))

        val task = createTokenService()
            .createToken(
                number = "4242424242424242",
                cvc = "123",
                expMonth = "02",
                expYear = "2020",
                name = "TARO YAMADA"
            )

        try {
            task.run()
            fail()
        } catch (e: PayjpRateLimitException) {
            assertEquals("The service is over capacity. Please try again later.", e.message)
            assertEquals(HttpException::class.java, e.cause::class.java)
            assertEquals(429, e.httpStatusCode)
            assertEquals("client_error", e.apiError.type)
            assertEquals("over_capacity", e.apiError.code)
            assertEquals(ERROR_OVER_CAPACITY, e.source)
        }
    }

    @Test
    fun finishTokenTds() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TOKEN_OK))

        val tokenId = TokenId(id = "tok_5ca06b51685e001723a2c3b4aeb4")
        createTokenService()
            .finishTokenThreeDSecure(tokenId)
            .run()
            .let { token ->
                assertEquals("tok_5ca06b51685e001723a2c3b4aeb4", token.id)
                assertEquals("car_e3ccd4e0959f45e7c75bacc4be90", token.card.id)
            }

        mockWebServer.takeRequest()
            .let { request ->
                assertEquals("POST", request.method)
                assertEquals("/tokens/${tokenId.id}/tds_finish", request.path)
                assertEquals(
                    "Basic cGtfdGVzdF8wMzgzYTFiOGY5MWU4YTZlM2VhMGUyYTk6",
                    request.getHeader("Authorization")
                )
                assertEquals("en", request.getHeader("Locale"))
            }
    }

    @Test
    fun getToken_ok() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TOKEN_OK))

        createTokenService()
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
                assertEquals(
                    "Basic cGtfdGVzdF8wMzgzYTFiOGY5MWU4YTZlM2VhMGUyYTk6",
                    request.getHeader("Authorization")
                )
                assertEquals("en", request.getHeader("Locale"))
            }
    }

    @Test
    fun getToken_auth_error() {
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setBody(ERROR_AUTH))

        val task = createTokenService()
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

        val task = createTokenService()
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

        val task = createTokenService()
            .getToken("tok_5ca06b51685e001723a2c3b4aeb4")

        try {
            task.run()
            fail()
        } catch (_: IOException) {
        }
    }

    @Test
    fun getAcceptedBrands_ok() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(ACCEPTED_BRANDS_FULL))

        createTokenService()
            .getAcceptedBrands()
            .run()
            .let { response ->
                assertEquals(true, response.livemode)
                assertThat(
                    response.brands,
                    contains(
                        CardBrand.VISA,
                        CardBrand.MASTER_CARD,
                        CardBrand.JCB,
                        CardBrand.AMEX,
                        CardBrand.DINERS_CLUB,
                        CardBrand.DISCOVER
                    )
                )
            }

        mockWebServer.takeRequest()
            .let { request ->
                assertEquals("GET", request.method)
                assertEquals("/accounts/brands", request.path)
                assertEquals(
                    "Basic cGtfdGVzdF8wMzgzYTFiOGY5MWU4YTZlM2VhMGUyYTk6",
                    request.getHeader("Authorization")
                )
                assertEquals("en", request.getHeader("Locale"))
            }
    }

    @Test
    fun getAcceptedBrands_empty() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(ACCEPTED_BRANDS_EMPTY))

        createTokenService()
            .getAcceptedBrands()
            .run()
            .let { response ->
                assertEquals(true, response.livemode)
                assertThat(response.brands, empty())
            }

        mockWebServer.takeRequest()
            .let { request ->
                assertEquals("GET", request.method)
                assertEquals("/accounts/brands", request.path)
                assertEquals(
                    "Basic cGtfdGVzdF8wMzgzYTFiOGY5MWU4YTZlM2VhMGUyYTk6",
                    request.getHeader("Authorization")
                )
            }
    }

    @Test
    fun getAcceptedBrands_tenant() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(ACCEPTED_BRANDS_FULL))

        createTokenService()
            .getAcceptedBrands(TenantId("foobar"))
            .run()
            .let { response ->
                assertEquals(true, response.livemode)
                assertThat(
                    response.brands,
                    contains(
                        CardBrand.VISA,
                        CardBrand.MASTER_CARD,
                        CardBrand.JCB,
                        CardBrand.AMEX,
                        CardBrand.DINERS_CLUB,
                        CardBrand.DISCOVER
                    )
                )
            }

        mockWebServer.takeRequest()
            .let { request ->
                assertEquals("GET", request.method)
                assertEquals("/accounts/brands?tenant=foobar", request.path)
                assertEquals(
                    "Basic cGtfdGVzdF8wMzgzYTFiOGY5MWU4YTZlM2VhMGUyYTk6",
                    request.getHeader("Authorization")
                )
            }
    }
}
