package com.ing.mint.util

object MapUtil {
    // getOrDefault exists from api lvl 24 onwards, we are at 21. Don't bump the project dependency
    // for just getOrDefault() method on a map.
    fun <K, V> Map<K, V>.getOrDefaultExt(key: K, defaultValue: V): V {
        val value = this[key]
        if (value != null) {
            return value
        } else {
            return defaultValue
        }
    }
}
