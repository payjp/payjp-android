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

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.material.shape.AbsoluteCornerSize
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import jp.pay.android.R
import jp.pay.android.model.CardBrand
import jp.pay.android.ui.extension.displayLogoResourceId
import jp.pay.android.ui.extension.fullMaskedPan
import jp.pay.android.ui.extension.lastMaskedPan
import jp.pay.android.validator.CardNumberValidator
import jp.pay.android.validator.CardNumberValidatorService

internal class PayjpCardDisplayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    companion object {
        private const val FLIP_DURATION = 300L
        // https://developer.android.com/reference/android/view/View#setCameraDistance(float)
        private const val CAMERA_DISTANCE_DEFAULT = 1280 * 5
    }

    private val frameFront: ViewGroup
    private val frameBack: ViewGroup
    private val numberDisplay: TextView
    private val expirationDisplay: TextView
    private val holderDisplay: TextView
    private val cvcDisplay: TextView
    private val cvcDisplayAmex: TextView
    private val brandLogo: ImageView

    private val cardNumberValidator = CardNumberValidator
    private val frontToBack: AnimatorSet
    private val backToFront: AnimatorSet
    private val highlightBackground: MaterialShapeDrawable
    private var brand: CardBrand = CardBrand.UNKNOWN
    private var frontVisible = true
    private var currentNum: CharSequence? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.payjp_card_display_view, this, true)

        frameFront = findViewById(R.id.card_display_front)
        frameBack = findViewById(R.id.card_display_back)
        numberDisplay = findViewById(R.id.display_pan)
        expirationDisplay = findViewById(R.id.display_expiration)
        holderDisplay = findViewById(R.id.display_holder)
        cvcDisplay = findViewById(R.id.display_cvc_default)
        cvcDisplayAmex = findViewById(R.id.display_cvc_amex)
        brandLogo = findViewById(R.id.display_brand_logo)

        val scale = resources.displayMetrics.density
        (CAMERA_DISTANCE_DEFAULT * scale).let {
            frameFront.cameraDistance = it
            frameBack.cameraDistance = it
        }

        val cardBackModel = ShapeAppearanceModel.Builder()
            .setAllCornerSizes(AbsoluteCornerSize(32f))
            .build()
        val backgroundColor = ContextCompat.getColor(context, R.color.payjp_card_display_background)
        val defaultBackground = MaterialShapeDrawable(cardBackModel).apply {
            fillColor = ColorStateList.valueOf(backgroundColor)
        }
        frameFront.background = defaultBackground
        frameBack.background = defaultBackground
        frontToBack = createFlipAnimator(frameFront, frameBack).apply {
            addListener(AnimatorOnEndListener {
                frontVisible = false
            })
        }
        backToFront = createFlipAnimator(frameBack, frameFront).apply {
            addListener(AnimatorOnEndListener {
                frontVisible = true
            })
        }
        highlightBackground = MaterialShapeDrawable(
            ShapeAppearanceModel.Builder()
                .setAllCornerSizes(AbsoluteCornerSize(16f))
                .build()
        ).apply {
            strokeWidth = 4.0f
            fillColor =
                ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.transparent))
            strokeColor =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.payjp_primaryColor))
        }
    }

    fun isFrontVisible() = frontVisible

    fun flipToBack() {
        frontToBack.start()
    }

    fun flipToFront() {
        backToFront.start()
    }

    fun setBrand(brand: CardBrand) {
        if (brand == this.brand) {
            return
        }
        this.brand = brand
        cvcDisplayAmex.visibility = when (brand) {
            CardBrand.AMEX -> View.VISIBLE
            else -> View.INVISIBLE
        }
        brand.displayLogoResourceId?.let { brandLogo.setImageResource(it) }
        brandLogo.visibility = when (brand) {
            CardBrand.UNKNOWN -> View.GONE
            else -> View.VISIBLE
        }
    }

    fun updateFocus(elementType: CardFormElementType, hasFocus: Boolean) {
        updateHighlight(elementType, hasFocus)
        if (elementType == CardFormElementType.Number) {
            toggleCardNumber(hasFocus)
        }
    }

    private fun updateHighlight(elementType: CardFormElementType, highlighted: Boolean) {
        val view = when (elementType) {
            CardFormElementType.Number -> numberDisplay
            CardFormElementType.Expiration -> expirationDisplay
            CardFormElementType.Cvc -> when (brand) {
                CardBrand.AMEX -> cvcDisplayAmex
                else -> cvcDisplay
            }
            CardFormElementType.HolderName -> holderDisplay
        }
        view.background = highlightBackground.takeIf { highlighted }
    }

    private fun toggleCardNumber(hasFocus: Boolean) {
        currentNum?.let { pan ->
            // only if number has valid length.
            if (cardNumberValidator.isCardNumberLengthValid(
                    pan.filter(Character::isDigit).toString(), brand
                ) == CardNumberValidatorService.CardNumberLengthStatus.MATCH
            ) {
                if (hasFocus) {
                    this.numberDisplay.text = pan
                } else {
                    this.numberDisplay.text =
                        brand.lastMaskedPan(maskChar = '•', delimiter = ' ', src = pan, lastSize = 4)
                }
            }
        }
    }

    fun setCardNumber(cardNumber: CharSequence) {
        val allMask = brand.fullMaskedPan(maskChar = 'X', delimiter = ' ')
        this.numberDisplay.text = filledWithHintSpannable(cardNumber, allMask).also {
            currentNum = it
        }
    }

    fun setCardExpiration(cardExpiration: CharSequence) {
        this.expirationDisplay.text = filledWithHintSpannable(cardExpiration, "MM/YY")
    }

    fun setCardHolderName(cardHolderName: CharSequence) {
        this.holderDisplay.text = cardHolderName
    }

    fun setCardCvcInputLength(length: Int) {
        val text = "•".repeat(length)
        this.cvcDisplay.text = filledWithHintSpannable(
            actual = text,
            hint = "•••",
            textColorRes = R.color.payjp_card_display_text_color_cvc_default,
            textHintColorRes = R.color.payjp_card_display_text_color_hint_cvc_default
        )
        this.cvcDisplayAmex.text = filledWithHintSpannable(
            actual = text,
            hint = "••••"
        )
    }

    private fun filledWithHintSpannable(
        actual: CharSequence,
        hint: CharSequence,
        @ColorRes textColorRes: Int = R.color.payjp_card_display_text_color,
        @ColorRes textHintColorRes: Int = R.color.payjp_card_display_text_color_hint
    ): Spannable {
        return SpannableStringBuilder(actual).apply {
            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, textColorRes)),
                0,
                actual.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (actual.length < hint.length) {
                append(hint.subSequence(actual.length, hint.length))
                setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, textHintColorRes)),
                    actual.length,
                    hint.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun createFlipAnimator(front: View, back: View): AnimatorSet {
        val flipDuration = FLIP_DURATION
        val flipInAlphaIn = ObjectAnimator.ofFloat(back, "alpha", 0f, 1f).apply {
            duration = 0
        }
        val flipInAlphaOut = ObjectAnimator.ofFloat(back, "alpha", 1f, 0f).apply {
            duration = 0
        }
        val flipInRotationIn = ObjectAnimator.ofFloat(back, "rotationY", 180f, 0f).apply {
            duration = flipDuration
        }
        val flipOutAlphaOut = ObjectAnimator.ofFloat(front, "alpha", 1f, 0f).apply {
            duration = 0
        }
        val flipOutRotationOut = ObjectAnimator.ofFloat(front, "rotationY", 0f, -180f).apply {
            duration = flipDuration
        }
        return AnimatorSet().apply {
            play(flipInAlphaOut).with(flipInRotationIn)
            play(flipInAlphaIn).after(flipInAlphaOut).after(flipDuration / 2)
            play(flipOutRotationOut).with(flipInAlphaOut)
            play(flipOutAlphaOut).after(flipInAlphaOut).after(flipDuration / 2)
        }
    }

    class AnimatorOnEndListener(
        private val onAnimationEnd: (animation: Animator) -> Unit
    ) : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator) {}

        override fun onAnimationEnd(animation: Animator) {
            this.onAnimationEnd.invoke(animation)
        }

        override fun onAnimationCancel(animation: Animator) {}

        override fun onAnimationStart(animation: Animator) {}
    }
}
