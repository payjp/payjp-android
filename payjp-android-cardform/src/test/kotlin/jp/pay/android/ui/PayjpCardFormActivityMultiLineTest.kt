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
package jp.pay.android.ui

import android.app.Activity
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.CardRobot
import jp.pay.android.PayjpTokenOperationObserverService
import jp.pay.android.PayjpTokenOperationStatus
import jp.pay.android.PayjpTokenParam
import jp.pay.android.PayjpTokenService
import jp.pay.android.Task
import jp.pay.android.TestStubs
import jp.pay.android.anyNullable
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.TenantId
import jp.pay.android.model.ThreeDSecureToken
import jp.pay.android.model.Token
import jp.pay.android.model.TokenId
import jp.pay.android.testing.FakeTokenOperationObserver
import jp.pay.android.testing.PayjpCardFormTestRule
import jp.pay.android.testing.assertion.withItemCount
import jp.pay.android.testing.mock.PayjpMockTokenServiceRecipes
import jp.pay.android.ui.pageobject.CardFormPage
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class PayjpCardFormActivityMultiLineTest {

    @Mock
    private lateinit var mockTokenService: PayjpTokenService
    private lateinit var mockRecipes: PayjpMockTokenServiceRecipes

    @get:Rule
    val payjpRule = PayjpCardFormTestRule(
        tokenService = object : PayjpTokenService {
            override fun getPublicKey(): String = mockTokenService.getPublicKey()

            override fun createToken(param: PayjpTokenParam): Task<Token> =
                mockTokenService.createToken(param)

            override fun createToken(threeDSecureToken: ThreeDSecureToken): Task<Token> =
                mockTokenService.createToken(threeDSecureToken)

            override fun finishTokenThreeDSecure(tokenId: TokenId): Task<Token> =
                mockTokenService.finishTokenThreeDSecure(tokenId)

            override fun getToken(id: String): Task<Token> = mockTokenService.getToken(id)

            override fun getAcceptedBrands(tenantId: TenantId?): Task<CardBrandsAcceptedResponse> =
                mockTokenService.getAcceptedBrands(tenantId)

            override fun getTokenOperationObserver(): PayjpTokenOperationObserverService =
                FakeTokenOperationObserver
        }
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mockRecipes = PayjpMockTokenServiceRecipes(mockTokenService)
        FakeTokenOperationObserver.reset()
    }

    @Test
    fun contentLoadingProgress_display_until_get_accepted_brands() {
        mockRecipes.prepareBrandsNever()

        CardFormPage.run {
            launchMultiLine()

            check {
                contentLoadingProgress(isDisplayed())
                errorMessage(not(isDisplayed()))
                submitButton(not(isDisplayed()))
            }
        }
    }

    @Test
    fun errorView_display_if_fail_to_get_accepted_brands() {
        mockRecipes.prepareBrandsError()

        CardFormPage.run {
            launchMultiLine()

            check {
                errorMessage(isDisplayed())
                contentLoadingProgress(not(isDisplayed()))
                submitButton(not(isDisplayed()))
                reloadButton(isDisplayed())
            }

            perform {
                clickReloadButton()
            }
        }

        verify(mockTokenService, times(2))
            .getAcceptedBrands(anyNullable())
    }

    @Test
    fun launch() {
        mockRecipes.prepareBrandsVM()

        CardFormPage.run {
            launchMultiLine()

            check {
                contentLoadingProgress(not(isDisplayed()))
                acceptedBrands(isDisplayed())
                submitButton(isDisplayed())
                submitButton(not(isEnabled()))
            }
        }
    }

    @Test
    fun button_is_enabled_after_input_correct_card() {
        mockRecipes.prepareBrandsVM()

        CardFormPage.run {
            launchMultiLine()

            perform {
                inputCard(CardRobot.SandboxVisa)
            }

            check {
                submitButton(isEnabled())
            }
        }
    }

    @Test
    fun brands_size() {
        mockRecipes.prepareBrandsAll()

        CardFormPage.run {
            launchMultiLine()

            check {
                acceptedBrands(withItemCount(6))
            }
        }
    }

    @Test
    fun focus_on_next_input() {
        mockRecipes.prepareBrandsAll()
        val robot = CardRobot.SandboxVisa

        CardFormPage.run {
            launchMultiLine()

            perform {
                clickInputNumber()
                inputNumber(robot.number)
            }
            check { inputExpiration(hasFocus()) }
            perform { inputExpiration(robot.exp) }
            check { inputCvc(hasFocus()) }
            perform { inputCvc(robot.cvc) }
            check { inputHolderName(hasFocus()) }
        }
    }

    @Test
    fun button_progress_is_displayed_when_you_submit() {
        mockRecipes.prepareBrandsVM().prepareTokenNever()

        CardFormPage.run {
            launchMultiLine()

            perform {
                inputCard(CardRobot.SandboxVisa)
                clickSubmitButton()
            }

            check {
                submitButtonProgressBar(isDisplayed())
                submitButton(not(isDisplayed()))
            }
        }
    }

    @Test
    fun button_progress_is_displayed_until_create_token_acceptable() {
        mockRecipes.prepareBrandsVM().prepareTokenError()

        CardFormPage.run {
            launchMultiLine()

            perform {
                inputCard(CardRobot.SandboxVisa)
                clickSubmitButton()
            }

            FakeTokenOperationObserver.status = PayjpTokenOperationStatus.THROTTLED

            check {
                submitButtonProgressBar(isDisplayed())
                submitButton(not(isDisplayed()))
            }

            FakeTokenOperationObserver.status = PayjpTokenOperationStatus.ACCEPTABLE

            check {
                submitButtonProgressBar(not(isDisplayed()))
                submitButton(isDisplayed())
            }
        }
    }

    @Test
    fun finish_with_ok_after_received_token() {
        val token = TestStubs.newToken()
        mockRecipes.prepareBrandsVM().prepareTokenSuccess(token)

        CardFormPage.run {
            val scenario = launchMultiLine()

            perform {
                inputCard(CardRobot.SandboxVisa)
                clickSubmitButton()
            }

            scenario.result.run {
                assertThat(resultCode, `is`(Activity.RESULT_OK))
                PayjpCardFormActivity.onActivityResult(resultData) {
                    assertThat(it.isSuccess(), `is`(true))
                    assertThat(it.retrieveToken(), `is`(token))
                }
            }
        }
    }
}
