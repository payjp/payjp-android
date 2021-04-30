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
@file:JvmName("PayjpCoroutine")

package jp.pay.android.coroutine

import jp.pay.android.PayjpTokenParam
import jp.pay.android.PayjpTokenService
import jp.pay.android.Task
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.model.TokenId
import jp.pay.android.ui.widget.PayjpCardFormView
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Create token by suspend function.
 *
 * @return token
 * @see [PayjpCardFormView.createToken]
 */
suspend fun PayjpCardFormView.createTokenSuspend(): Token = createToken().toSuspend()

/**
 * Create token with param by suspend function.
 *
 * @param param parameters for token.
 * @return token
 * @see [PayjpTokenService.createToken]
 */
suspend fun PayjpTokenService.createTokenSuspend(param: PayjpTokenParam): Token =
    createToken(param).toSuspend()

suspend fun PayjpTokenService.finishTokenTdsSuspend(tokenId: TokenId): Token =
    finishTokenThreeDSecure(tokenId).toSuspend()

/**
 * Get token by suspend function.
 *
 * @param id token id
 * @return token
 * @see [PayjpTokenService.getToken]
 */
suspend fun PayjpTokenService.getTokenSuspend(id: String): Token = getToken(id).toSuspend()

/**
 * Get accepted brands by suspend function.
 *
 * @param tenantId tenant id (only for platformer)
 * @return accepted brands
 * @see [PayjpTokenService.getAcceptedBrands]
 */
suspend fun PayjpTokenService.getAcceptedBrandsSuspend(tenantId: TenantId? = null): CardBrandsAcceptedResponse =
    getAcceptedBrands(tenantId).toSuspend()

/**
 * coroutine extension for [Task].
 *
 * @return result of task
 */
suspend fun <T> Task<T>.toSuspend(): T = suspendCoroutine { cont ->
    try {
        cont.resume(run())
    } catch (e: Exception) {
        cont.resumeWithException(e)
    }
}
