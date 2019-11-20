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
package jp.pay.android.network

import android.os.Build
import android.util.Log
import java.security.KeyStore
import java.util.Arrays
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion

/**
 * Enable Tls ON Pre-Lollipop
 * @link https://github.com/square/okhttp/issues/2372
 */
internal object OkHttpTlsHelper {

    fun enableTls12OnPreLollipop(
        builder: OkHttpClient.Builder,
        debuggable: Boolean
    ): OkHttpClient.Builder {
        return builder.takeIf {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1
        }?.apply {
            try {
                val trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
                )
                trustManagerFactory.init(null as KeyStore?)
                val trustManagers = trustManagerFactory.trustManagers
                check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                    "Unexpected default trust managers:" + Arrays.toString(trustManagers)
                }
                val trustManager = trustManagers[0] as X509TrustManager

                val sslContext = SSLContext.getInstance("TLSv1.2")
                    .apply { init(null, null, null) }

                sslSocketFactory(Tls12SocketFactory(sslContext.socketFactory), trustManager)

                val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .build()

                connectionSpecs(
                    mutableListOf(
                        cs,
                        ConnectionSpec.COMPATIBLE_TLS,
                        ConnectionSpec.CLEARTEXT
                    )
                )
                if (debuggable) {
                    Log.v("payjp-android", "apply TLSv1.2 for pre-Lollipop")
                }
            } catch (e: Exception) {
                if (debuggable) {
                    Log.e("payjp-android", "error while setting TLS 1.2", e)
                }
            }
        } ?: builder
    }
}
