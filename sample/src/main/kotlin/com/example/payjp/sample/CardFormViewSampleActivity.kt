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
package com.example.payjp.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.payjp.sample.databinding.ActivityCardFormViewSampleBinding
import jp.pay.android.Payjp
import jp.pay.android.Task
import jp.pay.android.exception.PayjpThreeDSecureRequiredException
import jp.pay.android.model.CardBrand
import jp.pay.android.model.ThreeDSecureToken
import jp.pay.android.model.Token
import jp.pay.android.ui.widget.PayjpCardFormAbstractFragment
import jp.pay.android.ui.widget.PayjpCardFormView
import jp.pay.android.verifier.ui.PayjpThreeDSecureResultCallback

private const val FRAGMENT_CARD_FORM = "FRAGMENT_CARD_FORM"

class CardFormViewSampleActivity :
    AppCompatActivity(),
    PayjpCardFormView.OnValidateInputListener,
    PayjpCardFormView.OnFetchAcceptedBrandsListener {

    private var createToken: Task<Token>? = null
    private var getToken: Task<Token>? = null
    private lateinit var cardFormFragment: PayjpCardFormAbstractFragment
    private lateinit var binding: ActivityCardFormViewSampleBinding

    override fun onValidateInput(view: PayjpCardFormView, isValid: Boolean) {
        binding.buttonCreateToken.isEnabled = isValid
    }

    override fun onSuccessFetchAcceptedBrands(brands: MutableList<CardBrand>) {
        Log.i("CardFormViewSample", "acceptedBrands => $brands")
    }

    override fun onErrorFetchAcceptedBrands(error: Throwable) {
        Log.i("CardFormViewSample", "onErrorFetchAcceptedBrands => $error")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(restoreTheme().id)
        binding = ActivityCardFormViewSampleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        findCardFormFragment()
        binding.buttonCreateToken.setOnClickListener {
            if (!cardFormFragment.isValid) {
                return@setOnClickListener
            }
            createToken()
        }
        binding.buttonCreateTokenWithValidate.setOnClickListener {
            if (cardFormFragment.validateCardForm()) {
                createToken()
            }
        }

        binding.buttonGetToken.setOnClickListener {
            getToken(binding.textTokenId.text.toString())
        }

        binding.switchCardHolderName.setOnCheckedChangeListener { _, isChecked ->
            cardFormFragment.setCardHolderNameInputEnabled(isChecked)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        createToken?.cancel()
        getToken?.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_theme -> {
                showThemeChooserDialog()
                return true
            }
            R.id.menu_daynight -> {
                changeDayNight()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Payjp.verifier().handleThreeDSecureResult(
            requestCode,
            PayjpThreeDSecureResultCallback {
                if (it.isSuccess()) {
                    createTokenForTds(it.retrieveThreeDSecureToken())
                }
            }
        )
    }

    private fun createToken() {
        binding.layoutButtons.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE
        binding.textTokenContent.visibility = View.INVISIBLE
        // create token
        createToken = cardFormFragment.createToken()
        createToken?.enqueue(
            object : Task.Callback<Token> {
                override fun onSuccess(data: Token) {
                    Log.i("CardFormViewSample", "token => $data")
                    binding.textTokenId.setText(data.id)
                    binding.textTokenContent.text = "The token has created."
                    binding.progressBar.visibility = View.GONE
                    binding.layoutButtons.visibility = View.VISIBLE
                    binding.textTokenContent.visibility = View.VISIBLE
                }

                override fun onError(throwable: Throwable) {
                    Log.e("CardFormViewSample", "failure creating token", throwable)
                    if (throwable is PayjpThreeDSecureRequiredException) {
                        // if support 3DSecure
                        // NOTE: 3DSecure is a limited feature for now.
                        Payjp.verifier()
                            .startThreeDSecureFlow(throwable.token, this@CardFormViewSampleActivity)
                    } else {
                        binding.textTokenContent.text = throwable.toString()
                        binding.progressBar.visibility = View.GONE
                        binding.layoutButtons.visibility = View.VISIBLE
                        binding.textTokenContent.visibility = View.VISIBLE
                    }
                }
            }
        )
    }

    private fun getToken(id: String) {
        binding.layoutButtons.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE
        binding.textTokenContent.visibility = View.INVISIBLE
        // get token
        getToken = Payjp.token().getToken(id)
        getToken?.enqueue(
            object : Task.Callback<Token> {
                override fun onSuccess(data: Token) {
                    Log.i("CardFormViewSample", "token => $data")
                    binding.textTokenContent.text = data.toString()
                    binding.progressBar.visibility = View.GONE
                    binding.layoutButtons.visibility = View.VISIBLE
                    binding.textTokenContent.visibility = View.VISIBLE
                }

                override fun onError(throwable: Throwable) {
                    Log.e("CardFormViewSample", "failure creating token", throwable)
                    binding.textTokenContent.text = throwable.toString()
                    binding.progressBar.visibility = View.GONE
                    binding.layoutButtons.visibility = View.VISIBLE
                    binding.textTokenContent.visibility = View.VISIBLE
                }
            }
        )
    }

    private fun findCardFormFragment() {
        supportFragmentManager.let { manager ->
            val f = manager.findFragmentByTag(FRAGMENT_CARD_FORM) as? PayjpCardFormAbstractFragment
            cardFormFragment = f ?: Payjp.cardForm().newCardFormFragment()
            if (!cardFormFragment.isAdded) {
                manager
                    .beginTransaction().apply {
                        replace(R.id.card_form_view, cardFormFragment, FRAGMENT_CARD_FORM)
                    }
                    .commit()
            }
        }
    }

    private fun showThemeChooserDialog() {
        val items = Theme.values().map { it.name }
        val current = Theme.values().toList().indexOf(restoreTheme())
        AlertDialog.Builder(this)
            .setTitle("テーマを選択")
            .setSingleChoiceItems(items.toTypedArray(), current) { _, which ->
                val theme = Theme.values()[which]
                changeTheme(theme)
            }
            .create()
            .show()
    }

    private fun changeTheme(theme: Theme) {
        getSharedPreferences("sample", Context.MODE_PRIVATE)
            .edit()
            .putString("theme", theme.name)
            .apply()
        startActivity(
            Intent(this, CardFormViewSampleActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun restoreTheme(): Theme = getSharedPreferences("sample", Context.MODE_PRIVATE)
        .getString("theme", null)
        ?.let { Theme.valueOf(it) }
        ?: Theme.AppCompat

    private fun changeDayNight() {
        val mode = when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun createTokenForTds(tdsToken: ThreeDSecureToken) {
        binding.layoutButtons.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE
        binding.textTokenContent.visibility = View.INVISIBLE
        // create token by 3DS
        createToken = Payjp.token().createToken(tdsToken)
        createToken?.enqueue(
            object : Task.Callback<Token> {
                override fun onSuccess(data: Token) {
                    Log.i("CardFormViewSample", "token => $data")
                    binding.textTokenId.setText(data.id)
                    binding.textTokenContent.text = "The token has created."
                    binding.progressBar.visibility = View.GONE
                    binding.layoutButtons.visibility = View.VISIBLE
                    binding.textTokenContent.visibility = View.VISIBLE
                }

                override fun onError(throwable: Throwable) {
                    Log.e("CardFormViewSample", "failure creating token", throwable)
                    binding.textTokenContent.text = throwable.toString()
                    binding.progressBar.visibility = View.GONE
                    binding.layoutButtons.visibility = View.VISIBLE
                    binding.textTokenContent.visibility = View.VISIBLE
                }
            }
        )
    }
}

enum class Theme(val id: Int) {
    AppCompat(R.style.AppTheme),
    MaterialComponent_Outline(R.style.Material_Outline),
    MaterialComponent_Filled(R.style.Material_Filled),
    MaterialComponent_Filled_Dense(R.style.Material_FilledDense),
}
