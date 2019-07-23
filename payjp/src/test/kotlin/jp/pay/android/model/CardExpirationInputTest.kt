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
package jp.pay.android.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.pay.android.anyNullable
import jp.pay.android.validator.CardExpirationProcessorService
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyChar
import org.mockito.Mockito.anyString
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class CardExpirationInputTest {

    @Mock
    private lateinit var processor: CardExpirationProcessorService

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    private fun createInput(input: String?) = CardExpirationInput(input, '/', processor)

    @Test
    fun value_is_null_when_input_null() {
        val input = createInput(null)
        assertThat(input.value, nullValue())
    }

    @Test
    fun value_is_null_when_processor_return_null_monthyear() {
        `when`(processor.processExpirationMonthYear(anyString(), anyChar())).thenReturn(null)
        val input = createInput("12/20")
        assertThat(input.value, nullValue())
    }

    @Test
    fun value_is_null_when_processor_return_null_expiration() {
        val monthYear = "12" to "20"
        `when`(processor.processExpirationMonthYear(anyString(), anyChar())).thenReturn(monthYear)
        `when`(processor.processCardExpiration(anyNullable(), anyNullable())).thenReturn(null)
        val input = createInput("12/20")
        assertThat(input.value, nullValue())
    }

    @Test
    fun value_exists_when_processor_return_expiration() {
        val monthYear = "12" to "20"
        val expiration = CardExpiration("12", "2020")
        `when`(processor.processExpirationMonthYear(anyString(), anyChar())).thenReturn(monthYear)
        `when`(processor.processCardExpiration(anyNullable(), anyNullable())).thenReturn(expiration)
        val input = createInput("12/20")
        assertThat(input.value, `is`(expiration))
        assertThat(input.valid, `is`(true))
    }
}