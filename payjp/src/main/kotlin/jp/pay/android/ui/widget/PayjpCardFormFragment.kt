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
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.transition.TransitionManager
import com.google.android.material.textfield.TextInputLayout
import jp.pay.android.PayjpConstants
import jp.pay.android.PayjpToken
import jp.pay.android.R
import jp.pay.android.Task
import jp.pay.android.exception.PayjpInvalidCardFormException
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.ui.extension.addOnTextChanged
import jp.pay.android.ui.extension.setErrorOrNull
import jp.pay.android.util.Tasks
import jp.pay.android.validator.CardCvcInputTransformer
import jp.pay.android.validator.CardExpirationInputTransformer
import jp.pay.android.validator.CardHolderNameInputTransformer
import jp.pay.android.validator.CardNumberInputTransformer

class PayjpCardFormFragment : Fragment(), PayjpCardFormView {

    companion object {
        private const val ARGS_HOLDER_NAME_ENABLED = "ARGS_HOLDER_NAME_ENABLED"
        private const val ARGS_TENANT_ID = "ARGS_TENANT_ID"

        /**
         * Create new fragment instance with args
         *
         * @param holderNameEnabled a option it require card holder name or not.
         * @param tenantId a option for platform tenant.
         * @return fragment
         */
        @JvmStatic
        fun newInstance(holderNameEnabled: Boolean = true, tenantId: TenantId? = null): PayjpCardFormFragment =
            PayjpCardFormFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARGS_HOLDER_NAME_ENABLED, holderNameEnabled)
                    putString(ARGS_TENANT_ID, tenantId?.id)
                }
            }
    }

    private lateinit var numberLayout: TextInputLayout
    private lateinit var expirationLayout: TextInputLayout
    private lateinit var cvcLayout: TextInputLayout
    private lateinit var holderNameLayout: TextInputLayout
    private lateinit var numberEditText: EditText
    private lateinit var expirationEditText: CardExpirationEditText
    private lateinit var cvcEditText: EditText
    private lateinit var holderNameEditText: EditText

    private var viewModel: CardFormViewModel? = null
    private var onValidateInputListener: PayjpCardFormView.OnValidateInputListener? = null
    private val delimiterExpiration = PayjpConstants.CARD_FORM_DELIMITER_EXPIRATION
    private val cardNumberFormatter = CardNumberFormatTextWatcher(PayjpConstants.CARD_FORM_DELIMITER_NUMBER)

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is PayjpCardFormView.OnValidateInputListener) {
            this.onValidateInputListener = context
        }
    }

    override fun onDetach() {
        this.onValidateInputListener = null
        super.onDetach()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.payjp_card_form_view, container, false).also { setUpUI(it as ViewGroup) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpViewModel()
    }

    override fun isValid(): Boolean = viewModel?.isValid?.value ?: false

    override fun validateCardForm(): Boolean {
        viewModel?.validate()
        return isValid
    }

    override fun setCardHolderNameInputEnabled(enabled: Boolean) {
        if (viewModel == null) {
            arguments = Bundle(arguments).apply {
                putBoolean(ARGS_HOLDER_NAME_ENABLED, enabled)
            }
        } else {
            viewModel?.updateCardHolderNameEnabled(enabled)
        }
    }

    override fun createToken(): Task<Token> {
        return viewModel?.createToken() ?: Tasks.failure(
            PayjpInvalidCardFormException("Card form is not ready.")
        )
    }

    private fun setUpUI(view: ViewGroup) {
        numberLayout = view.findViewById(R.id.input_layout_number)
        numberEditText = view.findViewById(R.id.input_edit_number)
        expirationLayout = view.findViewById(R.id.input_layout_expiration)
        expirationEditText = view.findViewById(R.id.input_edit_expiration)
        cvcLayout = view.findViewById(R.id.input_layout_cvc)
        cvcEditText = view.findViewById(R.id.input_edit_cvc)
        holderNameLayout = view.findViewById(R.id.input_layout_holder_name)
        holderNameEditText = view.findViewById(R.id.input_edit_holder_name)

        // add formatter
        numberEditText.addTextChangedListener(cardNumberFormatter)
        expirationEditText.addTextChangedListener(
            CardExpirationFormatTextWatcher(delimiterExpiration))
        cvcEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(PayjpConstants.CARD_FORM_MAX_LENGTH_CVC))
    }

    private fun setUpViewModel() {
        val tenantId = arguments?.getString(ARGS_TENANT_ID)?.let { TenantId(it) }
        val holderNameEnabled = arguments?.getBoolean(ARGS_HOLDER_NAME_ENABLED) ?: true
        val factory = CardFormViewModel.Factory(
            tokenService = PayjpToken.getInstance(),
            cardNumberInputTransformer = CardNumberInputTransformer(),
            cardExpirationInputTransformer = CardExpirationInputTransformer(delimiter = delimiterExpiration),
            cardCvcInputTransformer = CardCvcInputTransformer,
            cardHolderNameInputTransformer = CardHolderNameInputTransformer,
            tenantId = tenantId,
            holderNameEnabledDefault = holderNameEnabled
        )
        viewModel = ViewModelProviders.of(requireActivity(), factory).get(CardFormViewModel::class.java).apply {
            cardHolderNameEnabled.observe(viewLifecycleOwner) {
                holderNameLayout.visibility = if (it) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                TransitionManager.beginDelayedTransition(view as ViewGroup)
            }
            cvcImeOptions.observe(viewLifecycleOwner, cvcEditText::setImeOptions)
            isValid.observe(viewLifecycleOwner) {
                onValidateInputListener?.onValidateInput(this@PayjpCardFormFragment, it)
            }
            cardNumberBrand.observe(viewLifecycleOwner) {
                cardNumberFormatter.brand = it
            }
            cardExpiration.observe(viewLifecycleOwner) {
                expirationEditText.expiration = it
            }
            cardNumberError.observe(viewLifecycleOwner) { resId ->
                numberLayout.setErrorOrNull(resId?.let { getString(it) })
            }
            cardExpirationError.observe(viewLifecycleOwner) { resId ->
                expirationLayout.setErrorOrNull(resId?.let { getString(it) })
            }
            cardCvcError.observe(viewLifecycleOwner) { resId ->
                cvcLayout.setErrorOrNull(resId?.let { getString(it) })
            }
            cardHolderNameError.observe(viewLifecycleOwner) { resId ->
                holderNameLayout.setErrorOrNull(resId?.let { getString(it) })
            }
            cardNumberValid.observe(viewLifecycleOwner) { valid ->
                if (valid && numberEditText.hasFocus()) {
                    expirationEditText.requestFocusFromTouch()
                }
            }
            cardExpirationValid.observe(viewLifecycleOwner) { valid ->
                if (valid && expirationEditText.hasFocus()) {
                    cvcEditText.requestFocusFromTouch()
                }
            }
            cardCvcValid.observe(viewLifecycleOwner) { valid ->
                if (valid && cvcEditText.hasFocus() && holderNameLayout.visibility == View.VISIBLE) {
                    holderNameEditText.requestFocusFromTouch()
                }
            }
            numberEditText.addOnTextChanged { s, _, _, _ -> inputCardNumber(s.toString()) }
            expirationEditText.addOnTextChanged { s, _, _, _ -> inputCardExpiration(s.toString()) }
            cvcEditText.addOnTextChanged { s, _, _, _ -> inputCardCvc(s.toString()) }
            holderNameEditText.addOnTextChanged { s, _, _, _ -> inputCardHolderName(s.toString()) }
            this@PayjpCardFormFragment.lifecycle.addObserver(this)
        }
    }
}