/*
 *
 * Copyright (c) 2017 PAY.JP (https://pay.jp)
 * Copyright (c) 2017 BASE, Inc. (https://binc.jp/)
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

package com.example.payjp.samplejava;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import jp.pay.android.PayjpToken;
import jp.pay.android.Task;
import jp.pay.android.model.CardBrand;
import jp.pay.android.model.Token;

public class MainActivity extends AppCompatActivity {

    private Task<Token> createToken = null;
    private Task<Token> getToken = null;

    private ProgressBar progressBar;
    private TextView textTokenId;
    private TextView textTokenContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress_bar);
        textTokenId = findViewById(R.id.text_token_id);
        textTokenContent = findViewById(R.id.text_token_content);

        findViewById(R.id.button_create_token).setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            textTokenContent.setVisibility(View.INVISIBLE);

            createToken = PayjpToken.getInstance().createToken("4242424242424242",
                    "123", "02", "2020");
            createToken.enqueue(new Task.Callback<Token>() {
                @Override
                public void onSuccess(Token data) {
                    Log.i("MainActivity", "token => " + data);
                    textTokenId.setText(data.getId());
                    progressBar.setVisibility(View.GONE);
                    textTokenContent.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(@NotNull Throwable throwable) {
                    Log.e("MainActivity", "failure creating token", throwable);
                    progressBar.setVisibility(View.GONE);
                    textTokenContent.setVisibility(View.VISIBLE);
                    textTokenContent.setText(throwable.toString());
                }
            });
        });

        findViewById(R.id.button_get_token).setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            textTokenContent.setVisibility(View.INVISIBLE);

            getToken = PayjpToken.getInstance().getToken(textTokenId.getText().toString());
            getToken.enqueue(new Task.Callback<Token>() {
                @Override
                public void onSuccess(Token data) {
                    Log.i("MainActivity", "token => " + data);
                    textTokenContent.setText(data.toString());
                    progressBar.setVisibility(View.GONE);
                    textTokenContent.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(@NotNull Throwable throwable) {
                    Log.e("MainActivity", "failure creating token", throwable);
                    progressBar.setVisibility(View.GONE);
                    textTokenContent.setVisibility(View.VISIBLE);
                    textTokenContent.setText(throwable.toString());
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (createToken != null) {
            createToken.cancel();
        }
        if (getToken != null) {
            getToken.cancel();
        }
    }
}
