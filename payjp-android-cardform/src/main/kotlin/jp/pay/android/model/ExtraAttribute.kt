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

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Extra attributes for card form.
 * For now it is mainly used for 3-D Secure.
 * see [https://help.pay.jp/ja/articles/9556161]
 */
sealed class ExtraAttribute<T> : Parcelable {
    abstract val preset: T?

    /**
     * Email attribute.
     * @param preset preset value for card form.
     */
    @Parcelize
    data class Email(
        override val preset: String? = null
    ) : ExtraAttribute<String>()

    /**
     * Phone attribute.
     * @param region region code for phone number. (ISO 3166-1 alpha-2)
     * @param number preset phone number.
     */
    @Parcelize
    data class Phone(
        val region: String,
        val number: String? = null,
    ) : ExtraAttribute<Pair<String, String?>>() {
        override val preset: Pair<String, String?>
            get() = region to number
    }

    companion object {
        /**
         * Default 3-D Secure attributes.
         * It is available to change from this.
         * for example following cases:
         * - add preset values.
         * - only use email or phone.
         * - never request email nor phone.
         */
        fun defaults(): Array<ExtraAttribute<*>> = arrayOf(Email(), Phone(region = "JP"))
    }
}
