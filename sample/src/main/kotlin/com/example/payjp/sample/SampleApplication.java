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

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import jp.pay.android.Payjp;
import jp.pay.android.PayjpConfiguration;
import jp.pay.android.cardio.PayjpCardScannerPlugin;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class SampleApplication extends Application {

    /**
     * TODO: REPLACE WITH YOUR ENDPOINT URL
     *
     * You can set up sample server api with following repo.
     * https://github.com/payjp/example-tokenize-backend
     *
     * (If you deploy the sample server app to Heroku, the url will be like
     * `https://[your_app_name].herokuapp.com/`)
     *
     * See the link above for more details.
     */
    static final String BACKEND_URL = "";

    @Override
    public void onCreate() {
        super.onCreate();

        // For Modern TLS
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            try {
                ProviderInstaller.installIfNeeded(this);
            } catch (GooglePlayServicesRepairableException e) {
                Log.e("payjp-android", "error ssl setup", e);
            } catch (GooglePlayServicesNotAvailableException e) {
                Log.e("payjp-android", "error ssl setup", e);
            }
        }

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(loggingInterceptor)
                .build();

        SampleSendTokenHandler sendTokenHandler = new SampleSendTokenHandler(
                BACKEND_URL, okHttpClient);

        Payjp.init(new PayjpConfiguration.Builder("pk_test_0383a1b8f91e8a6e3ea0e2a9")
                .setDebugEnabled(BuildConfig.DEBUG)
                .setCardScannerPlugin(PayjpCardScannerPlugin.INSTANCE)
                .setTokenBackgroundHandler(sendTokenHandler)
                .setTdsRedirectName("mobileapp")
                .build());


    }
}