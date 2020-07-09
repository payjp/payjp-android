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

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.payjp.sample.databinding.ActivityGenerateTokenSampleBinding
import jp.pay.android.Payjp
import jp.pay.android.Task
import jp.pay.android.model.Token

class GenerateTokenSampleActivity : AppCompatActivity() {

    private var createToken: Task<Token>? = null
    private var getToken: Task<Token>? = null
    private lateinit var binding: ActivityGenerateTokenSampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateTokenSampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonCreateToken.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.textTokenContent.visibility = View.INVISIBLE
            // create token
            val number = binding.textCardNumber.text.toString()
            val cvc = binding.textCardCvc.text.toString()
            val expMonth = binding.textCardExpMonth.text.toString()
            val expYear = binding.textCardExpYear.text.toString()
            val name = binding.textCardName.text.toString()

            createToken = Payjp.token().createToken(
                number = number, cvc = cvc,
                expMonth = expMonth, expYear = expYear, name = name
            )
            createToken?.enqueue(object : Task.Callback<Token> {
                override fun onSuccess(data: Token) {
                    Log.i("GenerateTokenSample", "token => $data")
                    binding.textTokenId.setText(data.id)
                    binding.textTokenContent.text = "The token has created."
                    binding.progressBar.visibility = View.GONE
                    binding.textTokenContent.visibility = View.VISIBLE
                }

                override fun onError(throwable: Throwable) {
                    Log.e("GenerateTokenSample", "failure creating token", throwable)
                    binding.textTokenContent.text = throwable.toString()
                    binding.progressBar.visibility = View.GONE
                    binding.textTokenContent.visibility = View.VISIBLE
                }
            })
        }

        binding.buttonGetToken.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.textTokenContent.visibility = View.INVISIBLE

            // get token
            getToken = Payjp.token().getToken(binding.textTokenId.text.toString())
            getToken?.enqueue(object : Task.Callback<Token> {
                override fun onSuccess(data: Token) {
                    Log.i("GenerateTokenSample", "token => $data")
                    binding.textTokenContent.text = data.toString()
                    binding.progressBar.visibility = View.GONE
                    binding.textTokenContent.visibility = View.VISIBLE
                }

                override fun onError(throwable: Throwable) {
                    Log.e("GenerateTokenSample", "failure creating token", throwable)
                    binding.textTokenContent.text = throwable.toString()
                    binding.progressBar.visibility = View.GONE
                    binding.textTokenContent.visibility = View.VISIBLE
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        createToken?.cancel()
        getToken?.cancel()
    }
}
