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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.fragment.app.Fragment
import jp.pay.android.PayjpCardForm
import jp.pay.android.Task
import jp.pay.android.exception.PayjpInvalidCardFormException
import jp.pay.android.model.Token
import jp.pay.android.ui.PayjpSearchCountryCodeActivity
import jp.pay.android.ui.PayjpSearchCountryCodeActivity.Companion.EXTRA_REGION
import jp.pay.android.ui.widget.PayjpCardFormView.CardFormEditorListener
import jp.pay.android.ui.widget.PayjpCardFormView.OnFetchAcceptedBrandsListener
import jp.pay.android.ui.widget.PayjpCardFormView.OnValidateInputListener
import jp.pay.android.util.Tasks

abstract class PayjpCardFormAbstractFragment :
    Fragment(),
    PayjpCardFormView {

    protected var onValidateInputListener: OnValidateInputListener? = null
        private set
    protected var onFetchAcceptedBrandsListener: OnFetchAcceptedBrandsListener? = null
        private set
    protected var cardFormEditorListener: CardFormEditorListener? = null
        private set
    internal var viewModel: CardFormViewModel? = null
        private set
    protected val searchCountryCodeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            result?.data?.getStringExtra(EXTRA_REGION)?.let { region ->
                PayjpCardForm.phoneNumberService().findCountryCodeByRegion(requireContext(), region)?.let { countryCode ->
                    viewModel?.selectCountryCode(countryCode)
                }
            }
        }
    }

    internal abstract fun createViewModel(): CardFormViewModel

    abstract fun setUpUI(view: ViewGroup)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnValidateInputListener) {
            this.onValidateInputListener = context
        }
        if (context is OnFetchAcceptedBrandsListener) {
            this.onFetchAcceptedBrandsListener = context
        }
        if (context is CardFormEditorListener) {
            this.cardFormEditorListener = context
        }
    }

    override fun onDetach() {
        this.onValidateInputListener = null
        this.onFetchAcceptedBrandsListener = null
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = createViewModel().apply {
            lifecycle.addObserver(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI(view as ViewGroup)
        viewModel?.apply {
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
            isValid.observe(viewLifecycleOwner) {
                onValidateInputListener?.onValidateInput(this@PayjpCardFormAbstractFragment, it)
            }
        }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }

    override fun isValid(): Boolean = viewModel?.isValid?.value ?: false

    override fun validateCardForm(): Boolean {
        viewModel?.validate()
        return isValid
    }

    override fun createToken(): Task<Token> {
        return createToken(useThreeDSecure = false)
    }

    override fun createToken(useThreeDSecure: Boolean): Task<Token> {
        return viewModel?.createToken(useThreeDSecure) ?: Tasks.failure(
            PayjpInvalidCardFormException("Card form is not ready.")
        )
    }

    protected fun onEditorAction(view: TextView, actionId: Int, event: KeyEvent?): Boolean =
        when (actionId) {
            EditorInfo.IME_ACTION_DONE -> {
                cardFormEditorListener?.onLastFormEditorActionDone(this, view, event) ?: false
            }
            else -> false
        }

    protected fun startSearchCountryCode() {
        val intent = Intent(requireActivity(), PayjpSearchCountryCodeActivity::class.java)
        searchCountryCodeLauncher.launch(intent)
    }
}
