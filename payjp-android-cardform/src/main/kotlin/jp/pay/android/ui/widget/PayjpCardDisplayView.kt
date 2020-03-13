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
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.shape.AbsoluteCornerSize
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import jp.pay.android.R
import jp.pay.android.model.CardBrand
import kotlin.math.min

internal class PayjpCardDisplayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val frameFront: ViewGroup
    private val frameBack: ViewGroup
    private val numberDisplay: TextView
    private val expirationDisplay: TextView
    private val holderDisplay: TextView

    private val frontToBack: AnimatorSet
    private var brand: CardBrand = CardBrand.UNKNOWN

    init {
        LayoutInflater.from(context).inflate(R.layout.payjp_card_display_view, this, true)

        frameFront = findViewById(R.id.card_display_front)
        frameBack = findViewById(R.id.card_display_back)
        numberDisplay = findViewById(R.id.display_pan)
        expirationDisplay = findViewById(R.id.display_expiration)
        holderDisplay = findViewById(R.id.display_holder)

        // https://developer.android.com/reference/android/view/View#setCameraDistance(float)
        val defaultDistance = 1280
        val scale = resources.displayMetrics.density
        (defaultDistance * scale * 5).let {
            frameFront.cameraDistance = it
            frameBack.cameraDistance = it
        }

        val cardBackModel = ShapeAppearanceModel.Builder()
            .setAllCornerSizes(AbsoluteCornerSize(16f))
            .build()
        frameFront.background = MaterialShapeDrawable(cardBackModel).apply {
            fillColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.payjp_secondaryColor))
            setPadding(12, 12, 12, 12)
        }
        frameBack.background = MaterialShapeDrawable(cardBackModel).apply {
            fillColor = ColorStateList.valueOf(Color.BLUE)
            setPadding(12, 12, 12, 12)
        }

        frontToBack = createFlipAnimator(frameFront, frameBack, 500L)
    }

    fun flipToBack() {
        frontToBack.start()
    }

    fun setBrand(brand: CardBrand) {
        this.brand = brand
        // TODO
    }

    fun setCardNumber(cardNumber: CharSequence) {
        // TODO highlight input character
        val allMask = "XXXX XXXX XXXX XXXX"
        this.numberDisplay.text = allMask.replaceRange(0 until min(cardNumber.length, allMask.length), cardNumber)
    }

    fun setCardExpiration(cardExpiration: CharSequence) {
        val allMask = "XX/XX"
        this.expirationDisplay.text = allMask.replaceRange(0 until min(cardExpiration.length, allMask.length), cardExpiration)
    }

    fun setCardHolderName(cardHolderName: CharSequence) {
        this.holderDisplay.text = cardHolderName.ifEmpty { "NAME" }
    }

    fun setCardCvcInputLength(length: Int) {
        // TODO
    }

    private fun createFlipAnimator(front: View, back: View, flipDuration: Long): AnimatorSet {
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
}
