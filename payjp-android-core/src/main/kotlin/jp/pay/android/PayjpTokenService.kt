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

import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.TenantId
import jp.pay.android.model.ThreeDSecureToken
import jp.pay.android.model.Token

/**
 * interface for retrieve and create token.
 */
interface PayjpTokenService {

    fun getPublicKey(): String

    /**
     * Create token from card information.
     *
     * @param number card number
     * @param cvc card cvc
     * @param expMonth card expiration month (zero-padding 2 digits e.g. January -> `01`)
     * @param expYear card expiration year (4 digits e.g. 2020 -> `2020`)
     * @param name card holder name
     * @param tenantId tenant id (only for platform)
     * @return task to create token.
     */
    fun createToken(
        number: String,
        cvc: String,
        expMonth: String,
        expYear: String,
        name: String? = null,
        tenantId: TenantId? = null
    ): Task<Token> = createToken(PayjpTokenParam(number, cvc, expMonth, expYear, name, tenantId))

    /**
     * Create token from param
     *
     * @param param param
     * @return task to create token.
     */
    fun createToken(param: PayjpTokenParam): Task<Token>

    /**
     * Create token from 3DS token
     *
     * @param threeDSecureToken 3DS token
     * @return task to create token.
     */
    fun createToken(threeDSecureToken: ThreeDSecureToken): Task<Token>

    /**
     * Retrieve token from token id.
     *
     * @param id token id
     * @return task to retrieve token.
     */
    fun getToken(id: String): Task<Token>

    /**
     * Get accepted brands.
     *
     * @return task to get accepted brands.
     */
    fun getAcceptedBrands(): Task<CardBrandsAcceptedResponse> = getAcceptedBrands(tenantId = null)

    /**
     * Get accepted brands with tenantId.
     * Only for platform.
     *
     * @param tenantId tenant id.
     * @return task to get accepted brands.
     */
    fun getAcceptedBrands(tenantId: TenantId?): Task<CardBrandsAcceptedResponse>

    /**
     * You can observe [PayjpTokenOperationStatus] changes to know if you should make a request.
     *
     * @return observer
     */
    fun getTokenOperationObserver(): PayjpTokenOperationObserverService
}
