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
package jp.pay.android.plugin

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * Card scan plugin interface
 */
interface CardScannerPlugin {

    /**
     * start scan activity
     *
     * @param activity called activity
     */
    fun startScanActivity(activity: Activity)

    /**
     * start scan activity from fragment
     *
     * @param fragment called fragment
     */
    fun startScanActivity(fragment: Fragment)

    /**
     * pass result from Activity or Fragment's onActivityResult to listener.
     *
     * @param requestCode requestCode of onActivityResult
     * @param resultCode resultCode of onActivityResult
     * @param data data of onActivityResult
     * @param listener listener
     */
    fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        listener: CardScanOnResultListener?
    ): Boolean

    /**
     * Delegate [Activity.onRequestPermissionsResult]
     *
     * @param activity activity
     * @param requestCode code requested
     * @param grantResults results
     * @param delegate if user select "never ask again", delegate call
     *   [CardScannerPermissionDelegate.onNeverAskAgainCardScannerPermission]
     */
    fun onRequestPermissionResult(
        activity: Activity,
        requestCode: Int,
        grantResults: IntArray,
        delegate: CardScannerPermissionDelegate?
    )

    /**
     * Delegate [Fragment.onRequestPermissionsResult]
     *
     * @param fragment fragment
     * @param requestCode code requested
     * @param grantResults results
     * @param delegate if user select "never ask again", delegate call
     *   [CardScannerPermissionDelegate.onNeverAskAgainCardScannerPermission]
     */
    fun onRequestPermissionResult(
        fragment: Fragment,
        requestCode: Int,
        grantResults: IntArray,
        delegate: CardScannerPermissionDelegate?
    )

    /**
     * Communicates the result of scan.
     *
     */
    interface CardScanOnResultListener {

        /**
         * It will be called after scan success.
         * It will not be called when canceled.
         *
         * @param cardNumber card number scanned
         */
        fun onScanResult(cardNumber: String?)
    }

    /**
     * Delegate interface for requesting permission of card scanner.
     *
     */
    interface CardScannerPermissionDelegate {

        /**
         * Call if a permission request was disallowed with "Never ask again".
         *
         */
        fun onNeverAskAgainCardScannerPermission()
    }
}
