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
            assertThat(map, hasEntry<String, Any>("id", "car_1"))
            assertThat(map, hasEntry<String, Any>("name", "n"))
            assertThat(map, hasEntry<String, Any>("last4", "1234"))
            assertThat(map, hasEntry<String, Any>("brand", "Visa"))
            assertThat(map, hasEntry<String, Any>("created", 1577804400000L))
            assertThat(map, hasEntry<String, Any>("exp_month", 1L))
            assertThat(map, hasEntry<String, Any>("exp_year", 2050L))
            assertThat(map, hasEntry<String, Any>("fingerprint", "f"))
            assertThat(map, hasEntry<String, Any>("livemode", true))
            assertThat(map, hasEntry<String, Any>("customer", "c"))
            assertThat(map, hasEntry<String, Any>("country", "JP"))
            assertThat(map, hasEntry<String, Any>("address_state", "state"))
            assertThat(map, hasEntry<String, Any>("address_zip", "zip"))
            assertThat(map, hasEntry<String, Any>("address_city", "city"))
            assertThat(map, hasEntry<String, Any>("address_line1", "line1"))
            assertThat(map, hasEntry<String, Any>("address_line2", "line2"))
            assertThat(map, hasEntry<String, Any>("address_zip_check", "checked"))
            assertThat(map, hasEntry<String, Any>("cvc_check", "checked"))
            @Suppress("RemoveExplicitTypeArguments")
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
            assertThat(map, hasEntry<String, Any>("name", null))
            assertThat(map, hasEntry<String, Any>("customer", null))
            assertThat(map, hasEntry<String, Any>("country", null))
            assertThat(map, hasEntry<String, Any>("address_state", null))
            assertThat(map, hasEntry<String, Any>("address_zip", null))
            assertThat(map, hasEntry<String, Any>("address_city", null))
            assertThat(map, hasEntry<String, Any>("address_line1", null))
            assertThat(map, hasEntry<String, Any>("address_line2", null))
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
            assertThat(token, hasEntry<String, Any>("id", "tok_1"))
            assertThat(token, hasEntry<String, Any>("livemode", true))
            assertThat(token, hasEntry<String, Any>("used", false))
            assertThat(token, hasEntry<String, Any>("created", 1577804400000L))
            assertThat(token["card"], instanceOf(Map::class.java))
            (token["card"] as Map<*, *>).let { card ->
                assertThat(card, hasEntry<Any?, Any?>("id", "car_1"))
                assertThat(card, hasEntry<Any?, Any?>("name", "n"))
            }
        }
    }
}
