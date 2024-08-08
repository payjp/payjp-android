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
package jp.pay.android.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.model.CountryCode
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class PhoneNumberServiceImplTest {

    @Test
    fun normalize_valid() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = PhoneNumberServiceImpl(Locale.US)
        val phoneNumber = "09012345678"
        val countryCodeJP = CountryCode("JP", 81, Locale.US)
        val normalized = service.normalize(context, phoneNumber, countryCodeJP)
        assertThat(normalized, `is`("+819012345678"))
    }

    @Test
    fun normalize_invalid() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = PhoneNumberServiceImpl(Locale.US)
        val phoneNumber = "0"
        val countryCodeJP = CountryCode("JP", 81, Locale.US)
        val normalized = service.normalize(context, phoneNumber, countryCodeJP)
        assertThat(normalized, `is`(nullValue()))
    }

    @Test
    fun normalize_illegal() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = PhoneNumberServiceImpl(Locale.US)
        val phoneNumber = "foo"
        val countryCodeJP = CountryCode("JP", 81, Locale.US)
        val normalized = service.normalize(context, phoneNumber, countryCodeJP)
        assertThat(normalized, `is`(nullValue()))
    }
}
