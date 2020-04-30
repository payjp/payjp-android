/*
 *
 * Copyright (c) 2020 PAY, Inc.
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
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.textfield.TextInputEditText
import jp.pay.android.PayjpCardForm
import jp.pay.android.R
import jp.pay.android.model.CardBrand
import jp.pay.android.model.TenantId
import jp.pay.android.validator.CardCvcInputTransformer
import jp.pay.android.validator.CardExpirationInputTransformer
import jp.pay.android.validator.CardHolderNameInputTransformer
import jp.pay.android.validator.CardNumberInputTransformer
import kotlin.math.abs

class PayjpCardFormCardDisplayFragment :
    PayjpCardFormAbstractFragment(R.layout.payjp_card_form_view_card_display) {

    companion object {
        private const val ARGS_TENANT_ID = "ARGS_TENANT_ID"
        private const val ARGS_ACCEPTED_BRANDS = "ARGS_ACCEPTED_BRANDS"

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
            acceptedBrands: Array<CardBrand>? = null
        ): PayjpCardFormCardDisplayFragment =
            PayjpCardFormCardDisplayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGS_TENANT_ID, tenantId?.id)
                    putParcelableArray(ARGS_ACCEPTED_BRANDS, acceptedBrands)
                }
            }
    }

    private lateinit var formElementsPager: ViewPager2
    private lateinit var cardDisplay: PayjpCardDisplayView
    private lateinit var adapter: CardFormElementAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val cardNumberFormatter =
        CardNumberFormatTextWatcher(PayjpCardForm.CARD_FORM_DELIMITER_NUMBER_DISPLAY)
    private var autofillManager: AutofillManager? = null

    private val delimiterExpiration = PayjpCardForm.CARD_FORM_DELIMITER_EXPIRATION

    override fun onScanResult(cardNumber: String?) {
        cardNumber?.let { number ->
            viewModel?.inputCardNumber(number)
        }
    }

    override fun setCardHolderNameInputEnabled(enabled: Boolean) {
        // no-op
    }

    override fun setUpUI(view: ViewGroup) {
        formElementsPager = view.findViewById(R.id.form_element_pager)
        cardDisplay = view.findViewById(R.id.card_display)
        formElementsPager.isFocusableInTouchMode = false
        formElementsPager.isFocusable = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            autofillManager = requireContext().getSystemService(AutofillManager::class.java)
        }

        adapter = CardFormElementAdapter(
            cardNumberFormatter = cardNumberFormatter,
            cardExpirationFormatter = CardExpirationFormatTextWatcher(delimiterExpiration),
            scannerPlugin = PayjpCardForm.cardScannerPlugin(),
            onClickScannerIcon = View.OnClickListener {
                PayjpCardForm.cardScannerPlugin()?.startScanActivity(this)
            },
            onElementTextChanged = { type, s, _, _, _ ->
                viewModel?.run {
                    when (type) {
                        CardFormElementType.Number -> inputCardNumber(s.toString())
                        CardFormElementType.Expiration -> inputCardExpiration(s.toString())
                        CardFormElementType.Cvc -> inputCardCvc(s.toString())
                        CardFormElementType.HolderName -> inputCardHolderName(s.toString())
                    }
                }
            },
            onElementEditorAction = { type, v, actionId, event ->
                when (type) {
                    CardFormElementType.HolderName -> onEditorAction(v, actionId, event)
                    else -> if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        type.next()?.let(adapter::getPositionForElementType)?.let { next ->
                            formElementsPager.setCurrentItem(next, true)
                        }
                        true
                    } else false
                }
            },
            onElementFocusChanged = { type, _, hasFocus ->
                when (type) {
                    CardFormElementType.Cvc -> {
                        if (hasFocus && cardDisplay.isFrontVisible() &&
                            viewModel?.cardNumberBrand?.value != CardBrand.AMEX
                        ) {
                            cardDisplay.flipToBack()
                        }
                    }
                    else -> {
                        if (hasFocus && !cardDisplay.isFrontVisible()) {
                            cardDisplay.flipToFront()
                        }
                    }
                }
                val position = adapter.getPositionForElementType(type)
                if (hasFocus && formElementsPager.currentItem != position) {
                    formElementsPager.setCurrentItem(position, true)
                }
                cardDisplay.updateFocus(type, hasFocus)
            },
            onElementKeyDownDeleteWithEmpty = { type, _ ->
                type.prev()?.let(adapter::getPositionForElementType)?.let { next ->
                    formElementsPager.setCurrentItem(next, true)
                }
                true
            },
            onCardNumberInputChanged = cardDisplay::setCardNumber,
            autofillManager = autofillManager
        )
        formElementsPager.adapter = adapter
        formElementsPager.offscreenPageLimit = 2
        formElementsPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val element = CardFormElementType.values()[position]
                handler.postDelayed({
                    // prevent if current item has changed
                    if (isAdded && formElementsPager.currentItem == position) {
                        val id = CardFormElementAdapter.findEditTextId(element)
                        formElementsPager.findViewById<TextInputEditText>(id)?.requestFocusFromTouch()
                    }
                }, 300)
            }
        })

        val pagerMargin = resources.getDimensionPixelOffset(R.dimen.payjp_card_form_pager_margin)
        val offset = resources.getDimensionPixelOffset(R.dimen.payjp_card_form_element_offset)
        formElementsPager.setPageTransformer(CompositePageTransformer().apply {
            addTransformer { page, position ->
                page.translationX = position * (2 * offset + pagerMargin) * -1
            }
            addTransformer { page, position ->
                val scale = 1 - (abs(position) / 6)
                page.scaleX = scale
                page.scaleY = scale
            }
        })

        viewModel?.apply {
            // value
            cardNumberInput.observe(viewLifecycleOwner) { cardNumber ->
                adapter.cardNumberInput = cardNumber
                adapter.notifyCardFormElementChanged(CardFormElementType.Number)
                cardDisplay.setCardNumber(cardNumber.input.orEmpty())
            }
            cardExpirationInput.observe(viewLifecycleOwner) { expiration ->
                adapter.cardExpirationInput = expiration
                adapter.notifyCardFormElementChanged(CardFormElementType.Expiration)
                cardDisplay.setCardExpiration(expiration.input.orEmpty())
            }
            cardHolderNameInput.observe(viewLifecycleOwner) { holderName ->
                adapter.cardHolderNameInput = holderName
                adapter.notifyCardFormElementChanged(CardFormElementType.HolderName)
                cardDisplay.setCardHolderName(holderName.input.orEmpty())
            }
            cardCvcInput.observe(viewLifecycleOwner) { cvc ->
                adapter.cardCvcInput = cvc
                adapter.notifyCardFormElementChanged(CardFormElementType.Cvc)
                cardDisplay.setCardCvcInputLength(cvc.input?.length ?: 0)
            }
            cardNumberBrand.observe(viewLifecycleOwner) { brand ->
                cardNumberFormatter.brand = brand
                adapter.brand = brand
                adapter.notifyDataSetChanged()
                cardDisplay.setBrand(brand)
            }
            showErrorImmediately.observe(viewLifecycleOwner) {
                adapter.showErrorImmediately = it
            }
            currentPrimaryInput.observe(viewLifecycleOwner) { input ->
                input?.let(adapter::getPositionForElementType)
                    ?.takeIf { formElementsPager.currentItem != it }
                    ?.let { position ->
                        formElementsPager.setCurrentItem(position, true)
                    }
            }
        }
    }

    override fun createViewModel(): CardFormViewModel {
        val tenantId = arguments?.getString(ARGS_TENANT_ID)?.let { TenantId(it) }
        val acceptedBrandArray = arguments?.getParcelableArray(ARGS_ACCEPTED_BRANDS)
        val factory = CardFormViewModel.Factory(
            tokenService = checkNotNull(PayjpCardForm.tokenService()) {
                "You must initialize Payjp first"
            },
            cardNumberInputTransformer = CardNumberInputTransformer(),
            cardExpirationInputTransformer = CardExpirationInputTransformer(delimiter = delimiterExpiration),
            cardCvcInputTransformer = CardCvcInputTransformer(),
            cardHolderNameInputTransformer = CardHolderNameInputTransformer,
            tenantId = tenantId,
            holderNameEnabledDefault = true,
            acceptedBrands = acceptedBrandArray?.filterIsInstance<CardBrand>()
        )
        return ViewModelProvider(requireActivity(), factory).get(CardFormViewModel::class.java)
    }
}
