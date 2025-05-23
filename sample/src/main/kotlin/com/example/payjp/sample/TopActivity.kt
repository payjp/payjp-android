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
package com.example.payjp.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.payjp.sample.databinding.ActivityTopBinding
import com.example.payjp.sample.databinding.CardSampleBinding
import jp.pay.android.Payjp
import jp.pay.android.PayjpCardForm
import jp.pay.android.model.ExtraAttribute

typealias OnClickSample = (sample: TopActivity.Sample) -> Unit

class TopActivity : AppCompatActivity() {

    private val samples by lazy {
        listOf(
            Sample(
                "CardFormActivity (FACE_CARD_DISPLAY)",
                null,
                this::startCardFormCardFace
            ),
            Sample(
                "CardFormActivity (FACE_MULTI_LINE)",
                null,
                this::startCardForm
            ),
            Sample(
                "CardFormView",
                Intent(this, CardFormViewSampleActivity::class.java)
            ),
            Sample(
                "CardFormView (Coroutine)",
                Intent(this, CoroutineSampleActivity::class.java)
            ),
            Sample(
                "ThreeDSecureExampleActivity",
                Intent(this, ThreeDSecureExampleActivity::class.java),
                headerText = "支払い時の3Dセキュア、または顧客カードの3Dセキュア"
            )
        )
    }
    private val attributesOption: Array<Pair<String, Array<ExtraAttribute<*>>>> = arrayOf(
        "email and phone" to arrayOf(
            ExtraAttribute.Email(),
            ExtraAttribute.Phone(region = "JP"),
        ),
        "email only" to arrayOf(
            ExtraAttribute.Email()
        ),
        "phone only" to arrayOf(
            ExtraAttribute.Phone(region = "JP")
        ),
        "email only (preset)" to arrayOf(
            ExtraAttribute.Email("test@example.com")
        ),
        "phone only (preset)" to arrayOf(
            ExtraAttribute.Phone("JP", "09012345678")
        ),
        "none" to emptyArray()
    )

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
        Payjp.cardForm().handleResult(data) { result ->
            if (result.isSuccess()) {
                val token = result.retrieveToken()
                Log.i("handleCardFormResult", "token => $token")
                Toast.makeText(this, "Token: $token", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun startCardForm(face: Int = PayjpCardForm.FACE_MULTI_LINE) {
        // show selectable alert dialog
        val items = attributesOption.map { it.first }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Select Extra Attributes")
            .setItems(items) { _, which ->
                val attributes = attributesOption[which].second
                // You can pass extra attributes to start card form.
                // attributes are mainly used for 3-D Secure, please see the document.
                // https://help.pay.jp/ja/articles/9556161
                Payjp.cardForm().start(
                    activity = this,
                    face = face,
                    extraAttributes = attributes,
                    useThreeDSecure = true,
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startCardFormCardFace() = startCardForm(face = PayjpCardForm.FACE_CARD_DISPLAY)

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

                if (value?.headerText != null) {
                    binding.header.text = value.headerText
                    binding.header.visibility = android.view.View.VISIBLE
                } else {
                    binding.header.visibility = android.view.View.GONE
                }
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

    data class Sample(
        val name: String,
        val intent: Intent?,
        val startable: (() -> Unit)? = null,
        val headerText: String? = null
    ) {
        fun start(activity: AppCompatActivity) {
            startable?.invoke() ?: intent?.let { activity.startActivity(it) }
        }
    }
}
