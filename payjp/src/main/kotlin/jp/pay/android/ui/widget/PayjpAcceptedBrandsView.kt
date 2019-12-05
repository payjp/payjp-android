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
package jp.pay.android.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.pay.android.R
import jp.pay.android.model.CardBrand
import jp.pay.android.ui.extension.logoResourceId

/**
 * List of [CardBrand] logo.
 * You can use this to display card acceptance.
 * It will only show logos which provided by [PayjpAcceptedBrandsView.setAcceptedBrands].
 */
class PayjpAcceptedBrandsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private val acceptedBrandsAdapter: AcceptedBrandAdapter

    init {
        acceptedBrandsAdapter = AcceptedBrandAdapter(context)
        adapter = acceptedBrandsAdapter
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
    }

    /**
     * Return brand list.
     */
    fun getAcceptedBrands(): List<CardBrand> = acceptedBrandsAdapter.list

    /**
     * Set brand list.
     *
     * @param brands brand list.
     */
    fun setAcceptedBrands(brands: List<CardBrand>) {
        acceptedBrandsAdapter.list = brands
        acceptedBrandsAdapter.notifyDataSetChanged()
    }

    internal class AcceptedBrandAdapter(context: Context) : RecyclerView.Adapter<AcceptedBrandViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)
        var list: List<CardBrand> = listOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcceptedBrandViewHolder {
            return AcceptedBrandViewHolder(inflater, parent)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: AcceptedBrandViewHolder, position: Int) {
            val brand = list[position]
            (holder.itemView as ImageView).run {
                setImageResource(brand.logoResourceId)
                contentDescription = brand.rawValue
            }
        }
    }

    internal class AcceptedBrandViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(inflater.inflate(R.layout.payjp_card_accepted_brand_item_view, parent, false))
}
