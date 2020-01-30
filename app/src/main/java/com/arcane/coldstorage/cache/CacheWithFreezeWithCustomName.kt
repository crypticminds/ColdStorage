package com.arcane.coldstorage.cache

import com.arcane.coldstorageannotation.CacheKey
import com.arcane.coldstorageannotation.Freeze

/**
 * Example of custom class name.
 */
@Freeze(generatedClassName = "MyClassName")
class CacheWithFreezeWithCustomName {


    fun method1() {

    }


    fun method2(@CacheKey key1: String, key2: String) {

    }
}