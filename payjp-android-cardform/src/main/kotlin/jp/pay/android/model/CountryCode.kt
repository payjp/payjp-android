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
package jp.pay.android.model

import java.util.Locale

internal data class CountryCode(val region: String, val code: Int, val displayLocale: Locale = Locale.getDefault()) {
    val emoji: String? = createEmoji(region)
    val locale: Locale = Locale("", region)
    val shortName: String
        get() = "$emoji (+$code)"
    val searchDescription: String
        get() = "$emoji $region ${locale.getDisplayCountry(displayLocale)} (+$code)"

    companion object {
        /**
         * Create emoji from region code.
         * @param region region code (e.g. JP)
         * @return emoji (e.g. 🇯🇵)
         */
        fun createEmoji(region: String): String? {
            if (region.length != 2) {
                return null
            }
            return region.uppercase()
                .map { char ->
                    Character.codePointAt("$char", 0) - 0x41 + 0x1F1E6
                }.joinToString("") {
                    String(Character.toChars(it))
                }
        }
    }
}
