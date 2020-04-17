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

import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.PayjpConstants
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThreeDSecureTokenTest {
    @Test
    fun getTdsEntryUri() {
        val id = "tds_xxx"
        val token = ThreeDSecureToken(id)
        val uri = token.getVerificationEntryUri(
            publicKey = "pk_zzzz"
        )
        assertThat(
            uri.toString(),
            `is`("${PayjpConstants.API_ENDPOINT}tds/$id/start?publickey=pk_zzzz")
        )
    }

    @Test
    fun getTdsEntryUri_withRedirect() {
        val id = "tds_xxx"
        val token = ThreeDSecureToken(id)
        val uri = token.getVerificationEntryUri(
            publicKey = "pk_zzzz",
            redirectUrlName = "app"
        )
        assertThat(
            uri.toString(),
            `is`("${PayjpConstants.API_ENDPOINT}tds/$id/start?publickey=pk_zzzz&back=app")
        )
    }

    @Test
    fun getTdsFinishUri() {
        val id = "tds_xxx"
        val token = ThreeDSecureToken(id)
        assertThat(
            token.getVerificationFinishUri().toString(),
            `is`("${PayjpConstants.API_ENDPOINT}tds/$id/finish")
        )
    }
}
