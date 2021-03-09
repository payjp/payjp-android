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
package jp.pay.android.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import jp.pay.android.fixtures.CARD_HAS_METADATA
import jp.pay.android.fixtures.CARD_ID_NULL
import jp.pay.android.fixtures.CARD_NO_3DS_STATUS
import jp.pay.android.fixtures.CARD_OK
import jp.pay.android.network.TokenApiClientFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * for [Card]
 */
@RunWith(AndroidJUnit4::class)
class CardTest {

    private val moshi: Moshi = TokenApiClientFactory.moshi

    @Test
    fun json_to_properties() {
        moshi.adapter(Card::class.java)
            .fromJson(CARD_OK)
            ?.apply {
                assertEquals(id, "car_e3ccd4e0959f45e7c75bacc4be90")
                assertEquals(name, null)
                assertEquals(last4, "4242")
                assertEquals(brand, CardBrand.VISA)
                assertEquals(expirationMonth, 2)
                assertEquals(expirationYear, 2020)
                assertEquals(fingerprint, "e1d8225886e3a7211127df751c86787f")
                assertEquals(livemode, false)
                assertEquals(created, Date(1442290383L * 1000))
                assertEquals(addressCity, "city")
                assertEquals(addressLine1, "line1")
                assertEquals(addressLine2, "line2")
                assertEquals(addressState, "Japan")
                assertEquals(addressZip, "1234567")
                assertEquals(addressZipCheck, "unchecked")
                assertEquals(country, "JP")
                assertEquals(customer, "cus_xxxxx")
                assertEquals(cvcCheck, "passed")
                assertEquals(metadata.isEmpty, true)
                assertEquals(threeDSecureStatus, ThreeDSecureStatus.VERIFIED)
            } ?: fail("card is null")
    }

    @Test(expected = JsonDataException::class)
    fun if_id_null_throw_exception() {
        moshi.adapter(Card::class.java)
            .fromJson(CARD_ID_NULL)
        fail("should throw exception")
    }

    @Test
    fun card_with_metadata() {
        moshi.adapter(Card::class.java)
            .fromJson(CARD_HAS_METADATA)
            ?.apply {
                assertEquals(id, "car_e3ccd4e0959f45e7c75bacc4be90")
                assertEquals(metadata.get("user_id"), "id")
                assertEquals(metadata.get("user_verified"), true)
                assertEquals(metadata.get("user_tags"), "[car, bike]")
            } ?: fail("card is null")
    }

    @Test
    fun allow_three_d_secure_status_null() {
        moshi.adapter(Card::class.java)
            .fromJson(CARD_NO_3DS_STATUS)
            ?.apply {
                assertEquals(threeDSecureStatus, null)
            }
    }
}
