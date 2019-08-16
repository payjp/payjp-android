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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import jp.pay.android.PayjpToken;
import jp.pay.android.Task;
import jp.pay.android.model.Token;
import jp.pay.android.ui.widget.PayjpCardFormFragment;
import jp.pay.android.ui.widget.PayjpCardFormView;

public class CardFormViewSampleJavaActivity extends AppCompatActivity
        implements PayjpCardFormView.OnValidateInputListener {

    private static final String FRAGMENT_CARD_FORM = "FRAGMENT_CARD_FORM";

    private @Nullable Task<Token> createToken;
    private @Nullable Task<Token> getToken;
    private PayjpCardFormFragment cardFormFragment;
    private ProgressBar progressBar;
    private TextView textTokenId;
    private TextView textTokenContent;
    private Button buttonCreateToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_form_view_sample);
        progressBar = findViewById(R.id.progress_bar);
        textTokenId = findViewById(R.id.text_token_id);
        textTokenContent = findViewById(R.id.text_token_content);
        buttonCreateToken = findViewById(R.id.button_create_token);
        findCardFormFragment();
        buttonCreateToken.setOnClickListener(view -> {
            if (cardFormFragment.isValid()) {
                createToken();
            }
        });
        findViewById(R.id.button_create_token_with_validate).setOnClickListener(view -> {
            if (cardFormFragment.validateCardForm()) {
                createToken();
            }
        });
        findViewById(R.id.button_get_token).setOnClickListener(view ->
                getToken(textTokenId.getText().toString()));
        Switch switchCardHolderName = findViewById(R.id.switch_card_holder_name);
        switchCardHolderName.setOnCheckedChangeListener((view, isChecked) ->
                cardFormFragment.setCardHolderNameInputEnabled(isChecked));
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
        buttonCreateToken.setEnabled(isValid);
    }

    private void findCardFormFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentByTag(FRAGMENT_CARD_FORM);
        if (f instanceof PayjpCardFormFragment) {
            cardFormFragment = (PayjpCardFormFragment) f;
        } else {
            cardFormFragment = PayjpCardFormFragment.newInstance(false, null);
        }
        if (!cardFormFragment.isAdded()) {
            fm.beginTransaction()
                    .replace(R.id.card_form_view, cardFormFragment, FRAGMENT_CARD_FORM)
                    .commit();
        }
    }

    private void createToken() {
        progressBar.setVisibility(View.VISIBLE);
        textTokenContent.setVisibility(View.INVISIBLE);
        createToken = cardFormFragment.createToken();
        createToken.enqueue(new Task.Callback<Token>() {
            @Override
            public void onSuccess(Token data) {
                Log.i("GenerateTokenSampleJava", "token => " + data);
                textTokenId.setText(data.getId());
                progressBar.setVisibility(View.GONE);
                textTokenContent.setText("The token has created.");
                textTokenContent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                Log.e("GenerateTokenSampleJava", "failure creating token", throwable);
                progressBar.setVisibility(View.GONE);
                textTokenContent.setVisibility(View.VISIBLE);
                textTokenContent.setText(throwable.toString());
            }
        });
    }

    private void getToken(@NonNull String id) {
        progressBar.setVisibility(View.VISIBLE);
        textTokenContent.setVisibility(View.INVISIBLE);
        getToken = PayjpToken.getInstance().getToken(id);
        getToken.enqueue(new Task.Callback<Token>() {
            @Override
            public void onSuccess(Token data) {
                Log.i("GenerateTokenSampleJava", "token => " + data);
                textTokenContent.setText(data.toString());
                progressBar.setVisibility(View.GONE);
                textTokenContent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                Log.e("GenerateTokenSampleJava", "failure creating token", throwable);
                progressBar.setVisibility(View.GONE);
                textTokenContent.setVisibility(View.VISIBLE);
                textTokenContent.setText(throwable.toString());
            }
        });
    }
}
