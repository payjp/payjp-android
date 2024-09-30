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
package jp.pay.android

import jp.pay.android.model.TenantId

/**
 * Parameter to create card token.
 *
 * @param number card number
 * @param cvc card cvc
 * @param expMonth card expiration month (zero-padding 2 digits e.g. January -> `01`)
 * @param expYear card expiration year (4 digits e.g. 2020 -> `2020`)
 * @param name card holder name
 * @param tenantId optional tenant id (only for platform)
 * @param email email
 * @param phone phone number formatted by E.164 (e.g. +819012345678)
 * @param threeDSecure whether use 3-D secure or not
 */
data class PayjpTokenParam(
    val number: String,
    val cvc: String,
    val expMonth: String,
    val expYear: String,
    val name: String?,
    val tenantId: TenantId?,
    val email: String?,
    val phone: String?,
    val threeDSecure: Boolean,
) {

    /**
     * Builder of [PayjpTokenParam]
     *
     * @param number card number
     * @param cvc cvc
     * @param expMonth expiration month
     * @param expYear expiration year
     */
    class Builder(
        private val number: String,
        private val cvc: String,
        private val expMonth: String,
        private val expYear: String
    ) {

        private var name: String? = null
        private var tenantId: TenantId? = null
        private var email: String? = null
        private var phone: String? = null
        private var threeDSecure: Boolean = false

        /**
         * Set card holder name
         * default is `null`
         *
         * @param name card holder name
         */
        fun name(name: String?): Builder = apply { this.name = name }

        /**
         * Set tenant id
         *
         * @param tenantId tenant id
         */
        fun tenantId(tenantId: TenantId?): Builder = apply { this.tenantId = tenantId }

        /**
         * Set email
         *
         * @param email email
         */
        fun email(email: String?): Builder = apply { this.email = email }

        /**
         * Set phone number
         *
         * @param phone phone number
         */
        fun phone(phone: String?): Builder = apply { this.phone = phone }

        /**
         * Set 3-D secure
         *
         * @param threeDSecure whether use 3-D secure or not
         */
        fun threeDSecure(threeDSecure: Boolean): Builder = apply { this.threeDSecure = threeDSecure }

        /**
         * Build param
         *
         * @return param object.
         */
        fun build(): PayjpTokenParam =
            PayjpTokenParam(number, cvc, expMonth, expYear, name, tenantId, email, phone, threeDSecure)
    }
}
