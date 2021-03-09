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
package jp.pay.android.ui.extension

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

internal fun <T1, T2, T3, S> combineLatest(
    source1: LiveData<T1>,
    source2: LiveData<T2>,
    source3: LiveData<T3>,
    func: (T1, T2, T3) -> S
): LiveData<S> = MediatorLiveData<S>().also { result ->
    var source1EmitOnce = false
    var source2EmitOnce = false
    var source3EmitOnce = false
    val merge = {
        if (source1EmitOnce && source2EmitOnce && source3EmitOnce) {
            result.value = func.invoke(source1.value!!, source2.value!!, source3.value!!)
        }
    }
    result.addSource(source1) {
        source1EmitOnce = true
        merge()
    }
    result.addSource(source2) {
        source2EmitOnce = true
        merge()
    }
    result.addSource(source3) {
        source3EmitOnce = true
        merge()
    }
}

internal fun <T> LiveData<T>.startWith(startingValue: T?): LiveData<T> =
    MediatorLiveData<T>().also { result ->
        var initial: LiveData<T>? = MutableLiveData(startingValue)
        result.addSource(this) {
            if (null != initial) {
                result.removeSource(initial!!)
                initial = null
            }
            result.value = it
        }
        result.addSource(initial!!) {
            result.value = it
            result.removeSource(initial!!)
            initial = null
        }
    }
