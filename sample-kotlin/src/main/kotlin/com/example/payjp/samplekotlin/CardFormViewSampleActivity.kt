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
package com.example.payjp.samplekotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import jp.pay.android.PayjpToken
import jp.pay.android.Task
import jp.pay.android.model.Token
import jp.pay.android.ui.widget.PayjpCardFormFragment
import jp.pay.android.ui.widget.PayjpCardFormView
import kotlinx.android.synthetic.main.activity_card_form_view_sample.button_get_token
import kotlinx.android.synthetic.main.activity_card_form_view_sample.button_create_token
import kotlinx.android.synthetic.main.activity_card_form_view_sample.button_create_token_anyway
import kotlinx.android.synthetic.main.activity_card_form_view_sample.progress_bar
import kotlinx.android.synthetic.main.activity_card_form_view_sample.text_token_id
import kotlinx.android.synthetic.main.activity_card_form_view_sample.text_token_content
import kotlinx.android.synthetic.main.activity_card_form_view_sample.switch_card_holder_name

private const val FRAGMENT_CARD_FORM = "FRAGMENT_CARD_FORM"

class CardFormViewSampleActivity : AppCompatActivity(), PayjpCardFormView.OnValidateInputListener {

    private var createToken: Task<Token>? = null
    private var getToken: Task<Token>? = null
    private lateinit var cardFormFragment: PayjpCardFormFragment

    override fun onValidateInput(view: PayjpCardFormView, isValid: Boolean) {
        button_create_token.isEnabled = isValid
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_form_view_sample)
        findCardFormFragment()
        button_create_token.setOnClickListener {
            if (!cardFormFragment.isValid) {
                return@setOnClickListener
            }
            createToken()
        }
        button_create_token_anyway.setOnClickListener {
            if (cardFormFragment.validateCardForm()) {
                createToken()
            }
        }

        button_get_token.setOnClickListener {
            getToken(text_token_id.text.toString())
        }

        switch_card_holder_name.setOnCheckedChangeListener { _, isChecked ->
            cardFormFragment.setCardHolderNameInputEnabled(isChecked)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        createToken?.cancel()
        getToken?.cancel()
    }

    private fun createToken() {
        progress_bar.visibility = View.VISIBLE
        text_token_content.visibility = View.INVISIBLE
        // create token
        createToken = cardFormFragment.createToken()
        createToken?.enqueue(object : Task.Callback<Token> {
            override fun onSuccess(data: Token) {
                Log.i("CardFormViewSample", "token => $data")
                text_token_id.setText(data.id)
                text_token_content.text = "The token has created."
                progress_bar.visibility = View.GONE
                text_token_content.visibility = View.VISIBLE
            }

            override fun onError(throwable: Throwable) {
                Log.e("CardFormViewSample", "failure creating token", throwable)
                text_token_content.text = throwable.toString()
                progress_bar.visibility = View.GONE
                text_token_content.visibility = View.VISIBLE
            }
        })
    }

    private fun getToken(id: String) {
        progress_bar.visibility = View.VISIBLE
        text_token_content.visibility = View.INVISIBLE
        // get token
        getToken = PayjpToken.getInstance().getToken(id)
        getToken?.enqueue(object : Task.Callback<Token> {
            override fun onSuccess(data: Token) {
                Log.i("CardFormViewSample", "token => $data")
                text_token_content.text = data.toString()
                progress_bar.visibility = View.GONE
                text_token_content.visibility = View.VISIBLE
            }

            override fun onError(throwable: Throwable) {
                Log.e("CardFormViewSample", "failure creating token", throwable)
                text_token_content.text = throwable.toString()
                progress_bar.visibility = View.GONE
                text_token_content.visibility = View.VISIBLE
            }
        })
    }

    private fun findCardFormFragment() {
        supportFragmentManager?.let { manager ->
            val f = manager.findFragmentByTag(FRAGMENT_CARD_FORM)
            cardFormFragment = f as? PayjpCardFormFragment ?: PayjpCardFormFragment.newInstance()
            if (!cardFormFragment.isAdded) {
                manager
                    .beginTransaction().apply {
                        replace(R.id.card_form_view, cardFormFragment, FRAGMENT_CARD_FORM)
                    }
                    .commit()
            }
        }
    }
}
