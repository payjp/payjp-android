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
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Date
import jp.pay.android.TestStubs
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SerializersKtTest {

    private val cardFulFilled: Card by lazy {
        TestStubs.newCard(
            id = "car_1",
            name = "n",
            last4 = "1234",
            brand = CardBrand.VISA,
            created = Date(1577804400L * 1000),
            expirationMonth = 1,
            expirationYear = 2050,
            fingerprint = "f",
            livemode = true,
            customer = "c",
            country = "JP",
            addressState = "state",
            addressZip = "zip",
            addressCity = "city",
            addressLine1 = "line1",
            addressLine2 = "line2",
            addressZipCheck = "checked",
            cvcCheck = "checked",
            metadata = Bundle().apply {
                putString("meta_a", "a")
                putBoolean("meta_true", true)
            }
        )
    }

    private val cardHavingNull: Card by lazy {
        cardFulFilled.copy(
            name = null,
            customer = null,
            country = null,
            addressState = null,
            addressZip = null,
            addressCity = null,
            addressLine1 = null,
            addressLine2 = null,
            metadata = Bundle.EMPTY
        )
    }

    private val token: Token by lazy {
        TestStubs.newToken(
            id = "tok_1",
            card = cardFulFilled,
            livemode = true,
            used = false,
            created = Date(1577804400L * 1000)
        )
    }

    @Test
    fun cardToJson() {
        assertThat(cardFulFilled.toJson(), notNullValue())
    }

    @Test
    fun cardToJsonValue_fulfilled() {
        cardFulFilled.toJsonValue().let { map ->
            assertThat(map, notNullValue())
            assertThat<Map<String, Any?>>(map, hasEntry("id", "car_1"))
            assertThat<Map<String, Any?>>(map, hasEntry("name", "n"))
            assertThat<Map<String, Any?>>(map, hasEntry("last4", "1234"))
            assertThat<Map<String, Any?>>(map, hasEntry("brand", "Visa"))
            assertThat<Map<String, Any?>>(map, hasEntry("created", 1577804400000L))
            assertThat<Map<String, Any?>>(map, hasEntry("exp_month", 1L))
            assertThat<Map<String, Any?>>(map, hasEntry("exp_year", 2050L))
            assertThat<Map<String, Any?>>(map, hasEntry("fingerprint", "f"))
            assertThat<Map<String, Any?>>(map, hasEntry("livemode", true))
            assertThat<Map<String, Any?>>(map, hasEntry("customer", "c"))
            assertThat<Map<String, Any?>>(map, hasEntry("country", "JP"))
            assertThat<Map<String, Any?>>(map, hasEntry("address_state", "state"))
            assertThat<Map<String, Any?>>(map, hasEntry("address_zip", "zip"))
            assertThat<Map<String, Any?>>(map, hasEntry("address_city", "city"))
            assertThat<Map<String, Any?>>(map, hasEntry("address_line1", "line1"))
            assertThat<Map<String, Any?>>(map, hasEntry("address_line2", "line2"))
            assertThat<Map<String, Any?>>(map, hasEntry("address_zip_check", "checked"))
            assertThat<Map<String, Any?>>(map, hasEntry("cvc_check", "checked"))
            assertThat<Map<String, Any?>>(
                map,
                hasEntry("metadata", mapOf("meta_a" to "a", "meta_true" to true))
            )
        }
    }

    @Test
    fun cardToJsonValue_havingNull() {
        cardHavingNull.toJsonValue().let { map ->
            assertThat(map, notNullValue())
            assertThat<Map<String, Any?>>(map, hasEntry("name", null))
            assertThat<Map<String, Any?>>(map, hasEntry("customer", null))
            assertThat<Map<String, Any?>>(map, hasEntry("country", null))
            assertThat<Map<String, Any?>>(map, hasEntry("address_state", null))
            assertThat<Map<String, Any?>>(map, hasEntry("address_zip", null))
            assertThat<Map<String, Any?>>(map, hasEntry("address_city", null))
            assertThat<Map<String, Any?>>(map, hasEntry("address_line1", null))
            assertThat<Map<String, Any?>>(map, hasEntry("address_line2", null))
            assertThat((map["metadata"] as? Map<*, *>)?.isEmpty(), `is`(true))
        }
    }

    @Test
    fun tokenToJson() {
        assertThat(token.toJson(), notNullValue())
    }

    @Test
    fun tokenToJsonValue() {
        token.toJsonValue().let { token ->
            assertThat<Map<String, Any?>>(token, hasEntry("id", "tok_1"))
            assertThat<Map<String, Any?>>(token, hasEntry("livemode", true))
            assertThat<Map<String, Any?>>(token, hasEntry("used", false))
            assertThat<Map<String, Any?>>(token, hasEntry("created", 1577804400000L))
            assertThat(token["card"], instanceOf(Map::class.java))
            (token["card"] as Map<*, *>).let { card ->
                assertThat<Map<*, *>>(card, hasEntry("id", "car_1"))
                assertThat<Map<*, *>>(card, hasEntry("name", "n"))
            }
        }
    }
}
