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
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import jp.pay.android.Payjp
import jp.pay.android.PayjpTokenBackgroundHandler
import jp.pay.android.R
import jp.pay.android.Task
import jp.pay.android.model.CardBrand
import jp.pay.android.model.CardBrandsAcceptedResponse
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.ui.widget.PayjpAcceptedBrandsView
import jp.pay.android.ui.widget.PayjpCardFormFragment
import jp.pay.android.ui.widget.PayjpCardFormView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PayjpCardFormActivity : AppCompatActivity(R.layout.payjp_card_form_activity),
    PayjpCardFormView.OnValidateInputListener, CoroutineScope by MainScope() {

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

    private var cardFormFragment: PayjpCardFormFragment? = null
    private lateinit var acceptedBrandsView: PayjpAcceptedBrandsView
    private val submitButtonVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    private val submitButtonProgressVisibility: MutableLiveData<Int> = MutableLiveData(View.INVISIBLE)
    private val contentViewVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    private val loadingViewVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    private val errorViewVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    private val submitButtonIsEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    private val tenantId: TenantId? by lazy {
        intent?.getStringExtra(EXTRA_KEY_TENANT)?.let { TenantId(it) }
    }
    private var createTokenTask: Task<Token>? = null
    private var getAcceptedBrandsTask: Task<CardBrandsAcceptedResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "カードを登録"
        acceptedBrandsView = findViewById(R.id.accepted_brands)
        val submitButton = findViewById<Button>(R.id.card_form_button)
        val submitButtonProgress = findViewById<ProgressBar>(R.id.card_form_button_progress)
        submitButton.setOnClickListener {
            if (cardFormFragment?.isValid != true) {
                return@setOnClickListener
            }
            createToken()
        }
        val loadingView = findViewById<ViewGroup>(R.id.loading_view)
        // intercept focus
        loadingView.setOnClickListener { }
        val errorView = findViewById<ViewGroup>(R.id.error_view)
        findViewById<Button>(R.id.reload_content_button).setOnClickListener {
            fetchAcceptedBrands()
        }
        val contentView = findViewById<ViewGroup>(R.id.content_view)
        submitButtonVisibility.observe(this, submitButton::setVisibility)
        submitButtonProgressVisibility.observe(this, submitButtonProgress::setVisibility)
        submitButtonIsEnabled.observe(this, submitButton::setEnabled)
        loadingViewVisibility.observe(this, loadingView::setVisibility)
        errorViewVisibility.observe(this, errorView::setVisibility)
        contentViewVisibility.observe(this, contentView::setVisibility)
        fetchAcceptedBrands()
    }

    override fun onDestroy() {
        cancel()
        createTokenTask?.cancel()
        getAcceptedBrandsTask?.cancel()
        super.onDestroy()
    }

    override fun onValidateInput(view: PayjpCardFormView, isValid: Boolean) {
        submitButtonIsEnabled.value = isValid
    }

    private fun findCardFormFragment(acceptedBrands: Array<CardBrand>) {
        supportFragmentManager.let { manager ->
            val f = manager.findFragmentByTag(FRAGMENT_CARD_FORM)
            val fragment = f as? PayjpCardFormFragment
                ?: PayjpCardFormFragment.newInstance(
                    holderNameEnabled = true,
                    tenantId = tenantId,
                    acceptedBrands = acceptedBrands
                )
            if (!fragment.isAdded) {
                manager
                    .beginTransaction().apply {
                        replace(R.id.card_form_view, fragment, FRAGMENT_CARD_FORM)
                    }
                    .commit()
            }
            cardFormFragment = fragment
        }
    }

    private fun fetchAcceptedBrands() {
        if (acceptedBrandsView.getAcceptedBrands().isEmpty()) {
            loadingViewVisibility.value = View.VISIBLE
            contentViewVisibility.value = View.GONE
            errorViewVisibility.value = View.GONE
            getAcceptedBrandsTask = Payjp.getInstance().getAcceptedBrands(tenantId)
            getAcceptedBrandsTask?.enqueue(object : Task.Callback<CardBrandsAcceptedResponse> {
                override fun onSuccess(data: CardBrandsAcceptedResponse) {
                    acceptedBrandsView.setAcceptedBrands(data.brands)
                    findCardFormFragment(data.brands.toTypedArray())
                    loadingViewVisibility.value = View.GONE
                    contentViewVisibility.value = View.VISIBLE
                    errorViewVisibility.value = View.GONE
                }

                override fun onError(throwable: Throwable) {
                    // TODO: error message
                    loadingViewVisibility.value = View.GONE
                    contentViewVisibility.value = View.GONE
                    errorViewVisibility.value = View.VISIBLE
                }
            })
        }
    }

    private fun createToken() {
        cardFormFragment?.let { cardForm ->
            submitButtonVisibility.value = View.INVISIBLE
            submitButtonProgressVisibility.value = View.VISIBLE
            createTokenTask = cardForm.createToken()
            createTokenTask?.enqueue(object : Task.Callback<Token> {
                override fun onSuccess(data: Token) {
                    handleToken(data)
                }

                override fun onError(throwable: Throwable) {
                    submitButtonProgressVisibility.value = View.INVISIBLE
                    submitButtonVisibility.value = View.VISIBLE
                    // TODO: エラー
                    showErrorMessage("問題が発生しました。")
                }
            })
        }
    }

    private fun handleToken(token: Token) = launch {
        val handler = Payjp.getInstance().getTokenBackgroundHandler()
        if (handler != null) {
            try {
                val status = withContext(Dispatchers.IO) {
                    handler.handleTokenInBackground(token)
                }
                submitButtonProgressVisibility.value = View.INVISIBLE
                when (status) {
                    PayjpTokenBackgroundHandler.CardFormStatus.Complete -> {
                        finishWithSuccess(token)
                    }
                    is PayjpTokenBackgroundHandler.CardFormStatus.Error -> {
                        showErrorMessage(status.message)
                        submitButtonVisibility.value = View.VISIBLE
                    }
                }
            } catch (t: Throwable) {
                // TODO:
                showErrorMessage("問題が発生しました。")
                submitButtonProgressVisibility.value = View.INVISIBLE
                submitButtonVisibility.value = View.VISIBLE
            }
        } else {
            submitButtonProgressVisibility.value = View.INVISIBLE
            finishWithSuccess(token)
        }
    }

    private fun showErrorMessage(message: CharSequence) {
        AlertDialog.Builder(this)
            .setTitle("エラー")
            .setMessage(message)
            .setNegativeButton("OK", null)
            .create()
            .show()
    }

    private fun finishWithSuccess(token: Token) {
        val data = Intent().putExtra(CARD_FORM_EXTRA_KEY_TOKEN, token)
        setResult(Activity.RESULT_OK, data)
        finish()
    }
}
