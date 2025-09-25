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

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.example.payjp.sample.extension.applyWindowInsets
import jp.pay.android.verifier.PayjpVerifier

class ThreeDSecureExampleActivity : AppCompatActivity() {

    private lateinit var completeMessageView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_three_d_secure_example)
        setSupportActionBar(findViewById(R.id.three_d_secure_sample_toolbar))

        val resourceIdInput = findViewById<EditText>(R.id.resource_id_input)
        val start3DSecureButton = findViewById<Button>(R.id.start_3d_secure_button)
        completeMessageView = findViewById(R.id.complete_message)

        // Initially hide the message view
        completeMessageView.visibility = View.GONE

        start3DSecureButton.setOnClickListener {
            val resourceId = resourceIdInput.text.toString()
            if (resourceId.isNotEmpty()) {
                completeMessageView.text = ""
                completeMessageView.setTextColor(Color.BLACK)
                completeMessageView.setBackgroundColor("#E8F5E9".toColorInt())
                completeMessageView.visibility = View.GONE
                PayjpVerifier.startThreeDSecureFlow(resourceId, this)
            } else {
                Toast.makeText(this, "Please enter a resource ID", Toast.LENGTH_SHORT).show()
            }
        }

        window.decorView.rootView.applyWindowInsets()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PayjpVerifier.handleThreeDSecureResult(requestCode) { result ->
            when {
                result.isSuccess() -> {
                    completeMessageView.text = "3Dセキュア認証が終了しました。\nこの結果をサーバーサイドに伝え、完了処理や結果のハンドリングを行なってください。\n後続処理の実装方法に関してはドキュメントをご参照ください。"
                    completeMessageView.visibility = View.VISIBLE
                }
                result.isCanceled() -> {
                    completeMessageView.text = "3Dセキュア認証がキャンセルされました"
                    completeMessageView.visibility = View.VISIBLE
                }
                else -> {
                    completeMessageView.text = "3Dセキュア認証が失敗しました"
                    completeMessageView.setBackgroundColor(Color.RED)
                    completeMessageView.setTextColor(Color.WHITE)
                    completeMessageView.visibility = View.VISIBLE
                }
            }
        }
    }
}
