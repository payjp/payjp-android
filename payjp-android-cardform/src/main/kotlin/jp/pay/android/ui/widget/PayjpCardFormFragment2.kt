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

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.transition.TransitionManager
import com.google.android.material.shape.AbsoluteCornerSize
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputLayout
import jp.pay.android.PayjpCardForm
import jp.pay.android.R
import jp.pay.android.model.CardBrand
import jp.pay.android.model.TenantId
import jp.pay.android.ui.extension.addOnTextChanged
import jp.pay.android.ui.extension.cvcIconResourceId
import jp.pay.android.ui.extension.logoResourceId
import jp.pay.android.ui.extension.setErrorOrNull
import jp.pay.android.validator.CardCvcInputTransformer
import jp.pay.android.validator.CardExpirationInputTransformer
import jp.pay.android.validator.CardHolderNameInputTransformer
import jp.pay.android.validator.CardNumberInputTransformer

class PayjpCardFormFragment2 : PayjpCardFormAbstractFragment(R.layout.payjp_card_form_view_2) {

    companion object {
        private const val ARGS_HOLDER_NAME_ENABLED = "ARGS_HOLDER_NAME_ENABLED"
        private const val ARGS_TENANT_ID = "ARGS_TENANT_ID"
        private const val ARGS_ACCEPTED_BRANDS = "ARGS_ACCEPTED_BRANDS"

        /**
         * Create new fragment instance with args
         *
         * @param holderNameEnabled a option it require card holder name or not.
         * @param tenantId a option for platform tenant.
         * @param acceptedBrands accepted brands. if it is null, the fragment try to get them.
         * @return fragment
         */
        @JvmStatic
        fun newInstance(
            holderNameEnabled: Boolean = true,
            tenantId: TenantId? = null,
            acceptedBrands: Array<CardBrand>? = null
        ): PayjpCardFormFragment2 =
            PayjpCardFormFragment2().apply {
                arguments = Bundle().apply {
                    putBoolean(ARGS_HOLDER_NAME_ENABLED, holderNameEnabled)
                    putString(ARGS_TENANT_ID, tenantId?.id)
                    putParcelableArray(ARGS_ACCEPTED_BRANDS, acceptedBrands)
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

    private val delimiterExpiration = PayjpCardForm.CARD_FORM_DELIMITER_EXPIRATION
    private val cardNumberFormatter =
        CardNumberFormatTextWatcher(PayjpCardForm.CARD_FORM_DELIMITER_NUMBER)

    override fun onScanResult(cardNumber: String?) {
        cardNumber?.let(numberEditText::setText)
        expirationEditText.requestFocusFromTouch()
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

    override fun setUpUI(view: ViewGroup) {
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
            CardExpirationFormatTextWatcher(delimiterExpiration)
        )
        // default cvc length
        cvcEditText.filters =
            arrayOf<InputFilter>(InputFilter.LengthFilter(CardBrand.UNKNOWN.cvcLength))
        PayjpCardForm.cardScannerPlugin()?.let { bridge ->
            numberLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
            numberLayout.setEndIconOnClickListener {
                bridge.startScanActivity(this)
            }
        }
        // editor
        holderNameEditText.setOnEditorActionListener(this::onEditorAction)
        cvcEditText.setOnEditorActionListener { v, actionId, event ->
            if (viewModel?.cardHolderNameEnabled?.value == false) {
                onEditorAction(v, actionId, event)
            } else {
                false
            }
        }

        // display

        val cardDisplayFront = view.findViewById<ViewGroup>(R.id.card_display_front)
        val cardDisplayBack = view.findViewById<ViewGroup>(R.id.card_display_back)

        // https://developer.android.com/reference/android/view/View#setCameraDistance(float)
        val defaultDistance = 1280
        val scale = resources.displayMetrics.density
        (defaultDistance * scale * 5).let {
            cardDisplayFront.cameraDistance = it
            cardDisplayBack.cameraDistance = it
        }

        val cardBackModel = ShapeAppearanceModel.Builder()
            .setAllCornerSizes(AbsoluteCornerSize(16f))
            .build()
        cardDisplayFront.background = MaterialShapeDrawable(cardBackModel).apply {
            fillColor = ColorStateList.valueOf(Color.RED)
            setPadding(12, 12, 12, 12)
        }
        cardDisplayBack.background = MaterialShapeDrawable(cardBackModel).apply {
            fillColor = ColorStateList.valueOf(Color.BLUE)
            setPadding(12, 12, 12, 12)
        }

        val flipDuration = 1500L
        val flipInAlphaIn = ObjectAnimator.ofFloat(cardDisplayBack, "alpha", 0f, 1f).apply {
            duration = 0
        }
        val flipInAlphaOut = ObjectAnimator.ofFloat(cardDisplayBack, "alpha", 1f, 0f).apply {
            duration = 0
        }
        val flipInRotationIn = ObjectAnimator.ofFloat(cardDisplayBack, "rotationY", -180f, 0f).apply {
            duration = flipDuration
        }
        val flipOutAlphaOut = ObjectAnimator.ofFloat(cardDisplayFront, "alpha", 1f, 0f).apply {
            duration = 0
        }
        val flipOutRotationOut = ObjectAnimator.ofFloat(cardDisplayFront, "rotationY", 0f, 180f).apply {
            duration = flipDuration
        }
        val frontToBack = AnimatorSet().apply {
            play(flipInAlphaOut).with(flipInRotationIn)
            play(flipInAlphaIn).after(flipInAlphaOut).after(flipDuration / 2)
            play(flipOutRotationOut).with(flipInAlphaOut)
            play(flipOutAlphaOut).after(flipInAlphaOut).after(flipDuration / 2)
        }

        cardDisplayFront.setOnClickListener {
            frontToBack.start()
        }
    }

    override fun createViewModel(): CardFormViewModel {
        val tenantId = arguments?.getString(ARGS_TENANT_ID)?.let { TenantId(it) }
        val holderNameEnabled = arguments?.getBoolean(ARGS_HOLDER_NAME_ENABLED) ?: true
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
            holderNameEnabledDefault = holderNameEnabled,
            acceptedBrands = acceptedBrandArray?.filterIsInstance<CardBrand>()
        )
        return ViewModelProvider(requireActivity(), factory).get(CardFormViewModel::class.java)
                .apply {
                    acceptedBrands.observe(viewLifecycleOwner) { oneOff ->
                        oneOff.consume {
                            onFetchAcceptedBrandsListener?.onSuccessFetchAcceptedBrands(it)
                        }
                    }
                    errorFetchAcceptedBrands.observe(viewLifecycleOwner) { oneOff ->
                        oneOff.consume {
                            onFetchAcceptedBrandsListener?.onErrorFetchAcceptedBrands(it)
                        }
                    }
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
                        onValidateInputListener?.onValidateInput(this@PayjpCardFormFragment2, it)
                    }
                    cardNumberBrand.observe(viewLifecycleOwner) {
                        cardNumberFormatter.brand = it
                        cvcEditText.filters =
                            arrayOf<InputFilter>(InputFilter.LengthFilter(it.cvcLength))
                        cvcLayout.setEndIconDrawable(it.cvcIconResourceId)
                        numberLayout.setStartIconDrawable(it.logoResourceId)
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
                    this@PayjpCardFormFragment2.lifecycle.addObserver(this)
                }
    }
}
