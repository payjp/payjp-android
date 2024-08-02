/*
 *
 * Copyright (c) 2024 PAY, Inc.
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
import jp.pay.android.model.CountryCode

/**
 * Service for phone number.
 */
internal interface PhoneNumberService {

    /**
     * Return all country codes.
     */
    fun getAllCountryCodes(context: Context): List<CountryCode>

    /**
     * Find country code by region.
     */
    fun findCountryCodeByRegion(context: Context, region: String): CountryCode?

    /**
     * Return default country code.
     */
    fun defaultCountryCode(): CountryCode

    /**
     * Return normalized phone number formatted by E.164.
     * @param context context
     * @param phoneNumber phone number
     * @param countryCode country code
     * @return normalized phone number or null if failed to parse
     */
    fun normalize(context: Context, phoneNumber: String, countryCode: CountryCode): String?

    /**
     * Return example phone number.
     * @param context context
     * @param countryCode country code
     */
    fun examplePhoneNumber(context: Context, countryCode: CountryCode): String
}
