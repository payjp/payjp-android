/*
 *
 * Copyright (c) 2021 PAY, Inc.
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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import jp.pay.android.PayjpCardForm
import jp.pay.android.R
import jp.pay.android.databinding.PayjpCardFormActivityBinding
import jp.pay.android.model.CardBrand
import jp.pay.android.model.ExtraAttribute
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.model.TokenId
import jp.pay.android.ui.extension.showWith
import jp.pay.android.ui.widget.PayjpCardFormAbstractFragment
import jp.pay.android.ui.widget.PayjpCardFormView
import jp.pay.android.ui.widget.onClickDebounced
import jp.pay.android.util.nonNull
import jp.pay.android.verifier.PayjpVerifier

/**
 * PayjpCardFormActivity show card form.
 *
 */
internal class PayjpCardFormActivity :
    AppCompatActivity(),
    PayjpCardFormView.OnValidateInputListener,
    PayjpCardFormView.CardFormEditorListener {

    internal companion object {
        const val DEFAULT_CARD_FORM_REQUEST_CODE = 1
        private const val FRAGMENT_CARD_FORM = "FRAGMENT_CARD_FORM"
        private const val EXTRA_KEY_TENANT = "EXTRA_KEY_TENANT"
        private const val EXTRA_KEY_FACE = "EXTRA_KEY_FACE"
        private const val EXTRA_KEY_EXTRA_ATTRIBUTES = "EXTRA_KEY_EXTRA_ATTRIBUTES"
        private const val EXTRA_KEY_USE_THREE_D_SECURE = "EXTRA_KEY_USE_THREE_D_SECURE"
        private const val CARD_FORM_EXTRA_KEY_TOKEN = "DATA"

        fun createIntent(
            context: Context,
            tenant: TenantId?,
            @PayjpCardForm.CardFormFace face: Int,
            extraAttributes: Array<ExtraAttribute<*>>,
            useThreeDSecure: Boolean,
        ): Intent = Intent(context, PayjpCardFormActivity::class.java)
            .putExtra(EXTRA_KEY_FACE, face)
            .putExtra(EXTRA_KEY_EXTRA_ATTRIBUTES, extraAttributes)
            .putExtra(EXTRA_KEY_USE_THREE_D_SECURE, useThreeDSecure)
            .apply {
                if (tenant != null) {
                    putExtra(EXTRA_KEY_TENANT, tenant.id)
                }
            }

        fun start(
            activity: Activity,
            requestCode: Int?,
            tenant: TenantId?,
            @PayjpCardForm.CardFormFace face: Int,
            extraAttributes: Array<ExtraAttribute<*>>,
            useThreeDSecure: Boolean,
        ) {
            activity.startActivityForResult(
                createIntent(activity, tenant, face, extraAttributes, useThreeDSecure)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                requestCode ?: DEFAULT_CARD_FORM_REQUEST_CODE
            )
        }

        fun start(
            fragment: Fragment,
            requestCode: Int?,
            tenant: TenantId?,
            @PayjpCardForm.CardFormFace face: Int,
            extraAttributes: Array<ExtraAttribute<*>>,
            useThreeDSecure: Boolean,
        ) {
            fragment.startActivityForResult(
                createIntent(fragment.requireActivity(), tenant, face, extraAttributes, useThreeDSecure)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                requestCode ?: DEFAULT_CARD_FORM_REQUEST_CODE
            )
        }

        fun onActivityResult(data: Intent?, callback: PayjpCardFormResultCallback) {
            val token = data?.let {
                IntentCompat.getParcelableExtra(it, CARD_FORM_EXTRA_KEY_TOKEN, Token::class.java)
            }
            val result = if (token != null) {
                PayjpCardFormResult.Success(token = token)
            } else {
                PayjpCardFormResult.Canceled
            }
            callback.onResult(result)
        }
    }

    private lateinit var binding: PayjpCardFormActivityBinding
    private val tenantId: TenantId? by lazy {
        intent?.getStringExtra(EXTRA_KEY_TENANT)?.let { TenantId(it) }
    }
    private val face: Int by lazy {
        intent?.getIntExtra(EXTRA_KEY_FACE, PayjpCardForm.FACE_MULTI_LINE)
            ?: PayjpCardForm.FACE_MULTI_LINE
    }
    private val extraAttributes: Array<ExtraAttribute<*>> by lazy {
        intent?.let {
            IntentCompat.getParcelableArrayExtra(it, EXTRA_KEY_EXTRA_ATTRIBUTES, ExtraAttribute::class.java)
        }
            ?.filterIsInstance<ExtraAttribute<*>>()
            ?.toTypedArray()
            ?: emptyArray()
    }
    private val useThreeDSecure: Boolean by lazy {
        intent?.getBooleanExtra(EXTRA_KEY_USE_THREE_D_SECURE, false) ?: false
    }
    private val inputMethodManager: InputMethodManager by lazy {
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }
    private var cardFormFragment: PayjpCardFormAbstractFragment? = null
    private var viewModel: CardFormScreenViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PayjpCardFormActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle(R.string.payjp_card_form_screen_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setUpUI()
        cardFormFragment = findCardFormFragment()

        PayjpCardForm.clientInfoInterceptorProvider()?.getClientInfoInterceptor()?.applyClientInfoExtra {
            setCardFormType(PayjpCardForm.getCardFormFaceString(face))
        }
    }

    override fun onDestroy() {
        PayjpCardForm.clientInfoInterceptorProvider()?.getClientInfoInterceptor()?.applyClientInfoExtra {
            setCardFormType(null)
        }
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PayjpVerifier.handleThreeDSecureResult(requestCode) { result ->
            viewModel?.onCompleteCardVerify(result)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onValidateInput(view: PayjpCardFormView, isValid: Boolean) {
        viewModel?.onValidateInput(isValid)
    }

    override fun onLastFormEditorActionDone(
        view: PayjpCardFormView,
        textView: TextView,
        event: KeyEvent?
    ): Boolean {
        performSubmitButton(textView.windowToken)
        return true
    }

    private fun setUpUI() {
        binding.cardFormButton.onClickDebounced {
            performSubmitButton(it.windowToken)
        }
        binding.reloadContentButton.onClickDebounced {
            viewModel?.onClickReload()
        }

        val vmFactory = CardFormScreenViewModel.Factory(
            owner = this,
            tokenService = checkNotNull(PayjpCardForm.tokenService()) {
                "You must initialize Payjp first"
            },
            tenantId = tenantId,
            errorTranslator = ContextErrorTranslator(this),
            tokenHandlerExecutor = PayjpCardForm.tokenHandlerExecutor()
        )
        viewModel = ViewModelProvider(this, vmFactory).get(CardFormScreenViewModel::class.java)
            .also { vm ->
                lifecycle.addObserver(vm)
                vm.acceptedBrands.nonNull().observe(this, binding.acceptedBrands::setAcceptedBrands)
                vm.contentViewVisibility.observe(this, binding.contentView::setVisibility)
                vm.errorViewVisibility.observe(this, binding.errorView::setVisibility)
                vm.loadingViewVisibility.observe(this, binding.contentLoadingProgress::setVisibility)
                vm.reloadContentButtonVisibility.observe(this, binding.reloadContentButton::setVisibility)
                vm.submitButtonVisibility.observe(this, binding.cardFormButton::setVisibility)
                vm.submitButtonProgressVisibility.observe(this, binding.cardFormButtonProgress::setVisibility)
                vm.submitButtonIsEnabled.observe(this, binding.cardFormButton::setEnabled)
                vm.errorViewText.observe(this, binding.errorMessage::setText)
                vm.addCardFormCommand.nonNull().observe(this) { brands ->
                    cardFormFragment = addCardFormFragment(brands.toTypedArray())
                    viewModel?.onAddedCardForm()
                }
                vm.errorDialogMessage.nonNull().observe(this, this::showErrorMessage)
                vm.success.nonNull().observe(this, this::finishWithSuccess)
                vm.startVerifyCommand.nonNull().observe(this, this::startVerify)
                vm.snackBarMessage.nonNull().observe(this, this::showSnackBarMessage)
            }
    }

    private fun addCardFormFragment(acceptedBrands: Array<CardBrand>): PayjpCardFormAbstractFragment {
        return supportFragmentManager.let { manager ->
            PayjpCardForm.newCardFormFragment(
                tenantId = tenantId,
                acceptedBrands = acceptedBrands,
                face = face,
                extraAttributes = extraAttributes,
            ).also { fragment ->
                manager
                    .beginTransaction().apply {
                        replace(R.id.card_form_view, fragment, FRAGMENT_CARD_FORM)
                    }
                    .commit()
            }
        }
    }

    private fun findCardFormFragment(): PayjpCardFormAbstractFragment? {
        return supportFragmentManager.let { manager ->
            (manager.findFragmentByTag(FRAGMENT_CARD_FORM) as? PayjpCardFormAbstractFragment)?.also { f ->
                if (!f.isAdded) {
                    manager
                        .beginTransaction().apply {
                            replace(R.id.card_form_view, f, FRAGMENT_CARD_FORM)
                        }
                        .commit()
                }
            }
        }
    }

    private fun performSubmitButton(windowToken: IBinder) {
        inputMethodManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        cardFormFragment?.let { cardForm ->
            if (cardForm.isValid) {
                viewModel?.onCreateToken(cardForm.createToken(useThreeDSecure))
            }
        }
    }

    // FIXME: Remove SuppressLint Annotation after the issue is fixed.
    // https://github.com/material-components/material-components-android/issues/504
    @SuppressLint("WrongConstant")
    private fun showSnackBarMessage(@StringRes message: Int) {
        Snackbar.make(binding.contentView, message, Snackbar.LENGTH_SHORT).show()
        viewModel?.onDisplaySnackBarMessage()
    }

    private fun showErrorMessage(message: CharSequence) {
        AlertDialog.Builder(this)
            .setTitle(R.string.payjp_card_form_dialog_title_error)
            .setMessage(message)
            .setNegativeButton(R.string.payjp_card_form_dialog_ok, null)
            .create()
            .showWith(this)
        viewModel?.onDisplayedErrorMessage()
    }

    private fun finishWithSuccess(token: Token) {
        val data = Intent().putExtra(CARD_FORM_EXTRA_KEY_TOKEN, token)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun startVerify(tokenId: TokenId) {
        PayjpVerifier.startThreeDSecureFlow(tokenId.id, this)
        viewModel?.onStartedVerify()
    }
}
