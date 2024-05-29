package com.example.chessonline.data.socket

import kotlin.reflect.KProperty


class ResettableLazy<T>(private val initializer: () -> T) {
    @Volatile private var _value: Any? = null

    val value: T
        get() {
            val v1 = _value
            if (v1 != null) {
                @Suppress("UNCHECKED_CAST")
                return v1 as T
            }

            return synchronized(this) {
                val v2 = _value
                if (v2 != null) {
                    @Suppress("UNCHECKED_CAST")
                    v2 as T
                } else {
                    val typedValue = initializer()
                    _value = typedValue
                    typedValue
                }
            }
        }

    fun reset() {
        _value = null
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}

