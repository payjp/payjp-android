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
package jp.pay.android.testing.mock

import jp.pay.android.PayjpTokenService
import jp.pay.android.anyNullable
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.Token
import jp.pay.android.util.Tasks
import org.mockito.Mockito.`when`
import java.io.IOException

internal class PayjpMockTokenServiceRecipes(private val mock: PayjpTokenService) {

    fun prepareBrandsNever() = apply {
        `when`(mock.getAcceptedBrands(anyNullable()))
            .thenReturn(Tasks.never())
    }

    fun prepareBrandsError(error: Throwable = IOException("omg")) = apply {
        `when`(mock.getAcceptedBrands(anyNullable()))
            .thenReturn(Tasks.failure(error))
    }

    fun prepareBrandsAll() = apply {
        `when`(mock.getAcceptedBrands(anyNullable()))
            .thenReturn(
                Tasks.success(
                    CardBrandsAcceptedResponse(
                        brands = CardBrand.values().filter { it != CardBrand.UNKNOWN }.toList(),
                        livemode = true
                    )
                )
            )
    }

    fun prepareBrandsVM() = apply {
        `when`(mock.getAcceptedBrands(anyNullable()))
            .thenReturn(
                Tasks.success(
                    CardBrandsAcceptedResponse(
                        brands = listOf(CardBrand.VISA, CardBrand.MASTER_CARD),
                        livemode = true
                    )
                )
            )
    }

    fun prepareTokenNever() = apply {
        `when`(mock.createToken(param = anyNullable()))
            .thenReturn(Tasks.never())
    }

    fun prepareTokenSuccess(token: Token) = apply {
        `when`(mock.createToken(param = anyNullable()))
            .thenReturn(Tasks.success(token))
    }

    fun prepareTokenError(error: Throwable = IOException("omg")) = apply {
        `when`(mock.createToken(param = anyNullable()))
            .thenReturn(Tasks.failure(error))
    }
}
