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
package jp.pay.android.ui.pageobject

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import jp.pay.android.CardRobot
import jp.pay.android.PayjpCardForm
import jp.pay.android.R
import jp.pay.android.ui.PayjpCardFormActivity
import org.hamcrest.Matcher

internal object CardFormPage {

    fun launchCardDisplay(): ActivityScenario<PayjpCardFormActivity> {
        return launchActivity(
            PayjpCardFormActivity.createIntent(
                context = ApplicationProvider.getApplicationContext(),
                tenant = null,
                face = PayjpCardForm.FACE_CARD_DISPLAY
            )
        )
    }

    fun launchMultiLine(): ActivityScenario<PayjpCardFormActivity> {
        return launchActivity(
            PayjpCardFormActivity.createIntent(
                context = ApplicationProvider.getApplicationContext(),
                tenant = null,
                face = PayjpCardForm.FACE_MULTI_LINE
            )
        )
    }

    fun check(block: Assertion.() -> Unit) {
        block(Assertion)
    }

    fun perform(block: Interaction.() -> Unit) {
        block(Interaction)
    }

    object Assertion {

        fun contentLoadingProgress(viewMatcher: Matcher<in View>) {
            onView(withId(R.id.content_loading_progress))
                .check(matches(viewMatcher))
        }

        fun errorMessage(viewMatcher: Matcher<in View>) {
            onView(withId(R.id.error_message))
                .check(matches(viewMatcher))
        }

        fun reloadButton(viewMatcher: Matcher<in View>) {
            onView(withId(R.id.reload_content_button))
                .check(matches(viewMatcher))
        }

        fun submitButton(viewMatcher: Matcher<in View>) {
            onView(withId(R.id.card_form_button))
                .check(matches(viewMatcher))
        }

        fun submitButtonProgressBar(viewMatcher: Matcher<in View>) {
            onView(withId(R.id.card_form_button_progress))
                .check(matches(viewMatcher))
        }

        fun acceptedBrands(viewMatcher: Matcher<in View>) {
            acceptedBrands(matches(viewMatcher))
        }

        fun acceptedBrands(viewAssertion: ViewAssertion) {
            onView(withId(R.id.accepted_brands)).check(viewAssertion)
        }

        fun cardDisplay(viewMatcher: Matcher<in View>) {
            onView(withId(R.id.card_display))
                .check(matches(viewMatcher))
        }

        fun inputExpiration(viewMatcher: Matcher<in View>) {
            onView(withId(R.id.input_edit_expiration))
                .check(matches(viewMatcher))
        }

        fun inputCvc(viewMatcher: Matcher<in View>) {
            onView(withId(R.id.input_edit_cvc))
                .check(matches(viewMatcher))
        }

        fun inputHolderName(viewMatcher: Matcher<in View>) {
            onView(withId(R.id.input_edit_holder_name))
                .check(matches(viewMatcher))
        }
    }

    object Interaction {

        fun clickInputNumber() {
            onView(withId(R.id.input_edit_number))
                .perform(click())
        }

        fun inputCard(cardRobot: CardRobot) {
            inputNumber(cardRobot.number)
            inputExpiration(cardRobot.exp)
            inputCvc(cardRobot.cvc)
            inputHolderName(cardRobot.name)
        }

        fun inputNumber(number: String) {
            onView(withId(R.id.input_edit_number))
                .perform(scrollTo(), replaceText(number))
        }

        fun inputExpiration(exp: String) {
            onView(withId(R.id.input_edit_expiration))
                .perform(scrollTo(), replaceText(exp))
        }

        fun inputCvc(cvc: String) {
            onView(withId(R.id.input_edit_cvc))
                .perform(scrollTo(), replaceText(cvc))
        }

        fun inputHolderName(holderName: String) {
            onView(withId(R.id.input_edit_holder_name))
                .perform(scrollTo(), replaceText(holderName))
        }

        fun clickSubmitButton() {
            onView(withId(R.id.card_form_button))
                .perform(click())
        }

        fun clickReloadButton() {
            onView(withId(R.id.reload_content_button))
                .perform(click())
        }
    }
}
