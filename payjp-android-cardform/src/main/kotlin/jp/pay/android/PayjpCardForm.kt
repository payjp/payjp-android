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
package jp.pay.android

import android.app.Activity
import android.content.Intent
import androidx.annotation.IntDef
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import jp.pay.android.model.CardBrand
import jp.pay.android.model.TenantId
import jp.pay.android.plugin.CardScannerPlugin
import jp.pay.android.ui.PayjpCardFormActivity
import jp.pay.android.ui.PayjpCardFormResultCallback
import jp.pay.android.ui.widget.PayjpCardFormAbstractFragment
import jp.pay.android.ui.widget.PayjpCardFormCardDisplayFragment
import jp.pay.android.ui.widget.PayjpCardFormFragment
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Card form client.
 */
object PayjpCardForm {

    /**
     * Face type of Card form.
     * PAY.JP provide two different form UI to input credit card.
     * [FACE_MULTI_LINE] is the default form, it is standard form with multi rows.
     */
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(FACE_MULTI_LINE, FACE_CARD_DISPLAY)
    annotation class CardFormFace
    const val FACE_MULTI_LINE = 0
    const val FACE_CARD_DISPLAY = 1

    internal const val CARD_FORM_DELIMITER_NUMBER = '-'
    internal const val CARD_FORM_DELIMITER_NUMBER_DISPLAY = ' '
    internal const val CARD_FORM_DELIMITER_EXPIRATION = '/'

    private var cardScannerPlugin: CardScannerPlugin? = null
    private var tokenService: PayjpTokenService? = null
    private var tokenHandlerExecutor: TokenHandlerExecutor? = null

    fun configure(
        logger: PayjpLogger,
        tokenService: PayjpTokenService,
        cardScannerPlugin: CardScannerPlugin?,
        handler: PayjpTokenBackgroundHandler?,
        callbackExecutor: Executor?
    ) {
        this.tokenService = tokenService
        this.cardScannerPlugin = cardScannerPlugin
        tokenHandlerExecutor = if (handler != null && callbackExecutor != null) {
            TokenHandlerExecutorImpl(
                handler = handler,
                backgroundExecutor = newBackgroundExecutor(),
                futureExecutor = newBackgroundExecutor(),
                callbackExecutor = callbackExecutor,
                logger = logger
            )
        } else null
    }

    private fun newBackgroundExecutor() = Executors.newSingleThreadExecutor { r ->
        Thread(r, "payjp-android").apply {
            priority = Thread.MIN_PRIORITY
        }
    }

    internal fun tokenService(): PayjpTokenService = checkNotNull(tokenService) {
        "You must initialize Payjp first"
    }

    internal fun tokenHandlerExecutor(): TokenHandlerExecutor? = tokenHandlerExecutor

    internal fun cardScannerPlugin(): CardScannerPlugin? = cardScannerPlugin

    /**
     * Start card form screen from Activity.
     *
     * @param activity activity
     * @param requestCode requestCode. The default is [PayjpCardFormActivity.DEFAULT_CARD_FORM_REQUEST_CODE]
     * @param tenant tenant (only for platformer)
     * @param face card form face. The default is [FACE_MULTI_LINE].
     */
    @MainThread
    @JvmOverloads
    fun start(
        activity: Activity,
        requestCode: Int? = null,
        tenant: TenantId? = null,
        @CardFormFace face: Int = FACE_MULTI_LINE
    ) = PayjpCardFormActivity.start(activity = activity, requestCode = requestCode, tenant = tenant, face = face)

    /**
     * Start card form screen from Fragment.
     *
     * @param fragment fragment
     * @param requestCode requestCode. The default is [PayjpCardFormActivity.DEFAULT_CARD_FORM_REQUEST_CODE]
     * @param tenant tenant (only for platformer)
     */
    @MainThread
    @JvmOverloads
    fun start(
        fragment: Fragment,
        requestCode: Int? = null,
        tenant: TenantId? = null,
        @CardFormFace face: Int = FACE_MULTI_LINE
    ) = PayjpCardFormActivity.start(fragment = fragment, requestCode = requestCode, tenant = tenant, face = face)

    /**
     * Handle the result from the activity which is started by [PayjpCardForm.start].
     * Use this at [Activity.onActivityResult] in the activity that you call [PayjpCardForm.start].
     *
     * @param data the intent from [Activity.onActivityResult].
     * @param callback you can get card token if it is success.
     */
    @MainThread
    fun handleResult(data: Intent?, callback: PayjpCardFormResultCallback) =
        PayjpCardFormActivity.onActivityResult(data = data, callback = callback)

    /**
     * Create new fragment instance with args
     *
     * @param holderNameEnabled a option it require card holder name or not.
     * @param tenantId a option for platform tenant.
     * @param acceptedBrands accepted brands. if it is null, the fragment try to get them.
     * @return fragment
     */
    @JvmOverloads
    @Deprecated(
        message = "Use newCardFormFragment()",
        replaceWith = ReplaceWith(
            "newCardFormFragment(holderNameEnabled, tenantId, acceptedBrands, face)",
            "jp.pay.android"
        )
    )
    fun newFragment(
        holderNameEnabled: Boolean = true,
        tenantId: TenantId? = null,
        acceptedBrands: Array<CardBrand>? = null
    ): PayjpCardFormFragment =
        PayjpCardFormFragment.newInstance(holderNameEnabled, tenantId, acceptedBrands)

    /**
     * Create new Fragment instance that inherited [PayjpCardFormAbstractFragment].
     *
     * @param holderNameEnabled a option it require card holder name or not.
     * @param tenantId a option for platform tenant.
     * @param acceptedBrands accepted brands. if it is null, the fragment try to get them.
     * @param face form appearance type. cf. [PayjpCardForm.CardFormFace]
     */
    @JvmOverloads
    fun newCardFormFragment(
        holderNameEnabled: Boolean = true,
        tenantId: TenantId? = null,
        acceptedBrands: Array<CardBrand>? = null,
        @CardFormFace face: Int = FACE_MULTI_LINE
    ): PayjpCardFormAbstractFragment = when (face) {
        FACE_MULTI_LINE -> PayjpCardFormFragment.newInstance(holderNameEnabled, tenantId, acceptedBrands)
        FACE_CARD_DISPLAY -> PayjpCardFormCardDisplayFragment.newInstance(tenantId, acceptedBrands)
        else -> throw IllegalArgumentException("unknown face $face")
    }
}
