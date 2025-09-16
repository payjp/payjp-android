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

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.pay.android.PayjpCardForm
import jp.pay.android.R
import jp.pay.android.databinding.PayjpSearchCountryCodeActivityBinding
import jp.pay.android.model.CountryCode
import jp.pay.android.ui.extension.applyWindowInsets

internal class PayjpSearchCountryCodeActivity : AppCompatActivity() {
    internal companion object {
        const val EXTRA_REGION = "EXTRA_REGION"
    }
    private lateinit var binding: PayjpSearchCountryCodeActivityBinding
    private lateinit var adapter: CountryCodesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PayjpSearchCountryCodeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.payjpSearchCountryCodeToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // recycler view
        adapter = CountryCodesAdapter { countryCode, _ ->
            setResult(
                RESULT_OK,
                Intent().apply {
                    putExtra(EXTRA_REGION, countryCode.region)
                }
            )
            finish()
        }
        binding.payjpCountryCodeRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.payjpCountryCodeRecyclerView.adapter = adapter
        adapter.setCountryCodes(PayjpCardForm.phoneNumberService().getAllCountryCodes(this))

        binding.root.applyWindowInsets()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_country_code_menu, menu)
        val searchView = menu?.findItem(R.id.payjp_search_country_code_toolbar_action_search)?.actionView as? SearchView
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                val allCodes = PayjpCardForm.phoneNumberService().getAllCountryCodes(this@PayjpSearchCountryCodeActivity)
                val filtered = allCodes.filter { it.searchDescription.contains(query ?: "", ignoreCase = true) }
                adapter.setCountryCodes(filtered)
                return true
            }
        })
        searchView?.isIconified = false
        return true
    }

    class CountryCodesAdapter(private val onClick: (countryCode: CountryCode, view: ViewHolder) -> Unit) :
        RecyclerView.Adapter<CountryCodesAdapter.ViewHolder>() {
        private val countryCodes = mutableListOf<CountryCode>()

        @SuppressLint("NotifyDataSetChanged")
        fun setCountryCodes(countryCodes: List<CountryCode>) {
            this.countryCodes.clear()
            this.countryCodes.addAll(countryCodes)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view, onClick)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.countryCode = countryCodes[position]
        }

        override fun getItemCount(): Int {
            return countryCodes.size
        }

        class ViewHolder(itemView: View, onClick: (countryCode: CountryCode, view: ViewHolder) -> Unit) : RecyclerView.ViewHolder(itemView) {
            var countryCode: CountryCode? = null
                set(value) {
                    field = value
                    itemView.findViewById<TextView>(android.R.id.text1).text = value?.searchDescription
                }

            init {
                itemView.setOnClickListener {
                    countryCode?.let { onClick(it, this) }
                }
            }
        }
    }
}
