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
package jp.pay.android.fixtures

const val ERROR_AUTH = """
{
  "error": {
    "message": "Invalid API Key: {0}",
    "status": 401,
    "type": "auth_error"
  }
}
"""

const val ERROR_CARD_DECLINED = """
{
  "error": {
    "code": "card_declined",
    "message": "Card declined",
    "status": 402,
    "type": "card_error"
  }
}
"""

const val ERROR_INVALID_ID = """
{
  "error": {
    "code": "invalid_id",
    "message": "No such token: tok_587af2665fdced4742e5fbb3ecfcaa",
    "param": "id",
    "status": 404,
    "type": "client_error"
  }
}
"""
