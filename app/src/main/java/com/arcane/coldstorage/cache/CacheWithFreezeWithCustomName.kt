package com.arcane.coldstorage.cache

import com.arcane.coldstorageannotation.CacheKey
import com.arcane.coldstorageannotation.Freeze

/**
 * Example of custom class name.
 */
@Freeze(generatedClassName = "MyClassName")
class CacheWithFreezeWithCustomName {

    /**
     * Method to test freeze with no key.
     */
    fun method1(): String {
        return "B"
    }

    /**
     * Method to test freeze with one key.
     */
    fun method2(@CacheKey key1: String, key2: String): String {
        return "C"
    }
}