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

import com.facebook.flipper.android.AndroidFlipperClient;
import com.facebook.flipper.android.utils.FlipperUtils;
import com.facebook.flipper.core.FlipperClient;
import com.facebook.flipper.plugins.inspector.DescriptorMapping;
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin;
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor;
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin;
import com.facebook.soloader.SoLoader;

import jp.pay.android.Payjp;
import jp.pay.android.PayjpConfiguration;
import jp.pay.android.cardio.PayjpCardScannerPlugin;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class SampleApplication extends Application {

    static final String BACKEND_URL = ""; // TODO: REPLACE WITH YOUR ENDPOINT URL

    @Override
    public void onCreate() {
        super.onCreate();
        SoLoader.init(this, false);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .addNetworkInterceptor(loggingInterceptor);
        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            final FlipperClient client = AndroidFlipperClient.getInstance(this);
            client.addPlugin(new InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()));
            final NetworkFlipperPlugin networkFlipperPlugin = new NetworkFlipperPlugin();
            okHttpClientBuilder.addNetworkInterceptor(new FlipperOkhttpInterceptor(networkFlipperPlugin));
            client.addPlugin(networkFlipperPlugin);
            client.start();
        }

        SampleSendTokenHandler sendTokenHandler = new SampleSendTokenHandler(
                BACKEND_URL, okHttpClientBuilder.build());

        Payjp.init(new PayjpConfiguration.Builder("pk_test_0383a1b8f91e8a6e3ea0e2a9")
                .setDebugEnabled(BuildConfig.DEBUG)
                .setCardScannerPlugin(PayjpCardScannerPlugin.INSTANCE)
                .setTokenBackgroundHandler(sendTokenHandler)
                .build());
    }
}