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

import android.os.Parcelable
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson
import java.util.Locale
import kotlinx.android.parcel.Parcelize

/**
 * Card verification status of 3-D Secure
 *
 */
@Parcelize
enum class ThreeDSecureStatus : Parcelable {
    /**
     * The card object has not been verified, and the verification is required.
     */
    UNVERIFIED,
    /**
     * The card object has been verified.
     */
    VERIFIED,
    /**
     * The card 3-D Secure status check has been failed.
     */
    FAILED,
    /**
     * The card object has not been verified, but the verification is optional.
     */
    ATTEMPTED,
    /**
     * The card 3-D Secure status check has been aborted.
     */
    ABORTED,
    /**
     * The card 3-D Secure status check error has been occurred.
     */
    ERROR,
    ;

    /**
     * Moshi json adapter for ThreeDSecureStatus
     */
    class JsonAdapter {

        @ToJson
        fun toJson(status: ThreeDSecureStatus): String = status.name.toLowerCase(Locale.US)

        @FromJson
        fun fromJson(status: String): ThreeDSecureStatus {
            return values().firstOrNull { it.name.toLowerCase(Locale.US) == status }
                ?: throw JsonDataException("unknown status: $status")
        }
    }
}
