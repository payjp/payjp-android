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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.transition.TransitionManager
import com.google.android.material.textfield.TextInputLayout
import jp.pay.android.R
import jp.pay.android.Task
import jp.pay.android.exception.PayjpInvalidCardFormException
import jp.pay.android.model.CardComponentInput
import jp.pay.android.model.CardCvcInput
import jp.pay.android.model.CardExpirationInput
import jp.pay.android.model.CardHolderNameInput
import jp.pay.android.model.CardNumberInput
import jp.pay.android.model.TenantId
import jp.pay.android.model.Token
import jp.pay.android.ui.extension.setErrorOrNull
import jp.pay.android.ui.extension.toStringWith
import jp.pay.android.util.Tasks

class CardFormFragment : Fragment(), TokenCreatableView {

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
        fun newInstance(holderNameEnabled: Boolean = true, tenantId: TenantId? = null): CardFormFragment =
            CardFormFragment().apply {
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
    private lateinit var numberEditText: CardNumberEditText
    private lateinit var expirationEditText: CardExpirationEditText
    private lateinit var cvcEditText: CardCvcEditText
    private lateinit var holderNameEditText: CardHolderNameEditText

    private var viewModel: CardFormViewModel? = null
    private var onValidateInputListener: TokenCreatableView.OnValidateInputListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is TokenCreatableView.OnValidateInputListener) {
            this.onValidateInputListener = context
        }
    }

    override fun onDetach() {
        this.onValidateInputListener = null
        super.onDetach()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.card_form_view, container, false).also { setUpUI(it as ViewGroup) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpViewModel()
    }

    override fun isValid(): Boolean = viewModel?.isValid?.value ?: false

    override fun validateCardForm(): Boolean {
        forceValidate()
        updateAllErrorUI(lazy = false)
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
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProviders.of(requireActivity()).get(CardFormViewModel::class.java).apply {
            updateCardHolderNameEnabled(arguments?.getBoolean(ARGS_HOLDER_NAME_ENABLED) ?: true)
            setTenantId(arguments?.getString(ARGS_TENANT_ID)?.let { TenantId(it) })

            // observer
            acceptedBrands.observe(viewLifecycleOwner) {
                numberEditText.acceptedBrands = it
            }
            cardHolderNameEnabled.observe(viewLifecycleOwner) {
                holderNameLayout.visibility = if (it) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                TransitionManager.beginDelayedTransition(view as ViewGroup)
            }
            isValid.observe(viewLifecycleOwner) {
                onValidateInputListener?.onValidateInput(this@CardFormFragment, it)
            }
            cardNumberInput.observe(viewLifecycleOwner) {
                updateInputLayoutError(numberLayout, it, true)
            }
            cardExpirationInput.observe(viewLifecycleOwner) {
                updateInputLayoutError(expirationLayout, it, true)
            }
            cardCvcInput.observe(viewLifecycleOwner) {
                updateInputLayoutError(cvcLayout, it, true)
            }
            cardHolderNameInput.observe(viewLifecycleOwner) {
                updateInputLayoutError(holderNameLayout, it, true)
            }
        }
        watchInputUpdate(viewModel)
    }

    private fun forceValidate() {
        numberEditText.validate()
        expirationEditText.validate()
        cvcEditText.validate()
        holderNameEditText.validate()
    }

    private fun updateAllErrorUI(lazy: Boolean) {
        viewModel?.apply {
            updateInputLayoutError(numberLayout, cardNumberInput.value, lazy)
            updateInputLayoutError(expirationLayout, cardExpirationInput.value, lazy)
            updateInputLayoutError(cvcLayout, cardCvcInput.value, lazy)
            updateInputLayoutError(holderNameLayout, cardHolderNameInput.value, lazy)
        }
    }

    private fun updateInputLayoutError(layout: TextInputLayout, input: CardComponentInput<*>?, lazy: Boolean) {
        layout.setErrorOrNull(input?.errorMessage?.toStringWith(resources, lazy))
    }

    private fun watchInputUpdate(viewModel: CardFormViewModel?) {
        numberEditText.onChangeInputListener = object : CardComponentInputView.OnChangeInputListener<CardNumberInput> {
            override fun onChangeInput(input: CardNumberInput) {
                viewModel?.updateCardInput(input)
            }
        }
        expirationEditText.onChangeInputListener = object :
            CardComponentInputView.OnChangeInputListener<CardExpirationInput> {
            override fun onChangeInput(input: CardExpirationInput) {
                viewModel?.updateCardInput(input)
            }
        }
        cvcEditText.onChangeInputListener = object : CardComponentInputView.OnChangeInputListener<CardCvcInput> {
            override fun onChangeInput(input: CardCvcInput) {
                viewModel?.updateCardInput(input)
            }
        }
        holderNameEditText.onChangeInputListener = object :
            CardComponentInputView.OnChangeInputListener<CardHolderNameInput> {
            override fun onChangeInput(input: CardHolderNameInput) {
                viewModel?.updateCardInput(input)
            }
        }
    }
}