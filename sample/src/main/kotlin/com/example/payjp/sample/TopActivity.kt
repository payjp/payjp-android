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
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.payjp.sample.databinding.ActivityTopBinding
import com.example.payjp.sample.databinding.CardSampleBinding
import jp.pay.android.Payjp
import jp.pay.android.PayjpCardForm
import jp.pay.android.ui.PayjpCardFormResultCallback

typealias OnClickSample = (sample: TopActivity.Sample) -> Unit

class TopActivity : AppCompatActivity() {

    private val samples by lazy {
        listOf(
            Sample(
                "CardFormActivity (Card Display)",
                null,
                this::startCardFormCardFace
            ),
            Sample(
                "CardFormActivity (Multi Line)",
                null,
                this::startCardForm
            ),
            Sample(
                "CardFormView",
                Intent(this, CardFormViewSampleActivity::class.java)
            ),
            Sample(
                "CardFormView (Java)",
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

    private lateinit var binding: ActivityTopBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.let {
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = TopAdapter(this, samples) { sample ->
                sample.start(this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Payjp.cardForm().handleResult(
            data,
            PayjpCardFormResultCallback { result ->
                if (result.isSuccess()) {
                    val token = result.retrieveToken()
                    Log.i("handleCardFormResult", "token => $token")
                    Toast.makeText(this, "Token: $token", Toast.LENGTH_SHORT).show()
                }
            }
        )
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun startCardForm() {
        Payjp.cardForm().start(this)
    }

    private fun startCardFormCardFace() {
        Payjp.cardForm().start(this, face = PayjpCardForm.FACE_CARD_DISPLAY)
    }

    class TopAdapter(
        context: Context,
        private val list: List<Sample>,
        private val onClickSample: OnClickSample? = null
    ) : RecyclerView.Adapter<TopViewHolder>() {
        private val inflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopViewHolder {
            return TopViewHolder(
                CardSampleBinding.inflate(inflater, parent, false),
                onClickSample
            )
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: TopViewHolder, position: Int) {
            holder.sample = list[position]
        }
    }

    class TopViewHolder(
        private val binding: CardSampleBinding,
        onClick: OnClickSample? = null
    ) :
        RecyclerView.ViewHolder(binding.root) {

        var sample: Sample? = null
            set(value) {
                field = value
                binding.name.text = value?.name
            }

        init {
            onClick?.let { onClickNonNull ->
                binding.card.setOnClickListener {
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
