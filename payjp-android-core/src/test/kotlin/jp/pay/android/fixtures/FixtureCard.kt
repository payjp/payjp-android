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
package jp.pay.android.fixtures

const val CARD_OK =
    """
{
    "address_city": "city",
    "address_line1": "line1",
    "address_line2": "line2",
    "address_state": "Japan",
    "address_zip": "1234567",
    "address_zip_check": "unchecked",
    "brand": "Visa",
    "country": "JP",
    "created": 1442290383,
    "customer": "cus_xxxxx",
    "cvc_check": "passed",
    "exp_month": 2,
    "exp_year": 2020,
    "fingerprint": "e1d8225886e3a7211127df751c86787f",
    "id": "car_e3ccd4e0959f45e7c75bacc4be90",
    "livemode": false,
    "metadata": {},
    "last4": "4242",
    "name": null,
    "object": "card",
    "three_d_secure_status": "verified"
}
        """

const val CARD_ID_NULL =
    """
{
    "address_city": null,
    "address_line1": null,
    "address_line2": null,
    "address_state": null,
    "address_zip": null,
    "address_zip_check": "unchecked",
    "brand": "Visa",
    "country": null,
    "created": 1442290383,
    "customer": null,
    "cvc_check": "passed",
    "exp_month": 2,
    "exp_year": 2020,
    "fingerprint": "e1d8225886e3a7211127df751c86787f",
    "id": null,
    "livemode": false,
    "metadata": {},
    "last4": "4242",
    "name": null,
    "object": "card",
    "three_d_secure_status": "verified"
}
"""

const val CARD_HAS_METADATA =
    """
{
    "address_city": null,
    "address_line1": null,
    "address_line2": null,
    "address_state": null,
    "address_zip": null,
    "address_zip_check": "unchecked",
    "brand": "Visa",
    "country": null,
    "created": 1442290383,
    "customer": null,
    "cvc_check": "passed",
    "exp_month": 2,
    "exp_year": 2020,
    "fingerprint": "e1d8225886e3a7211127df751c86787f",
    "id": "car_e3ccd4e0959f45e7c75bacc4be90",
    "livemode": false,
    "metadata": {
      "user_id": "id",
      "user_verified": true,
      "user_tags": [
        "car", "bike"
      ]
    },
    "last4": "4242",
    "name": null,
    "object": "card"
}
        """

const val CARD_NO_3DS_STATUS =
    """
{
    "address_city": null,
    "address_line1": null,
    "address_line2": null,
    "address_state": null,
    "address_zip": null,
    "address_zip_check": "unchecked",
    "brand": "Visa",
    "country": null,
    "created": 1442290383,
    "customer": null,
    "cvc_check": "passed",
    "exp_month": 2,
    "exp_year": 2020,
    "fingerprint": "e1d8225886e3a7211127df751c86787f",
    "id": "car_e3ccd4e0959f45e7c75bacc4be90",
    "livemode": false,
    "metadata": {},
    "last4": "4242",
    "name": null,
    "object": "card",
    "three_d_secure_status": null
}
        """
