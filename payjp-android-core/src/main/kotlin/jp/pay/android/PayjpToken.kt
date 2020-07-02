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
package jp.pay.android

import android.util.Base64
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.TenantId
import jp.pay.android.model.ThreeDSecureToken
import jp.pay.android.model.Token
import jp.pay.android.network.TokenApiClientFactory.createApiClient
import jp.pay.android.network.TokenApiClientFactory.createOkHttp
import java.nio.charset.Charset

/**
 * Payjp token client
 *
 * @param configuration configuration
 * @param payjpApi api
 * @constructor create new Payjp instance.
 */
class PayjpToken internal constructor(
    private val configuration: PayjpTokenConfiguration,
    private val payjpApi: PayjpApi
) : PayjpTokenService {

    /**
     * Constructor for configuration.
     *
     * @param configuration create with configuration
     */
    constructor(configuration: PayjpTokenConfiguration) : this(
        configuration = configuration,
        payjpApi = createApiClient(
            baseUrl = PayjpConstants.API_ENDPOINT,
            okHttpClient = createOkHttp(
                baseUrl = PayjpConstants.API_ENDPOINT,
                locale = configuration.locale,
                clientInfo = configuration.clientInfo,
                debuggable = configuration.debugEnabled
            ),
            callbackExecutor = configuration.callbackExecutor
        )
    )

    private val authorization: String

    init {
        authorization = createAuthorization(configuration.publicKey)
    }

    override fun getPublicKey(): String = configuration.publicKey

    override fun createToken(param: PayjpTokenParam): Task<Token> {
        return payjpApi.createToken(
            authorization = authorization,
            number = param.number,
            cvc = param.cvc,
            expMonth = param.expMonth,
            expYear = param.expYear,
            name = param.name,
            tenant = param.tenantId?.id
        )
    }

    override fun createToken(threeDSecureToken: ThreeDSecureToken): Task<Token> {
        return payjpApi.createToken(
            authorization = authorization,
            tdsId = threeDSecureToken.id
        )
    }

    /**
     * Obtain token from token id.
     *
     */
    override fun getToken(id: String): Task<Token> {
        return payjpApi.getToken(authorization, id)
    }

    /**
     * Get accepted brands with tenant id (for platform)
     *
     * @param tenantId tenant id (only for platformer)
     * @return task of accepted brands
     */
    override fun getAcceptedBrands(tenantId: TenantId?): Task<CardBrandsAcceptedResponse> {
        return payjpApi.getAcceptedBrands(authorization, tenantId?.id)
    }

    private fun createAuthorization(publicKey: String) =
        "$publicKey:".toByteArray(Charset.forName("UTF-8"))
            .let { data -> Base64.encodeToString(data, Base64.NO_WRAP) }
            .let { credential -> "Basic $credential" }
}
