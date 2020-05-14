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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import java.lang.ref.WeakReference
import jp.pay.android.PayjpCardForm
import jp.pay.android.R
import jp.pay.android.Task
import jp.pay.android.exception.PayjpInvalidCardFormException
import jp.pay.android.model.Token
import jp.pay.android.plugin.CardScannerPlugin
import jp.pay.android.ui.widget.PayjpCardFormView.CardFormEditorListener
import jp.pay.android.ui.widget.PayjpCardFormView.OnFetchAcceptedBrandsListener
import jp.pay.android.ui.widget.PayjpCardFormView.OnValidateInputListener
import jp.pay.android.util.Tasks

abstract class PayjpCardFormAbstractFragment(layoutId: Int) : Fragment(layoutId), PayjpCardFormView,
    CardScannerPlugin.CardScanOnResultListener,
    CardScannerPlugin.CardScannerPermissionDelegate {

    protected var onValidateInputListener: OnValidateInputListener? = null
        private set
    protected var onFetchAcceptedBrandsListener: OnFetchAcceptedBrandsListener? = null
        private set
    protected var cardFormEditorListener: CardFormEditorListener? = null
        private set
    internal var viewModel: CardFormViewModel? = null
        private set

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        PayjpCardForm.cardScannerPlugin()?.onActivityResult(requestCode, resultCode, data, this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PayjpCardForm.cardScannerPlugin()
            ?.onRequestPermissionResult(this, requestCode, grantResults, this)
    }

    override fun onNeverAskAgainCardScannerPermission() {
        activity?.let { activity ->
            val weakFragment = WeakReference(this)
            AlertDialog.Builder(activity)
                .setTitle(R.string.payjp_card_scanner_permission_denied_title)
                .setMessage(R.string.payjp_card_scanner_permission_denied_message)
                .setPositiveButton(R.string.payjp_card_scanner_permission_denied_setting) { _, _ ->
                    weakFragment.get()?.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .apply { data = Uri.parse("package:${activity.packageName}") }
                    )
                }
                .setNegativeButton(R.string.payjp_card_scanner_permission_denied_cancel) { _, _ -> }
                .create()
                .show()
        }
    }

    override fun isValid(): Boolean = viewModel?.isValid?.value ?: false

    override fun validateCardForm(): Boolean {
        viewModel?.validate()
        return isValid
    }

    override fun createToken(): Task<Token> {
        return viewModel?.createToken() ?: Tasks.failure(
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
}
