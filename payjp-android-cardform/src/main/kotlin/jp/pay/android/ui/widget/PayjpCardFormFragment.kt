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
package jp.pay.android.ui.widget

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import jp.pay.android.PayjpCardForm
import jp.pay.android.databinding.PayjpCardFormViewBinding
import jp.pay.android.model.CardBrand
import jp.pay.android.model.TenantId
import jp.pay.android.model.ThreeDSecureAttribute
import jp.pay.android.ui.extension.addOnTextChanged
import jp.pay.android.ui.extension.cvcIconResourceId
import jp.pay.android.ui.extension.logoResourceId
import jp.pay.android.ui.extension.setErrorOrNull
import jp.pay.android.util.autoCleared
import jp.pay.android.validator.CardCvcInputTransformer
import jp.pay.android.validator.CardEmailInputTransformer
import jp.pay.android.validator.CardExpirationInputTransformer
import jp.pay.android.validator.CardHolderNameInputTransformer
import jp.pay.android.validator.CardNumberInputTransformer
import jp.pay.android.validator.CardPhoneNumberInputTransformer

class PayjpCardFormFragment : PayjpCardFormAbstractFragment() {

    companion object {
        private const val ARGS_TENANT_ID = "ARGS_TENANT_ID"
        private const val ARGS_ACCEPTED_BRANDS = "ARGS_ACCEPTED_BRANDS"
        private const val ARGS_TDS_ATTRIBUTES = "ARGS_TDS_ATTRIBUTES"

        /**
         * Create new fragment instance with args
         *
         * @param tenantId a option for platform tenant.
         * @param acceptedBrands accepted brands. if it is null, the fragment try to get them.
         * @param threeDSecureAttributes a option for 3D secure attributes.
         * @return fragment
         */
        @JvmStatic
        fun newInstance(
            tenantId: TenantId? = null,
            acceptedBrands: Array<CardBrand>? = null,
            threeDSecureAttributes: Array<ThreeDSecureAttribute<*>>,
        ): PayjpCardFormFragment =
            PayjpCardFormFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGS_TENANT_ID, tenantId?.id)
                    putParcelableArray(ARGS_ACCEPTED_BRANDS, acceptedBrands)
                    putParcelableArray(ARGS_TDS_ATTRIBUTES, threeDSecureAttributes)
                }
            }
    }

    private var binding: PayjpCardFormViewBinding by autoCleared()

    private val delimiterExpiration = PayjpCardForm.CARD_FORM_DELIMITER_EXPIRATION
    private val cardNumberFormatter =
        CardNumberFormatTextWatcher(PayjpCardForm.CARD_FORM_DELIMITER_NUMBER)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PayjpCardFormViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onScanResult(cardNumber: String?) {
        cardNumber?.let(binding.layoutNumber.inputEditNumber::setText)
        binding.layoutExpiration.inputEditExpiration.requestFocusFromTouch()
    }

    override fun setUpUI(view: ViewGroup) {
        // add formatter
        binding.layoutNumber.inputEditNumber.addTextChangedListener(cardNumberFormatter)
        binding.layoutExpiration.inputEditExpiration.addTextChangedListener(
            CardExpirationFormatTextWatcher(delimiterExpiration)
        )
        // default cvc length
        binding.layoutCvc.inputEditCvc.filters =
            arrayOf<InputFilter>(InputFilter.LengthFilter(CardBrand.UNKNOWN.cvcLength))
        PayjpCardForm.cardScannerPlugin()?.let { bridge ->
            binding.layoutNumber.inputLayoutNumber.endIconMode = TextInputLayout.END_ICON_CUSTOM
            binding.layoutNumber.inputLayoutNumber.setEndIconOnClickListener {
                bridge.startScanActivity(this)
            }
        }
        binding.layoutPhoneNumber.inputLayoutCountryCode.setEndIconOnClickListener {
            startSearchCountryCode()
        }

        viewModel?.apply {
            binding.layoutEmail.root.isVisible = cardEmailEnabled
            binding.layoutPhoneNumber.root.isVisible = cardPhoneNumberEnabled
            cardNumberBrand.observe(viewLifecycleOwner) {
                cardNumberFormatter.brand = it
                binding.layoutCvc.inputEditCvc.filters =
                    arrayOf<InputFilter>(InputFilter.LengthFilter(it.cvcLength))
                binding.layoutCvc.inputLayoutCvc.setEndIconDrawable(it.cvcIconResourceId)
                binding.layoutNumber.inputLayoutNumber.setStartIconDrawable(it.logoResourceId)
            }
            cardExpiration.observe(viewLifecycleOwner) {
                binding.layoutExpiration.inputEditExpiration.expiration = it
            }
            cardNumberError.observe(viewLifecycleOwner) { resId ->
                binding.layoutNumber.inputLayoutNumber.setErrorOrNull(resId?.let { getString(it) })
            }
            cardExpirationError.observe(viewLifecycleOwner) { resId ->
                binding.layoutExpiration.inputLayoutExpiration.setErrorOrNull(resId?.let { getString(it) })
            }
            cardCvcError.observe(viewLifecycleOwner) { resId ->
                binding.layoutCvc.inputLayoutCvc.setErrorOrNull(resId?.let { getString(it) })
            }
            cardHolderNameError.observe(viewLifecycleOwner) { resId ->
                binding.layoutHolderName.inputLayoutHolderName.setErrorOrNull(resId?.let { getString(it) })
            }
            cardNumberValid.observe(viewLifecycleOwner) { valid ->
                if (valid && binding.layoutNumber.inputEditNumber.hasFocus()) {
                    binding.layoutExpiration.inputEditExpiration.requestFocusFromTouch()
                }
            }
            cardExpirationValid.observe(viewLifecycleOwner) { valid ->
                if (valid && binding.layoutExpiration.inputEditExpiration.hasFocus()) {
                    binding.layoutCvc.inputEditCvc.requestFocusFromTouch()
                }
            }
            cardCvcValid.observe(viewLifecycleOwner) { valid ->
                if (valid &&
                    binding.layoutCvc.inputEditCvc.hasFocus() &&
                    binding.layoutHolderName.inputLayoutHolderName.visibility == View.VISIBLE
                ) {
                    binding.layoutHolderName.inputEditHolderName.requestFocusFromTouch()
                }
            }

            cardEmailError.observe(viewLifecycleOwner) { resId ->
                binding.layoutEmail.inputLayoutEmail.setErrorOrNull(resId?.let { getString(it) })
            }
            cardPhoneNumberCountryCode.observe(viewLifecycleOwner) { countryCode ->
                binding.layoutPhoneNumber.inputEditCountryCode.setText(countryCode.shortName)
            }
            cardPhoneNumberError.observe(viewLifecycleOwner) { resId ->
                binding.layoutPhoneNumber.inputLayoutPhoneNumber.setErrorOrNull(resId?.let { getString(it) })
            }
            // preset
            binding.layoutEmail.inputEditEmail.setText(cardEmailInput.value?.input)
            binding.layoutPhoneNumber.inputEditCountryCode.setText(cardPhoneNumberCountryCode.value?.shortName)
            binding.layoutPhoneNumber.inputEditPhoneNumber.setText(cardPhoneNumberInput.value?.input)
            // onTextChanged
            binding.layoutNumber.inputEditNumber.addOnTextChanged { s, _, _, _ -> inputCardNumber(s.toString()) }
            binding.layoutExpiration.inputEditExpiration.addOnTextChanged { s, _, _, _ ->
                inputCardExpiration(s.toString())
            }
            binding.layoutCvc.inputEditCvc.addOnTextChanged { s, _, _, _ -> inputCardCvc(s.toString()) }
            binding.layoutHolderName.inputEditHolderName.addOnTextChanged { s, _, _, _ ->
                inputCardHolderName(s.toString())
            }
            binding.layoutEmail.inputEditEmail.addOnTextChanged { s, _, _, _ -> inputEmail(s.toString()) }
            binding.layoutPhoneNumber.inputEditPhoneNumber.addOnTextChanged { s, _, _, _ ->
                inputPhoneNumber(s.toString())
            }
            binding.layoutHolderName.inputEditHolderName
            when (lastInput) {
                CardFormInputType.Email -> binding.layoutEmail.inputEditEmail
                CardFormInputType.PhoneNumber -> binding.layoutPhoneNumber.inputEditPhoneNumber
                else -> binding.layoutHolderName.inputEditHolderName
            }.setOnEditorActionListener(this@PayjpCardFormFragment::onEditorAction)
            // additional info
            binding.additionalInfo.isVisible = cardEmailEnabled || cardPhoneNumberEnabled
            binding.additionalInfoContent.requiredAtLeastOne.isVisible = cardEmailEnabled && cardPhoneNumberEnabled
        }
    }

    override fun createViewModel(): CardFormViewModel {
        val tenantId = arguments?.getString(ARGS_TENANT_ID)?.let { TenantId(it) }
        val acceptedBrandArray = arguments?.let {
            BundleCompat.getParcelableArray(it, ARGS_ACCEPTED_BRANDS, CardBrand::class.java)
        }
        val threeDSecureAttributes = arguments?.let {
            BundleCompat.getParcelableArray(it, ARGS_TDS_ATTRIBUTES, ThreeDSecureAttribute::class.java)
        }
        val factory = CardFormViewModel.Factory(
            tokenService = PayjpCardForm.tokenService(),
            cardNumberInputTransformer = CardNumberInputTransformer(),
            cardExpirationInputTransformer = CardExpirationInputTransformer(delimiter = delimiterExpiration),
            cardCvcInputTransformer = CardCvcInputTransformer(),
            cardHolderNameInputTransformer = CardHolderNameInputTransformer,
            cardEmailInputTransformer = CardEmailInputTransformer(),
            cardPhoneNumberInputTransformer = CardPhoneNumberInputTransformer(
                context = requireContext(),
                service = PayjpCardForm.phoneNumberService()
            ),
            tenantId = tenantId,
            acceptedBrands = acceptedBrandArray?.filterIsInstance<CardBrand>(),
            phoneNumberService = PayjpCardForm.phoneNumberService(),
            threeDSecureAttributes = threeDSecureAttributes?.filterIsInstance<ThreeDSecureAttribute<*>>() ?: emptyList(),
        )
        return ViewModelProvider(requireActivity(), factory).get(CardFormViewModel::class.java)
    }
}
