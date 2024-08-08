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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.payjp.sample.databinding.ActivityCardFormViewSampleBinding;

import jp.pay.android.Payjp;
import jp.pay.android.Task;
import jp.pay.android.model.Token;
import jp.pay.android.ui.widget.PayjpCardFormFragment;
import jp.pay.android.ui.widget.PayjpCardFormView;

public class CardFormViewSampleJavaActivity extends AppCompatActivity
        implements PayjpCardFormView.OnValidateInputListener {

    private static final String FRAGMENT_CARD_FORM = "FRAGMENT_CARD_FORM";

    private @Nullable
    Task<Token> createToken;
    private @Nullable
    Task<Token> getToken;
    private PayjpCardFormFragment cardFormFragment;
    private ActivityCardFormViewSampleBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCardFormViewSampleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        findCardFormFragment();
        binding.buttonCreateToken.setOnClickListener(view -> {
            if (cardFormFragment.isValid()) {
                createToken();
            }
        });
        binding.buttonCreateTokenWithValidate.setOnClickListener(view -> {
            if (cardFormFragment.validateCardForm()) {
                createToken();
            }
        });
        binding.buttonGetToken.setOnClickListener(view ->
                getToken(binding.textTokenId.getText().toString()));
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

    @Override
    public void onValidateInput(@NonNull PayjpCardFormView view, boolean isValid) {
        binding.buttonCreateToken.setEnabled(isValid);
    }

    private void findCardFormFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentByTag(FRAGMENT_CARD_FORM);
        if (f instanceof PayjpCardFormFragment) {
            cardFormFragment = (PayjpCardFormFragment) f;
        } else {
            cardFormFragment = Payjp.cardForm().newFragment();
        }
        if (!cardFormFragment.isAdded()) {
            fm.beginTransaction()
                    .replace(R.id.card_form_view, cardFormFragment, FRAGMENT_CARD_FORM)
                    .commit();
        }
    }

    private void createToken() {
        binding.layoutButtons.setVisibility(View.INVISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.textTokenContent.setVisibility(View.INVISIBLE);
        createToken = cardFormFragment.createToken();
        createToken.enqueue(new Task.Callback<Token>() {
            @Override
            public void onSuccess(Token data) {
                Log.i("GenerateTokenSampleJava", "token => " + data);
                binding.textTokenId.setText(data.getId());
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutButtons.setVisibility(View.VISIBLE);
                binding.textTokenContent.setText("The token has created.");
                binding.textTokenContent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                Log.e("GenerateTokenSampleJava", "failure creating token", throwable);
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutButtons.setVisibility(View.VISIBLE);
                binding.textTokenContent.setVisibility(View.VISIBLE);
                binding.textTokenContent.setText(throwable.toString());
            }
        });
    }

    private void getToken(@NonNull String id) {
        binding.layoutButtons.setVisibility(View.INVISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.textTokenContent.setVisibility(View.INVISIBLE);
        getToken = Payjp.token().getToken(id);
        getToken.enqueue(new Task.Callback<Token>() {
            @Override
            public void onSuccess(Token data) {
                Log.i("GenerateTokenSampleJava", "token => " + data);
                binding.textTokenContent.setText(data.toString());
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutButtons.setVisibility(View.VISIBLE);
                binding.textTokenContent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                Log.e("GenerateTokenSampleJava", "failure creating token", throwable);
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutButtons.setVisibility(View.VISIBLE);
                binding.textTokenContent.setVisibility(View.VISIBLE);
                binding.textTokenContent.setText(throwable.toString());
            }
        });
    }
}
