package jp.pay.android.validator

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.R
import jp.pay.android.model.FormInputError
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardEmailInputTransformerTest {

    @Test
    fun transform_empty() {
        val transformer = CardEmailInputTransformer()
        val result = transformer.transform(" ")
        assertThat(result.value, `is`(nullValue()))
        assertThat(result.errorMessage, `is`(FormInputError(R.string.payjp_card_form_error_no_email, true)))
    }

    @Test
    fun transform_invalid() {
        val transformer = CardEmailInputTransformer()
        val result = transformer.transform("invalid")
        assertThat(result.value, `is`(nullValue()))
        assertThat(result.errorMessage, `is`(FormInputError(R.string.payjp_card_form_error_invalid_email, false)))
    }

    @Test
    fun transform_valid() {
        val transformer = CardEmailInputTransformer()
        val email = "test@example.com"
        val result = transformer.transform(email)
        assertThat(result.value, `is`(email))
        assertThat(result.errorMessage, `is`(nullValue()))
    }
}