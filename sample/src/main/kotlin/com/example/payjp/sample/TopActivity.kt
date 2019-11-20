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
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.pay.android.Payjp
import jp.pay.android.ui.PayjpCardFormResultCallback
import kotlinx.android.synthetic.main.activity_top.recycler_view

typealias OnClickSample = (sample: TopActivity.Sample) -> Unit

class TopActivity : AppCompatActivity() {

    private val samples by lazy {
        listOf(
            Sample("CardFormActivity Example",
                null, this::startCardForm),
            Sample(
                "CardFormView Example",
                Intent(this, CardFormViewSampleActivity::class.java)
            ),
            Sample(
                "CardFormView Example (Java)",
                Intent(this, CardFormViewSampleJavaActivity::class.java)
            ),
            Sample(
                "Generate Card Tokens Manually",
                Intent(this, GenerateTokenSampleActivity::class.java)
            ),
            Sample(
                "Generate Card Tokens Manually (Java)",
                Intent(this, GenerateTokenSampleJavaActivity::class.java)
            ),
            Sample(
                "Coroutine Extension Example",
                Intent(this, CoroutineSampleActivity::class.java)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = TopAdapter(this, samples) { sample ->
            sample.start(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Payjp.handleCardFormResult(data, PayjpCardFormResultCallback { result ->
            if (result.isSuccess()) {
                Toast.makeText(this, "Token: ${result.retrieveToken()}", Toast.LENGTH_SHORT).show()
            }
        })
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun startCardForm() {
        Payjp.startCardForm(this)
    }

    class TopAdapter(
        context: Context,
        private val list: List<Sample>,
        private val onClickSample: OnClickSample? = null
    ) : RecyclerView.Adapter<TopViewHolder>() {
        private val inflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopViewHolder {
            return TopViewHolder(inflater, parent, onClickSample)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: TopViewHolder, position: Int) {
            holder.sample = list[position]
        }
    }

    class TopViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        onClick: OnClickSample? = null
    ) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.card_sample, parent, false)) {

        private val nameView by lazy { itemView.findViewById<TextView>(R.id.name) }
        private val cardView by lazy { itemView.findViewById<CardView>(R.id.card) }

        var sample: Sample? = null
            set(value) {
                field = value
                nameView.text = value?.name
            }

        init {
            onClick?.let { onClickNonNull ->
                cardView.setOnClickListener {
                    sample?.let {
                        onClickNonNull(it)
                    }
                }
            }
        }
    }

    data class Sample(val name: String, val intent: Intent?, val startable: (() -> Unit)? = null) {
        fun start(activity: AppCompatActivity) {
            startable?.invoke() ?: intent?.let { activity.startActivity(it) }
        }
    }
}
