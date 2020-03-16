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

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.pay.android.PayjpCardForm
import jp.pay.android.R
import jp.pay.android.model.CardBrand
import jp.pay.android.model.TenantId
import jp.pay.android.validator.CardCvcInputTransformer
import jp.pay.android.validator.CardExpirationInputTransformer
import jp.pay.android.validator.CardHolderNameInputTransformer
import jp.pay.android.validator.CardNumberInputTransformer

class PayjpCardFormFragment2 : PayjpCardFormAbstractFragment(R.layout.payjp_card_form_view_2) {

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
        ): PayjpCardFormFragment2 =
            PayjpCardFormFragment2().apply {
                arguments = Bundle().apply {
                    putString(ARGS_TENANT_ID, tenantId?.id)
                    putParcelableArray(ARGS_ACCEPTED_BRANDS, acceptedBrands)
                }
            }
    }

    private lateinit var formElementsView: RecyclerView
    private lateinit var cardDisplay: PayjpCardDisplayView
    private lateinit var adapter: CardFormElementAdapter

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
        formElementsView = view.findViewById(R.id.form_element_recycler_view)
        cardDisplay = view.findViewById(R.id.card_display)

        adapter = CardFormElementAdapter(
            cardNumberFormatter = CardNumberFormatTextWatcher(PayjpCardForm.CARD_FORM_DELIMITER_NUMBER_DISPLAY),
            cardExpirationFormatter = CardExpirationFormatTextWatcher(delimiterExpiration),
            scannerPlugin = PayjpCardForm.cardScannerPlugin(),
            onClickScannerIcon = View.OnClickListener {
                PayjpCardForm.cardScannerPlugin()?.startScanActivity(this)
            },
            onCvcEditorActionListener = TextView.OnEditorActionListener { v, actionId, event ->
                onEditorAction(v, actionId, event)
            },
            onTextChangedNumber = { s, _, _, _ -> viewModel?.inputCardNumber(s.toString()) },
            onTextChangedExpiration = { s, _, _, _ -> viewModel?.inputCardExpiration(s.toString()) },
            onTextChangedHolderName = { s, _, _, _ -> viewModel?.inputCardHolderName(s.toString()) },
            onTextChangedCvc = { s, _, _, _ -> viewModel?.inputCardCvc(s.toString()) }
        )
        formElementsView.adapter = adapter
        formElementsView.layoutManager = LinearLayoutManager(requireContext())
        formElementsView.itemAnimator = null

        viewModel?.apply {
            // value
            cardNumberInput.observe(viewLifecycleOwner) { cardNumber ->
                adapter.cardNumberInput = cardNumber
                adapter.notifyDataSetChanged()
            }
            cardExpirationInput.observe(viewLifecycleOwner) { expiration ->
                adapter.cardExpirationInput = expiration
                adapter.notifyDataSetChanged()
            }
            cardHolderNameInput.observe(viewLifecycleOwner) { holderName ->
                adapter.cardHolderNameInput = holderName
                adapter.notifyDataSetChanged()
            }
            cardCvcInput.observe(viewLifecycleOwner) { cvc ->
                adapter.cardCvcInput = cvc
                adapter.notifyDataSetChanged()
            }
            cardNumberBrand.observe(viewLifecycleOwner) { brand ->
                adapter.brand = brand
                adapter.notifyDataSetChanged()
            }
            showErrorImmediately.observe(viewLifecycleOwner) {
                adapter.showErrorImmediately = it
            }
            // DisplayView
            cardNumberBrand.observe(viewLifecycleOwner) { brand ->
                cardDisplay.setBrand(brand)
            }
            cardNumberInput.observe(viewLifecycleOwner) { cardNumber ->
                cardDisplay.setCardNumber(cardNumber.input.orEmpty())
            }
            cardExpirationInput.observe(viewLifecycleOwner) { expiration ->
                cardDisplay.setCardExpiration(expiration.input.orEmpty())
            }
            cardHolderNameInput.observe(viewLifecycleOwner) { holderName ->
                cardDisplay.setCardHolderName(holderName.input.orEmpty())
            }
            cardCvcInput.observe(viewLifecycleOwner) { cvc ->
                cardDisplay.setCardCvcInputLength(cvc.input?.length ?: 0)
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
