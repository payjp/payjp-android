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

import android.net.Uri
import android.os.Parcelable
import jp.pay.android.PayjpConstants
import kotlinx.android.parcel.Parcelize

/**
 *
 * @param id id
 */
@Parcelize
data class ThreeDSecureToken(val id: String) : Parcelable {
    private fun getTdsBaseUri(): Uri = Uri.parse(PayjpConstants.API_ENDPOINT)
        .buildUpon()
        .appendPath("tds")
        .appendPath(id)
        .build()

    fun getTdsEntryUri(publicKey: String, redirectUrlName: String? = null): Uri = getTdsBaseUri()
        .buildUpon()
        .appendPath("start")
        .appendQueryParameter("publickey", publicKey)
        .apply {
            if (redirectUrlName != null) {
                appendQueryParameter("back", redirectUrlName)
            }
        }
        .build()

    fun getTdsFinishUri(): Uri = getTdsBaseUri()
        .buildUpon()
        .appendPath("finish")
        .build()
}
