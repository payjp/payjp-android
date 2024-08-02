package jp.pay.android.validator

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.R
import jp.pay.android.anyNullable
import jp.pay.android.data.PhoneNumberService
import jp.pay.android.model.CountryCode
import jp.pay.android.model.FormInputError
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class CardPhoneNumberInputTransformerTest {

    @Mock
    private lateinit var mockPhoneNumberService: PhoneNumberService

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun transform_empty() {
        val transformer = CardPhoneNumberInputTransformer(
            context = ApplicationProvider.getApplicationContext(),
            service = mockPhoneNumberService
        ).apply {
            currentCountryCode = CountryCode("JP", 81, Locale.US)
        }
        val result = transformer.transform(" ")
        assertThat(result.value, `is`(nullValue()))
        assertThat(result.errorMessage, `is`(FormInputError(R.string.payjp_card_form_error_no_phone_number, true)))
    }

    @Test
    fun transform_normalized() {
        val transformer = CardPhoneNumberInputTransformer(
            context = ApplicationProvider.getApplicationContext(),
            service = mockPhoneNumberService
        ).apply {
            currentCountryCode = CountryCode("JP", 81, Locale.US)
        }
        val validNumber = "+819012345678"
        `when`(mockPhoneNumberService.normalize(anyNullable(), anyString(), anyNullable())).thenReturn(validNumber)
        val result = transformer.transform("09012345678")
        assertThat(result.value, `is`(validNumber))
        assertThat(result.errorMessage, `is`(nullValue()))
    }

    @Test
    fun transform_invalid_tooLong() {
        val transformer = CardPhoneNumberInputTransformer(
            context = ApplicationProvider.getApplicationContext(),
            service = mockPhoneNumberService
        ).apply {
            currentCountryCode = CountryCode("JP", 81, Locale.US)
        }
        `when`(mockPhoneNumberService.normalize(anyNullable(), anyString(), anyNullable())).thenReturn(null)
        `when`(mockPhoneNumberService.examplePhoneNumber(anyNullable(), anyNullable())).thenReturn("09012345678")
        val result = transformer.transform("09012345678901")
        assertThat(result.value, `is`(nullValue()))
        assertThat(result.errorMessage, `is`(FormInputError(R.string.payjp_card_form_error_invalid_phone_number, false)))
    }

    @Test
    fun transform_invalid_not_tooLong() {
        val transformer = CardPhoneNumberInputTransformer(
            context = ApplicationProvider.getApplicationContext(),
            service = mockPhoneNumberService
        ).apply {
            currentCountryCode = CountryCode("JP", 81, Locale.US)
        }
        `when`(mockPhoneNumberService.normalize(anyNullable(), anyString(), anyNullable())).thenReturn(null)
        `when`(mockPhoneNumberService.examplePhoneNumber(anyNullable(), anyNullable())).thenReturn("09012345678")
        val result = transformer.transform("090123456")
        assertThat(result.value, `is`(nullValue()))
        assertThat(result.errorMessage, `is`(FormInputError(R.string.payjp_card_form_error_invalid_phone_number, true)))
    }
}