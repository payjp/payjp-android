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
package jp.pay.android.ui

import androidx.lifecycle.LiveData
import jp.pay.android.Task
import jp.pay.android.model.CardBrand
import jp.pay.android.model.Token
import jp.pay.android.model.TokenId
import jp.pay.android.verifier.ui.PayjpThreeDSecureResult

internal interface CardFormScreenContract {

    interface Input {

        fun onValidateInput(isValid: Boolean)

        fun onCreateToken(task: Task<Token>)

        fun onClickReload()

        fun onCompleteCardVerify(result: PayjpThreeDSecureResult)

        fun onAddedCardForm()

        fun onStartedVerify()

        fun onDisplayedErrorMessage()

        fun onDisplaySnackBarMessage()
    }

    interface Output {
        val contentViewVisibility: LiveData<Int>
        val errorViewVisibility: LiveData<Int>
        val loadingViewVisibility: LiveData<Int>
        val reloadContentButtonVisibility: LiveData<Int>
        val submitButtonVisibility: LiveData<Int>
        val submitButtonProgressVisibility: LiveData<Int>
        val submitButtonIsEnabled: LiveData<Boolean>
        val acceptedBrands: LiveData<ArrayList<CardBrand>>
        val addCardFormCommand: LiveData<ArrayList<CardBrand>>
        val errorDialogMessage: LiveData<CharSequence>
        val errorViewText: LiveData<CharSequence>
        val success: LiveData<Token>
        val startVerifyWithTokenIdCommand: LiveData<TokenId>
        val snackBarMessage: LiveData<Int>
    }
}
