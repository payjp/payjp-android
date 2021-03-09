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
import jp.pay.android.network.TokenApiClientFactory
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PayjpResourceObjectTest {

    private val moshi: Moshi = TokenApiClientFactory.moshi

    @Test
    fun fromJson_ok() {
        moshi.adapter(PayjpResourceObject::class.java)
            .fromJson(OBJECT_TDS_TOKEN)
            ?.run {
                assertThat(id, `is`("tds_abc"))
                assertThat(name, `is`("three_d_secure_token"))
            } ?: fail("object is null")
    }

    @Test(expected = JsonDataException::class)
    fun fromJson_ng_no_name() {
        moshi.adapter(PayjpResourceObject::class.java)
            .fromJson(OBJECT_NO_NAME)
    }

    @Test(expected = JsonDataException::class)
    fun fromJson_ng_no_id() {
        moshi.adapter(PayjpResourceObject::class.java)
            .fromJson(OBJECT_NO_ID)
    }
}

const val OBJECT_TDS_TOKEN =
    """
{
  "object": "three_d_secure_token",
  "id": "tds_abc"
}
"""

const val OBJECT_NO_NAME =
    """
{
  "id": "tds_abc"
}
"""

const val OBJECT_NO_ID =
    """
{
  "object": "foo"
}
"""
