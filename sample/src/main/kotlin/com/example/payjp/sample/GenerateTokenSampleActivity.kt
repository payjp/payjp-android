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
package com.example.payjp.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import jp.pay.android.Payjp
import jp.pay.android.Task
import jp.pay.android.model.Token
import kotlinx.android.synthetic.main.activity_generate_token_sample.button_create_token
import kotlinx.android.synthetic.main.activity_generate_token_sample.button_get_token
import kotlinx.android.synthetic.main.activity_generate_token_sample.progress_bar
import kotlinx.android.synthetic.main.activity_generate_token_sample.text_card_cvc
import kotlinx.android.synthetic.main.activity_generate_token_sample.text_card_exp_month
import kotlinx.android.synthetic.main.activity_generate_token_sample.text_card_exp_year
import kotlinx.android.synthetic.main.activity_generate_token_sample.text_card_name
import kotlinx.android.synthetic.main.activity_generate_token_sample.text_card_number
import kotlinx.android.synthetic.main.activity_generate_token_sample.text_token_content
import kotlinx.android.synthetic.main.activity_generate_token_sample.text_token_id

class GenerateTokenSampleActivity : AppCompatActivity() {

    private var createToken: Task<Token>? = null
    private var getToken: Task<Token>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_token_sample)

        button_create_token.setOnClickListener {
            progress_bar.visibility = View.VISIBLE
            text_token_content.visibility = View.INVISIBLE
            // create token
            val number = text_card_number.text.toString()
            val cvc = text_card_cvc.text.toString()
            val expMonth = text_card_exp_month.text.toString()
            val expYear = text_card_exp_year.text.toString()
            val name = text_card_name.text.toString()

            createToken = Payjp.getInstance().createToken(
                number = number, cvc = cvc,
                expMonth = expMonth, expYear = expYear, name = name
            )
            createToken?.enqueue(object : Task.Callback<Token> {
                override fun onSuccess(data: Token) {
                    Log.i("GenerateTokenSample", "token => $data")
                    text_token_id.setText(data.id)
                    text_token_content.text = "The token has created."
                    progress_bar.visibility = View.GONE
                    text_token_content.visibility = View.VISIBLE
                }

                override fun onError(throwable: Throwable) {
                    Log.e("GenerateTokenSample", "failure creating token", throwable)
                    text_token_content.text = throwable.toString()
                    progress_bar.visibility = View.GONE
                    text_token_content.visibility = View.VISIBLE
                }
            })
        }

        button_get_token.setOnClickListener {
            progress_bar.visibility = View.VISIBLE
            text_token_content.visibility = View.INVISIBLE

            // get token
            getToken = Payjp.getInstance().getToken(text_token_id.text.toString())
            getToken?.enqueue(object : Task.Callback<Token> {
                override fun onSuccess(data: Token) {
                    Log.i("GenerateTokenSample", "token => $data")
                    text_token_content.text = data.toString()
                    progress_bar.visibility = View.GONE
                    text_token_content.visibility = View.VISIBLE
                }

                override fun onError(throwable: Throwable) {
                    Log.e("GenerateTokenSample", "failure creating token", throwable)
                    text_token_content.text = throwable.toString()
                    progress_bar.visibility = View.GONE
                    text_token_content.visibility = View.VISIBLE
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        createToken?.cancel()
        getToken?.cancel()
    }
}
