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

package com.example.payjp.sample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.payjp.sample.databinding.ActivityGenerateTokenSampleBinding;

import jp.pay.android.Payjp;
import jp.pay.android.PayjpTokenParam;
import jp.pay.android.Task;
import jp.pay.android.model.Token;

public class GenerateTokenSampleJavaActivity extends AppCompatActivity {

    private Task<Token> createToken = null;
    private Task<Token> getToken = null;
    private ActivityGenerateTokenSampleBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGenerateTokenSampleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonCreateToken.setOnClickListener(view -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.textTokenContent.setVisibility(View.INVISIBLE);

            final String number = binding.textCardNumber.getText().toString();
            final String cvc = binding.textCardCvc.getText().toString();
            final String expMonth = binding.textCardExpMonth.getText().toString();
            final String expYear = binding.textCardExpYear.getText().toString();
            final String name = binding.textCardName.getText().toString();

            createToken = Payjp.token().createToken(
                    new PayjpTokenParam.Builder(number, cvc, expMonth, expYear)
                            .name(name)
                            .build()
            );
            createToken.enqueue(new Task.Callback<Token>() {
                @Override
                public void onSuccess(Token data) {
                    Log.i("GenerateTokenSampleJava", "token => " + data);
                    binding.textTokenId.setText(data.getId());
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textTokenContent.setText("The token has created.");
                    binding.textTokenContent.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(@NonNull Throwable throwable) {
                    Log.e("GenerateTokenSampleJava", "failure creating token", throwable);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textTokenContent.setVisibility(View.VISIBLE);
                    binding.textTokenContent.setText(throwable.toString());
                }
            });
        });

        binding.buttonGetToken.setOnClickListener(view -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.textTokenContent.setVisibility(View.INVISIBLE);

            getToken = Payjp.token().getToken(binding.textTokenId.getText().toString());
            getToken.enqueue(new Task.Callback<Token>() {
                @Override
                public void onSuccess(Token data) {
                    Log.i("GenerateTokenSampleJava", "token => " + data);
                    binding.textTokenContent.setText(data.toString());
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textTokenContent.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(@NonNull Throwable throwable) {
                    Log.e("GenerateTokenSampleJava", "failure creating token", throwable);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textTokenContent.setVisibility(View.VISIBLE);
                    binding.textTokenContent.setText(throwable.toString());
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
