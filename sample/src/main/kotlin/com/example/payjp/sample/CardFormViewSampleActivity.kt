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
import jp.pay.android.Payjp
import jp.pay.android.Task
import jp.pay.android.model.CardBrand
import jp.pay.android.model.Token
import jp.pay.android.ui.widget.PayjpCardFormFragment
import jp.pay.android.ui.widget.PayjpCardFormView
import kotlinx.android.synthetic.main.activity_card_form_view_sample.button_create_token
import kotlinx.android.synthetic.main.activity_card_form_view_sample.button_create_token_with_validate
import kotlinx.android.synthetic.main.activity_card_form_view_sample.button_get_token
import kotlinx.android.synthetic.main.activity_card_form_view_sample.layout_buttons
import kotlinx.android.synthetic.main.activity_card_form_view_sample.progress_bar
import kotlinx.android.synthetic.main.activity_card_form_view_sample.switch_card_holder_name
import kotlinx.android.synthetic.main.activity_card_form_view_sample.text_token_content
import kotlinx.android.synthetic.main.activity_card_form_view_sample.text_token_id

private const val FRAGMENT_CARD_FORM = "FRAGMENT_CARD_FORM"

class CardFormViewSampleActivity : AppCompatActivity(),
    PayjpCardFormView.OnValidateInputListener,
    PayjpCardFormView.OnFetchAcceptedBrandsListener {

    private var createToken: Task<Token>? = null
    private var getToken: Task<Token>? = null
    private lateinit var cardFormFragment: PayjpCardFormFragment

    override fun onValidateInput(view: PayjpCardFormView, isValid: Boolean) {
        button_create_token.isEnabled = isValid
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
        setContentView(R.layout.activity_card_form_view_sample)
        findCardFormFragment()
        button_create_token.setOnClickListener {
            if (!cardFormFragment.isValid) {
                return@setOnClickListener
            }
            createToken()
        }
        button_create_token_with_validate.setOnClickListener {
            if (cardFormFragment.validateCardForm()) {
                createToken()
            }
        }

        button_get_token.setOnClickListener {
            getToken(text_token_id.text.toString())
        }

        switch_card_holder_name.setOnCheckedChangeListener { _, isChecked ->
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

    private fun createToken() {
        layout_buttons.visibility = View.INVISIBLE
        progress_bar.visibility = View.VISIBLE
        text_token_content.visibility = View.INVISIBLE
        // create token
        createToken = cardFormFragment.createToken()
        createToken?.enqueue(object : Task.Callback<Token> {
            override fun onSuccess(data: Token) {
                Log.i("CardFormViewSample", "token => $data")
                text_token_id.setText(data.id)
                text_token_content.text = "The token has created."
                progress_bar.visibility = View.GONE
                layout_buttons.visibility = View.VISIBLE
                text_token_content.visibility = View.VISIBLE
            }

            override fun onError(throwable: Throwable) {
                Log.e("CardFormViewSample", "failure creating token", throwable)
                text_token_content.text = throwable.toString()
                progress_bar.visibility = View.GONE
                layout_buttons.visibility = View.VISIBLE
                text_token_content.visibility = View.VISIBLE
            }
        })
    }

    private fun getToken(id: String) {
        layout_buttons.visibility = View.INVISIBLE
        progress_bar.visibility = View.VISIBLE
        text_token_content.visibility = View.INVISIBLE
        // get token
        getToken = Payjp.token().getToken(id)
        getToken?.enqueue(object : Task.Callback<Token> {
            override fun onSuccess(data: Token) {
                Log.i("CardFormViewSample", "token => $data")
                text_token_content.text = data.toString()
                progress_bar.visibility = View.GONE
                layout_buttons.visibility = View.VISIBLE
                text_token_content.visibility = View.VISIBLE
            }

            override fun onError(throwable: Throwable) {
                Log.e("CardFormViewSample", "failure creating token", throwable)
                text_token_content.text = throwable.toString()
                progress_bar.visibility = View.GONE
                layout_buttons.visibility = View.VISIBLE
                text_token_content.visibility = View.VISIBLE
            }
        })
    }

    private fun findCardFormFragment() {
        supportFragmentManager.let { manager ->
            val f = manager.findFragmentByTag(FRAGMENT_CARD_FORM)
            cardFormFragment = f as? PayjpCardFormFragment ?: Payjp.cardForm().newFragment()
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
}

enum class Theme(val id: Int) {
    AppCompat(R.style.AppTheme),
    MaterialComponent_Outline(R.style.Material_Outline),
    MaterialComponent_Filled(R.style.Material_Filled),
    MaterialComponent_Filled_Dense(R.style.Material_FilledDense),
}
