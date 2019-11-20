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
package jp.pay.android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import jp.pay.android.R
import jp.pay.android.Task
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.ui.widget.PayjpCardFormFragment
import jp.pay.android.ui.widget.PayjpCardFormView

class PayjpCardFormActivity : AppCompatActivity(R.layout.payjp_card_form_activity),
    PayjpCardFormView.OnValidateInputListener {

    internal companion object {
        const val DEFAULT_CARD_FORM_REQUEST_CODE = 1
        private const val FRAGMENT_CARD_FORM = "FRAGMENT_CARD_FORM"
        private const val EXTRA_KEY_TENANT = "EXTRA_KEY_TENANT"
        private const val CARD_FORM_EXTRA_KEY_TOKEN = "DATA"

        fun start(activity: Activity, requestCode: Int?, tenant: TenantId?) {
            activity.startActivityForResult(
                Intent(activity, PayjpCardFormActivity::class.java).apply {
                    if (tenant != null) {
                        putExtra(EXTRA_KEY_TENANT, tenant.id)
                    }
                },
                requestCode ?: DEFAULT_CARD_FORM_REQUEST_CODE
            )
        }

        fun onActivityResult(data: Intent?, callback: PayjpCardFormResultCallback) {
            val token = data?.getParcelableExtra<Token>(CARD_FORM_EXTRA_KEY_TOKEN)
            val result = if (token != null) {
                PayjpCardFormResult.Success(token = token)
            } else {
                PayjpCardFormResult.Canceled
            }
            callback.onResult(result)
        }
    }

    private lateinit var cardFormFragment: PayjpCardFormFragment
    private val submitButtonVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    private val submitButtonProgressVisibility: MutableLiveData<Int> = MutableLiveData(View.INVISIBLE)
    private val submitButtonIsEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    private val tenantId: TenantId? by lazy {
        intent?.getStringExtra(EXTRA_KEY_TENANT)?.let { TenantId(it) }
    }
    private var createTokenTask: Task<Token>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "カードを登録"
        findCardFormFragment()
        val submitButton = findViewById<Button>(R.id.card_form_button)
        val submitButtonProgress = findViewById<ProgressBar>(R.id.card_form_button_progress)
        submitButton.setOnClickListener {
            if (!cardFormFragment.isValid) {
                return@setOnClickListener
            }
            createToken()
        }
        submitButtonVisibility.observe(this, submitButton::setVisibility)
        submitButtonProgressVisibility.observe(this, submitButtonProgress::setVisibility)
        submitButtonIsEnabled.observe(this, submitButton::setEnabled)
    }

    override fun onDestroy() {
        createTokenTask?.cancel()
        super.onDestroy()
    }

    override fun onValidateInput(view: PayjpCardFormView, isValid: Boolean) {
        submitButtonIsEnabled.value = isValid
    }

    private fun findCardFormFragment() {
        supportFragmentManager.let { manager ->
            val f = manager.findFragmentByTag(FRAGMENT_CARD_FORM)
            cardFormFragment = f as? PayjpCardFormFragment
                ?: PayjpCardFormFragment.newInstance(
                    holderNameEnabled = true,
                    tenantId = tenantId
                )
            if (!cardFormFragment.isAdded) {
                manager
                    .beginTransaction().apply {
                        replace(R.id.card_form_view, cardFormFragment,
                            FRAGMENT_CARD_FORM
                        )
                    }
                    .commit()
            }
        }
    }

    private fun createToken() {
        submitButtonVisibility.value = View.INVISIBLE
        submitButtonProgressVisibility.value = View.VISIBLE
        createTokenTask = cardFormFragment.createToken()
        createTokenTask?.enqueue(object : Task.Callback<Token> {
            override fun onSuccess(data: Token) {
                submitButtonProgressVisibility.value = View.INVISIBLE
                // TODO: サーバーに送信する
                finishWithSuccess(data)
            }

            override fun onError(throwable: Throwable) {
                submitButtonProgressVisibility.value = View.INVISIBLE
                submitButtonVisibility.value = View.VISIBLE
            }
        })
    }

    private fun finishWithSuccess(token: Token) {
        val data = Intent().putExtra(CARD_FORM_EXTRA_KEY_TOKEN, token)
        setResult(Activity.RESULT_OK, data)
        finish()
    }
}
