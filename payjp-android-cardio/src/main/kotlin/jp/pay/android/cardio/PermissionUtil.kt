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
package jp.pay.android.cardio

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment

internal object PermissionUtil {

    fun hasSelfPermissions(context: Context, permissions: Array<String>): Boolean =
        permissions.all {
            try {
                PermissionChecker.checkSelfPermission(
                    context,
                    it
                ) == PermissionChecker.PERMISSION_GRANTED
            } catch (e: RuntimeException) {
                false
            }
        }

    fun shouldShowRequestPermissionRationale(
        activity: Activity,
        permissions: Array<String>
    ): Boolean =
        permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }

    fun shouldShowRequestPermissionRationale(
        fragment: Fragment,
        permissions: Array<String>
    ): Boolean =
        permissions.any {
            fragment.shouldShowRequestPermissionRationale(it)
        }

    fun verifyPermissionResults(grantResults: IntArray): Boolean =
        grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
}
