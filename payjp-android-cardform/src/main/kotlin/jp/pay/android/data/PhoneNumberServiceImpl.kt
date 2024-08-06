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
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import jp.pay.android.model.CountryCode
import java.util.Locale

internal class PhoneNumberServiceImpl(
    private val locale: Locale
) : PhoneNumberService {
    private var phoneNumberUtilOrNull: PhoneNumberUtil? = null
    private fun phoneNumberUtil(context: Context): PhoneNumberUtil {
        return phoneNumberUtilOrNull ?: PhoneNumberUtil.createInstance(context.applicationContext).also {
            phoneNumberUtilOrNull = it
        }
    }
    private var all: List<CountryCode>? = null

    override fun getAllCountryCodes(context: Context): List<CountryCode> {
        return all ?: run {
            val result = phoneNumberUtil(context).supportedRegions.map {
                CountryCode(it, phoneNumberUtil(context).getCountryCodeForRegion(it), locale)
            }
            all = result
            result
        }
    }

    override fun findCountryCodeByRegion(context: Context, region: String): CountryCode? {
        return phoneNumberUtil(context).getCountryCodeForRegion(region).takeIf { it > 0 }?.let {
            CountryCode(region, it, locale)
        }
    }

    override fun defaultCountryCode(): CountryCode {
        // hardcode to JP
        return CountryCode("JP", 81, locale)
    }

    override fun normalize(context: Context, phoneNumber: String, countryCode: CountryCode): String? {
        return try {
            phoneNumberUtil(context).run {
                parse(phoneNumber, countryCode.region)
                    .takeIf { isValidNumberForRegion(it, countryCode.region) }
                    ?.let { format(it, PhoneNumberUtil.PhoneNumberFormat.E164) }
            }
        } catch (e: NumberParseException) {
            null
        }
    }

    override fun examplePhoneNumber(context: Context, countryCode: CountryCode): String {
        return phoneNumberUtil(context)
            .getExampleNumberForType(countryCode.region, PhoneNumberUtil.PhoneNumberType.MOBILE)
            .nationalNumber
            .toString()
    }
}
