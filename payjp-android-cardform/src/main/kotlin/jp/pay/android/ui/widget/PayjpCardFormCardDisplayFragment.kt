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

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import android.view.inputmethod.EditorInfo
import androidx.core.os.BundleCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.textfield.TextInputEditText
import jp.pay.android.PayjpCardForm
import jp.pay.android.R
import jp.pay.android.databinding.PayjpCardFormViewCardDisplayBinding
import jp.pay.android.model.CardBrand
import jp.pay.android.model.TenantId
import jp.pay.android.model.ThreeDSecureAttribute
import jp.pay.android.util.autoCleared
import jp.pay.android.validator.CardCvcInputTransformer
import jp.pay.android.validator.CardEmailInputTransformer
import jp.pay.android.validator.CardExpirationInputTransformer
import jp.pay.android.validator.CardHolderNameInputTransformer
import jp.pay.android.validator.CardNumberInputTransformer
import jp.pay.android.validator.CardPhoneNumberInputTransformer
import kotlin.math.abs

class PayjpCardFormCardDisplayFragment : PayjpCardFormAbstractFragment() {

    companion object {
        private const val ARGS_TENANT_ID = "ARGS_TENANT_ID"
        private const val ARGS_ACCEPTED_BRANDS = "ARGS_ACCEPTED_BRANDS"
        private const val ARGS_TDS_ATTRIBUTES = "ARGS_TDS_ATTRIBUTES"

        /**
         * Create new fragment instance with args
         *
         * @param tenantId a option for platform tenant.
         * @param acceptedBrands accepted brands. if it is null, the fragment try to get them.
         * @return fragment
         */
        @JvmStatic
        fun newInstance(
            tenantId: TenantId? = null,
            acceptedBrands: Array<CardBrand>? = null,
            threeDSecureAttributes: Array<ThreeDSecureAttribute<*>>,
        ): PayjpCardFormCardDisplayFragment =
            PayjpCardFormCardDisplayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGS_TENANT_ID, tenantId?.id)
                    putParcelableArray(ARGS_ACCEPTED_BRANDS, acceptedBrands)
                    putParcelableArray(ARGS_TDS_ATTRIBUTES, threeDSecureAttributes)
                }
            }
    }

    private var binding: PayjpCardFormViewCardDisplayBinding by autoCleared()
    private lateinit var adapter: CardFormElementAdapter
    private val cardNumberFormatter =
        CardNumberFormatTextWatcher(PayjpCardForm.CARD_FORM_DELIMITER_NUMBER_DISPLAY)
    private var autofillManager: AutofillManager? = null

    private val delimiterExpiration = PayjpCardForm.CARD_FORM_DELIMITER_EXPIRATION

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PayjpCardFormViewCardDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onScanResult(cardNumber: String?) {
        cardNumber?.let { number ->
            viewModel?.inputCardNumber(number)
        }
    }

    override fun setUpUI(view: ViewGroup) {
        binding.formElementPager.run {
            isFocusableInTouchMode = false
            isFocusable = false
            // disable swiping until found valid card number input.
            isUserInputEnabled = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            autofillManager = requireContext().getSystemService(AutofillManager::class.java)
        }

        adapter = CardFormElementAdapter(
            inputTypes = CardFormInputType.createList(
                viewModel!!.cardEmailEnabled,
                viewModel!!.cardPhoneNumberEnabled,
            ),
            cardNumberFormatter = cardNumberFormatter,
            cardExpirationFormatter = CardExpirationFormatTextWatcher(delimiterExpiration),
            scannerPlugin = PayjpCardForm.cardScannerPlugin(),
            onClickScannerIcon = {
                PayjpCardForm.cardScannerPlugin()?.startScanActivity(this)
            },
            onElementTextChanged = { type, s, _, _, _ ->
                viewModel?.run {
                    when (type) {
                        CardFormInputType.Number -> inputCardNumber(s.toString())
                        CardFormInputType.Expiration -> inputCardExpiration(s.toString())
                        CardFormInputType.Cvc -> inputCardCvc(s.toString())
                        CardFormInputType.HolderName -> inputCardHolderName(s.toString())
                        CardFormInputType.Email -> inputEmail(s.toString())
                        CardFormInputType.PhoneNumber -> inputPhoneNumber(s.toString())
                    }
                }
            },
            onElementEditorAction = { type, v, actionId, event ->
                when {
                    type == viewModel?.lastInput -> onEditorAction(v, actionId, event)
                    actionId == EditorInfo.IME_ACTION_NEXT -> {
                        type.elementType().next().let(adapter::getPositionForElementType).let { moveToPosition(it) }
                        true
                    }
                    else -> false
                }
            },
            onElementFocusChanged = { type, _, hasFocus ->
                when (type) {
                    CardFormElementType.EmailAndPhoneNumber -> {
                        binding.cardDisplay.gone()
                    }
                    CardFormElementType.Cvc -> {
                        binding.cardDisplay.visible()
                        if (hasFocus && binding.cardDisplay.isFrontVisible() &&
                            viewModel?.cardNumberBrand?.value != CardBrand.AMEX
                        ) {
                            binding.cardDisplay.flipToBack()
                        }
                    }
                    else -> {
                        binding.cardDisplay.visible()
                        if (hasFocus && !binding.cardDisplay.isFrontVisible()) {
                            binding.cardDisplay.flipToFront()
                        }
                    }
                }
                if (hasFocus) {
                    moveToPosition(adapter.getPositionForElementType(type))
                }
                binding.cardDisplay.updateFocus(type, hasFocus)
            },
            onElementKeyDownDeleteWithEmpty = { type, _ ->
                type.elementType().prev().let(adapter::getPositionForElementType).let { moveToPosition(it) }
                true
            },
            onCardNumberInputChanged = binding.cardDisplay::setCardNumber,
            autofillManager = autofillManager,
            onClickCountryCode = {
                startSearchCountryCode()
            },
            countryCode = viewModel?.cardPhoneNumberCountryCode?.value
                ?: PayjpCardForm.phoneNumberService().defaultCountryCode(),
            lastInputType = viewModel?.lastInput ?: CardFormInputType.HolderName,
        )
        binding.formElementPager.adapter = adapter
        binding.formElementPager.offscreenPageLimit = 2
        binding.formElementPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val element = adapter.getElementTypeForPosition(position)
// prevent if current item has changed
                    if (isAdded && binding.formElementPager.currentItem == position) {
                        val id = adapter.findEditTextId(element)
                        binding.formElementPager.findViewById<TextInputEditText>(id)?.requestFocusFromTouch()
                    }
                }
            }
        )

        val pagerMargin = resources.getDimensionPixelOffset(R.dimen.payjp_card_form_pager_margin)
        val offset = resources.getDimensionPixelOffset(R.dimen.payjp_card_form_element_offset)
        binding.formElementPager.setPageTransformer(
            CompositePageTransformer().apply {
                addTransformer { page, position ->
                    page.translationX = position * (2 * offset + pagerMargin) * -1
                }
                addTransformer { page, position ->
                    val scale = 1 - (abs(position) / 6)
                    page.scaleX = scale
                    page.scaleY = scale
                }
            }
        )

        viewModel?.apply {
            // value
            cardNumberInput.observe(viewLifecycleOwner) { cardNumber ->
                binding.formElementPager.isUserInputEnabled = cardNumber.valid
                adapter.cardNumberInput = cardNumber
                adapter.notifyCardFormElementChanged(CardFormElementType.Number)
                binding.cardDisplay.setCardNumber(cardNumber.input.orEmpty())
            }
            cardExpirationInput.observe(viewLifecycleOwner) { expiration ->
                adapter.cardExpirationInput = expiration
                adapter.notifyCardFormElementChanged(CardFormElementType.Expiration)
                binding.cardDisplay.setCardExpiration(expiration.input.orEmpty())
            }
            cardHolderNameInput.observe(viewLifecycleOwner) { holderName ->
                adapter.cardHolderNameInput = holderName
                adapter.notifyCardFormElementChanged(CardFormElementType.HolderName)
                binding.cardDisplay.setCardHolderName(holderName.input.orEmpty())
            }
            cardCvcInput.observe(viewLifecycleOwner) { cvc ->
                adapter.cardCvcInput = cvc
                adapter.notifyCardFormElementChanged(CardFormElementType.Cvc)
                binding.cardDisplay.setCardCvcInputLength(cvc.input?.length ?: 0)
            }
            cardNumberBrand.observe(viewLifecycleOwner) { brand ->
                cardNumberFormatter.brand = brand
                adapter.brand = brand
                adapter.notifyDataSetChanged()
                binding.cardDisplay.setBrand(brand)
            }
            cardEmailInput.observe(viewLifecycleOwner) { email ->
                adapter.cardEmailInput = email
                adapter.notifyCardFormElementChanged(CardFormElementType.EmailAndPhoneNumber)
            }
            cardPhoneNumberInput.observe(viewLifecycleOwner) { phoneNumber ->
                adapter.cardPhoneNumberInput = phoneNumber
                adapter.notifyCardFormElementChanged(CardFormElementType.EmailAndPhoneNumber)
            }
            cardPhoneNumberCountryCode.observe(viewLifecycleOwner) { countryCode ->
                adapter.countryCode = countryCode
                adapter.notifyCardFormElementChanged(CardFormElementType.EmailAndPhoneNumber)
            }
            showErrorImmediately.observe(viewLifecycleOwner) {
                adapter.showErrorImmediately = it
            }
            currentPrimaryElement.observe(viewLifecycleOwner) { input ->
                input?.let(adapter::getPositionForElementType)?.let { moveToPosition(it) }
            }
        }
    }

    override fun createViewModel(): CardFormViewModel {
        val tenantId = arguments?.getString(ARGS_TENANT_ID)?.let { TenantId(it) }
        val acceptedBrandArray = arguments?.let { BundleCompat.getParcelableArray(it, ARGS_ACCEPTED_BRANDS, CardBrand::class.java) }
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
            acceptedBrands = acceptedBrandArray?.filterIsInstance<CardBrand>() ?: emptyList(),
            phoneNumberService = PayjpCardForm.phoneNumberService(),
            threeDSecureAttributes = threeDSecureAttributes?.filterIsInstance<ThreeDSecureAttribute<*>>() ?: emptyList(),
        )
        return ViewModelProvider(requireActivity(), factory)[CardFormViewModel::class.java]
    }

    private fun moveToPosition(position: Int, smoothScroll: Boolean = true) {
        binding.formElementPager.takeIf { it.currentItem != position && it.isUserInputEnabled }
            ?.setCurrentItem(position, smoothScroll)
    }
}
