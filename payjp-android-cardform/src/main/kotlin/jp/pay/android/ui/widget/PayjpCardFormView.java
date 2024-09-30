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

package jp.pay.android.ui.widget;


import android.view.KeyEvent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import jp.pay.android.Task;
import jp.pay.android.model.CardBrand;
import jp.pay.android.model.Token;

/**
 * Interface of view create token.
 */
public interface PayjpCardFormView {

    /**
     * Check current card input.
     *
     * @return true if the card input is valid.
     */
    boolean isValid();

    /**
     * Validate input and force update form.
     *
     * @return true if the card input is valid.
     */
    boolean validateCardForm();

    /**
     * Create token.
     * @return task
     * @see [jp.pay.android.PayjpTokenService.createToken]
     */
    @NonNull
    Task<Token> createToken();

    /**
     * Create token with 3-D Secure.
     *
     * @param useThreeDSecure if true, use 3-D secure.
     * @return task
     * @see [jp.pay.android.PayjpTokenService.createToken]
     */
    @NonNull
    Task<Token> createToken(boolean useThreeDSecure);

    /**
     * listener for every validation result.
     */
    interface OnValidateInputListener {

        /**
         * @param view    view
         * @param isValid if true card input is ready to create token.
         */
        void onValidateInput(@NonNull PayjpCardFormView view, boolean isValid);
    }

    /**
     * listener for result of request accepted brands.
     */
    interface OnFetchAcceptedBrandsListener {

        /**
         * Success callback of fetch accepted brands.
         *
         * @param brands brands that accepted.
         */
        void onSuccessFetchAcceptedBrands(@NonNull List<CardBrand> brands);

        /**
         * Error callback of fetch accepted brands.
         *
         * @param error error
         */
        void onErrorFetchAcceptedBrands(@NonNull Throwable error);
    }

    /**
     * listener for editor events.
     */
    interface CardFormEditorListener {

        /**
         * listener for the last part form editor action {@link android.view.inputmethod.EditorInfo#IME_ACTION_DONE}.
         * params is from {@link TextView.OnEditorActionListener#onEditorAction(TextView, int, KeyEvent)}.
         *
         * @param view view
         * @param textView TextView
         * @param event event
         * @return handle or not
         */
        boolean onLastFormEditorActionDone(
                @NonNull PayjpCardFormView view,
                @NonNull TextView textView,
                @Nullable KeyEvent event);
    }
}