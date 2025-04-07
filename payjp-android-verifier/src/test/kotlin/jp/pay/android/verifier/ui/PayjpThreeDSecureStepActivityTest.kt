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
package jp.pay.android.verifier.ui

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario.launchActivityForResult
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.PayjpTokenOperationObserverService
import jp.pay.android.PayjpTokenParam
import jp.pay.android.PayjpTokenService
import jp.pay.android.Task
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.model.TokenId
import jp.pay.android.verifier.PayjpVerifier
import jp.pay.android.verifier.R
import jp.pay.android.verifier.testing.PayjpVerifierTestRule
import jp.pay.android.verifier.testing.TestEntryActivity
import jp.pay.android.verifier.threeDSecure.getVerificationEntryUri
import jp.pay.android.verifier.threeDSecure.getVerificationFinishUri
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class PayjpThreeDSecureStepActivityTest {

    private companion object {
        const val TEST_TDS_REDIRECT_NAME = "test"
        const val TEST_PUBLIC_KEY = "pk_test_123"
    }

    @Mock
    private lateinit var mockTokenService: PayjpTokenService

    @get:Rule
    val payjpRule = PayjpVerifierTestRule(
        tokenService = object : PayjpTokenService {
            override fun getPublicKey(): String = mockTokenService.getPublicKey()

            override fun createToken(param: PayjpTokenParam): Task<Token> =
                mockTokenService.createToken(param)

            override fun finishTokenThreeDSecure(tokenId: TokenId): Task<Token> =
                mockTokenService.finishTokenThreeDSecure(tokenId)

            override fun getToken(id: String): Task<Token> = mockTokenService.getToken(id)

            override fun getAcceptedBrands(tenantId: TenantId?): Task<CardBrandsAcceptedResponse> =
                mockTokenService.getAcceptedBrands(tenantId)

            override fun getTokenOperationObserver(): PayjpTokenOperationObserverService =
                mockTokenService.getTokenOperationObserver()
        },
        threeDSecureRedirectName = TEST_TDS_REDIRECT_NAME
    )

    @Before
    fun setUp() {
        Intents.init()
        MockitoAnnotations.openMocks(this)
        Mockito.`when`(mockTokenService.getPublicKey())
            .thenReturn(TEST_PUBLIC_KEY)
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun open_tds_step_intent_with_token_id() {
        val tokenId = TokenId(id = "tok_123")
        val scenario = launchActivityForResult<TestEntryActivity>(
            Intent(ApplicationProvider.getApplicationContext(), TestEntryActivity::class.java)
        )
        scenario.onActivity {
            PayjpVerifier.startThreeDSecureFlow(
                tokenId = tokenId,
                activity = it
            )

            intended(hasComponent(PayjpThreeDSecureStepActivity::class.java.name))
            intended(hasExtra(PayjpThreeDSecureStepActivity.EXTRA_KEY_RESOURCE_ID, tokenId.id))
        }
    }

    @Test
    fun open_tds_intent_with_token_id() {
        val tokenId = TokenId(id = "tok_123")
        val expectedStartUri = getVerificationEntryUri(
            resourceId = tokenId.id,
            publicKey = TEST_PUBLIC_KEY,
            redirectUrlName = TEST_TDS_REDIRECT_NAME
        )
        val expectedCallbackUri = getVerificationFinishUri(tokenId.id)
        val expectedTitle = ApplicationProvider.getApplicationContext<Application>()
            .getString(R.string.payjp_verifier_card_verify_title)

        val intent = PayjpThreeDSecureStepActivity.createLaunchIntent(
            context = ApplicationProvider.getApplicationContext(),
            resourceId = tokenId.id
        )
        val scenario = launchActivity<PayjpThreeDSecureStepActivity>(intent)
        scenario.onActivity {
            intended(hasComponent(PayjpWebActivity::class.java.name))
            intended(hasExtra(PayjpWebActivity.EXTRA_KEY_START_URI, expectedStartUri.toString()))
            intended(hasExtra(PayjpWebActivity.EXTRA_KEY_CALLBACK_URI, expectedCallbackUri.toString()))
            intended(hasExtra(PayjpWebActivity.EXTRA_KEY_TITLE, expectedTitle))
        }
    }

    @Test
    fun finish_if_tds_flow_canceled_with_token_id() {
        val tokenId = TokenId(id = "tok_123")
        // prepare onActivityResult
        intending(
            hasComponent(PayjpWebActivity::class.java.name)
        ).respondWith(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))

        val intent = PayjpThreeDSecureStepActivity.createLaunchIntent(
            context = ApplicationProvider.getApplicationContext(),
            resourceId = tokenId.id
        )
        val scenario = launchActivityForResult<PayjpThreeDSecureStepActivity>(intent)
        assertThat(scenario.result.resultCode, `is`(Activity.RESULT_CANCELED))
        assertThat(PayjpThreeDSecureStepActivity.getResult().isCanceled(), `is`(true))
    }

    @Test
    fun finish_with_tokenId_result_from_received_intent() {
        val tokenId = TokenId(id = "tok_123")

        val intent = PayjpThreeDSecureStepActivity.createLaunchIntent(
            context = ApplicationProvider.getApplicationContext(),
            resourceId = tokenId.id
        )
        val callbackIntent = Intent(
            ApplicationProvider.getApplicationContext(),
            PayjpThreeDSecureStepActivity::class.java
        ).setData(getVerificationFinishUri(tokenId.id))

        val scenario = launchActivityForResult<PayjpThreeDSecureStepActivity>(intent)
        scenario.onActivity {
            it.onNewIntentInternal(callbackIntent)

            val result = PayjpThreeDSecureStepActivity.getResult()
            assertThat(result.isSuccess(), `is`(true))
            assertThat(result.retrieveTokenId(), `is`(tokenId))
            assertThat(scenario.result.resultCode, `is`(Activity.RESULT_OK))
        }
    }

    @Test
    fun finish_with_successResourceId_result_from_received_intent() {
        val resourceId = "car_789"

        val intent = PayjpThreeDSecureStepActivity.createLaunchIntent(
            context = ApplicationProvider.getApplicationContext(),
            resourceId = resourceId
        )
        val callbackIntent = Intent(
            ApplicationProvider.getApplicationContext(),
            PayjpThreeDSecureStepActivity::class.java
        ).setData(getVerificationFinishUri(resourceId))

        val scenario = launchActivityForResult<PayjpThreeDSecureStepActivity>(intent)
        scenario.onActivity {
            it.onNewIntentInternal(callbackIntent)

            val result = PayjpThreeDSecureStepActivity.getResult()
            assertThat(result.isSuccess(), `is`(true))
            assertThat(result is PayjpThreeDSecureResult.SuccessResourceId, `is`(true))
            assertThat((result as PayjpThreeDSecureResult.SuccessResourceId).id, `is`(resourceId))
            assertThat(scenario.result.resultCode, `is`(Activity.RESULT_OK))
        }
    }
}
