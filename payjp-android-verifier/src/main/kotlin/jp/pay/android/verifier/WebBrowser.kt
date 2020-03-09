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
package jp.pay.android.verifier

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import jp.pay.android.verifier.ui.PayjpWebActivity
import jp.pay.android.verifier.util.CustomTabsHelper

internal sealed class WebBrowser {

    abstract fun canResolveComponent(context: Context, uri: Uri): Boolean

    abstract fun createIntent(context: Context, uri: Uri, callbackUri: Uri): Intent

    internal object ChromeTab : WebBrowser() {
        override fun canResolveComponent(context: Context, uri: Uri): Boolean {
            return CustomTabsHelper.getPackageNameToUse(context)?.isNotEmpty() ?: false
        }

        override fun createIntent(context: Context, uri: Uri, callbackUri: Uri): Intent {
            return CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build().intent
                .setPackage(CustomTabsHelper.getPackageNameToUse(context))
                .setData(uri)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    }

    internal object AnyBrowsable : WebBrowser() {
        override fun canResolveComponent(context: Context, uri: Uri): Boolean {
            val intent = Intent(Intent.ACTION_VIEW, uri)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .addCategory(Intent.CATEGORY_BROWSABLE)

            val resolves = context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

            return resolves?.isNotEmpty() ?: false
        }

        override fun createIntent(context: Context, uri: Uri, callbackUri: Uri): Intent {
            // implicit intent
            return Intent(Intent.ACTION_VIEW, uri)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setPackage(null)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    }

    internal object InAppWeb : WebBrowser() {
        override fun canResolveComponent(context: Context, uri: Uri): Boolean {
            return true
        }

        override fun createIntent(context: Context, uri: Uri, callbackUri: Uri): Intent {
            return PayjpWebActivity.createIntent(
                    context = context,
                    startUri = uri,
                    callbackUri = callbackUri
                )
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }
}
