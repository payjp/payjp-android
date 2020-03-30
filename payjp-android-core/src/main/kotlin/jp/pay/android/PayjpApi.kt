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
import jp.pay.android.model.Token
import jp.pay.android.network.ResultCall
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * PAYJP Token API
 *
 * see https://pay.jp/docs/api/#introduction
 */
internal interface PayjpApi {

    @POST("tokens")
    @FormUrlEncoded
    fun createToken(
        @Header("Authorization") authorization: String,
        @Field("card[number]") number: String,
        @Field("card[cvc]") cvc: String,
        @Field("card[exp_month]") expMonth: String,
        @Field("card[exp_year]") expYear: String,
        @Field("card[name]") name: String?,
        @Field("tenant") tenant: String?
    ): ResultCall<Token>

    @POST("tokens")
    @FormUrlEncoded
    fun createToken(
        @Header("Authorization") authorization: String,
        @Field("three_d_secure_token") tdsId: String
    ): ResultCall<Token>

    @GET("tokens/{id}")
    fun getToken(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): ResultCall<Token>

    @GET("accounts/brands")
    fun getAcceptedBrands(
        @Header("Authorization") authorization: String,
        @Query("tenant") tenant: String?
    ): ResultCall<CardBrandsAcceptedResponse>
}
