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
package jp.pay.android.model

import android.os.Bundle
import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import java.util.Date

/**
 * PAY.JP card object.
 * For security reasons, the card number is masked and you can get only last4 character.
 * The full documentations are following.
 * cf. [https://pay.jp/docs/api/#cardオブジェクト](https://pay.jp/docs/api/#cardオブジェクト)
 */
@JsonClass(generateAdapter = true)
@Parcelize
data class Card(
    val id: String,
    val name: String?,
    val last4: String,
    val brand: CardBrand,
    @Json(name = "exp_month") val expirationMonth: Int,
    @Json(name = "exp_year") val expirationYear: Int,
    val fingerprint: String,
    val livemode: Boolean,
    val created: Date,
    @Json(name = "address_state") val addressState: String?,
    @Json(name = "address_city") val addressCity: String?,
    @Json(name = "address_line1") val addressLine1: String?,
    @Json(name = "address_line2") val addressLine2: String?,
    val country: String?,
    @Json(name = "address_zip") val addressZip: String?,
    @Json(name = "address_zip_check") val addressZipCheck: String,
    val customer: String?,
    @Json(name = "cvc_check") val cvcCheck: String,
    val metadata: Bundle,
    @Json(name = "three_d_secure_status") val threeDSecureStatus: ThreeDSecureStatus?
) : Parcelable {

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        return other === this || (other is Card && other.id == id)
    }
}
