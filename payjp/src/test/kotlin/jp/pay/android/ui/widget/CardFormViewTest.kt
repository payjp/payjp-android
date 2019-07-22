/*
 *
 * Copyright (c) 2019 PAY, Inc.
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
package jp.pay.android.ui.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.CardRobot
import jp.pay.android.PayjpToken
import jp.pay.android.PayjpTokenService
import jp.pay.android.R
import jp.pay.android.TestStubs
import jp.pay.android.exception.PayjpInvalidCardFormException
import jp.pay.android.util.Tasks
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class CardFormViewTest {

    private lateinit var cardFormView: CardFormView
    @Mock
    lateinit var mockTokenService: PayjpTokenService
    @Mock
    lateinit var mockValidateInputListener: TokenCreatableView.OnValidateInputListener

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val context: Context = ApplicationProvider.getApplicationContext()
        // require appcompat to
        context.setTheme(R.style.Theme_AppCompat)
        // Set up because it called from inside of CardFormView.
        // It will be replaced with mock.
        PayjpToken.init("")
        cardFormView = CardFormView(context).apply {
            tokenService = mockTokenService
        }
        `when`(mockTokenService.createToken(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Tasks.success(TestStubs.newToken()))
    }

    @Test
    fun isValid_default_false() {
        assertThat(cardFormView.isValid, `is`(false))
    }

    @Test
    fun isValid_after_correct_input() {
        CardRobot().input(cardFormView)
        assertThat(cardFormView.isValid, `is`(true))
    }

    @Test
    fun not_required_card_holder_name_if_not_enabled() {
        CardRobot().input(cardFormView)
        cardFormView.holderNameEditText.text = null
        assertThat(cardFormView.isValid, `is`(false))
        cardFormView.setCardHolderNameInputEnabled(false)
        assertThat(cardFormView.isValid, `is`(true))
    }

    @Test
    fun validateCardForm_false_without_input() {
        assertThat(cardFormView.validateCardForm(), `is`(false))
    }

    @Test
    fun validateCardForm_true_with_correct_input() {
        CardRobot().input(cardFormView)
        assertThat(cardFormView.validateCardForm(), `is`(true))
    }

    @Test
    fun onValidateInput_update() {
        // 1. set listener (call immediately)
        cardFormView.setOnValidateInputListener(mockValidateInputListener)
        verify(mockValidateInputListener).onValidateInput(cardFormView, false)
        clearInvocations(mockValidateInputListener)

        // 2. input something (not valid)
        cardFormView.numberEditText.setText("4242424242424242")
        verify(mockValidateInputListener).onValidateInput(cardFormView, false)
        clearInvocations(mockValidateInputListener)

        // 3. input correctly
        CardRobot().input(cardFormView)
        verify(mockValidateInputListener).onValidateInput(cardFormView, true)
        clearInvocations(mockValidateInputListener)

        // 4. input incorrectly
        cardFormView.expirationEditText.text = null
        verify(mockValidateInputListener).onValidateInput(cardFormView, false)
    }

    @Test(expected = PayjpInvalidCardFormException::class)
    fun createToken_skip_if_not_valid() {
        cardFormView.createToken().run()
    }

    @Test
    fun createToken_if_valid() {
        val robot = CardRobot(
            number = "4242424242424242",
            exp = "12/30",
            cvc = "123",
            name = "TARO YAMADA"
        )
        robot.input(cardFormView)
        cardFormView.createToken().run()
        verify(mockTokenService).createToken(
            number = robot.number,
            expMonth = "12",
            expYear = "2030",
            cvc = robot.cvc,
            name = robot.name
        )
    }
}