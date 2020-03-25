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
package jp.pay.android.util

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> SavedStateHandle.delegate(
    key: String? = null,
    initialValue: T? = null
): ReadWriteProperty<Any, T> = SavedStateHandleDelegate(this, key, initialValue)

fun <T> SavedStateHandle.delegateLiveData(
    key: String? = null,
    initialValue: T? = null
): ReadWriteProperty<Any, MutableLiveData<T>> =
    SavedStateHandleLiveDataDelegate(this, key, initialValue)

private class SavedStateHandleLiveDataDelegate<T>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String? = null,
    private val initialValue: T? = null
) : ReadWriteProperty<Any, MutableLiveData<T>> {

    override fun getValue(thisRef: Any, property: KProperty<*>): MutableLiveData<T> {
        val savedStateKey = key ?: property.name
        return if (initialValue == null) {
            savedStateHandle.getLiveData<T>(savedStateKey)
        } else {
            savedStateHandle.getLiveData<T>(savedStateKey, initialValue)
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: MutableLiveData<T>) {
        val savedStateKey = key ?: property.name
        savedStateHandle.set(savedStateKey, value.value)
    }
}

private class SavedStateHandleDelegate<T>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String? = null,
    private val initialValue: T? = null
) : ReadWriteProperty<Any, T> {
    private object EMPTY

    private var value: Any? = EMPTY

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val savedStateKey = key ?: property.name
        if (value == EMPTY) {
            value = savedStateHandle.get(savedStateKey) ?: initialValue
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        val savedStateKey = key ?: property.name
        this.value = value
        savedStateHandle.set(savedStateKey, value)
    }
}
