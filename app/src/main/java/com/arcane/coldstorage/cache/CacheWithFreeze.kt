package com.arcane.coldstorage.cache

import com.arcane.coldstorageannotation.Freeze

/**
 * Class to generate cache layer with freeze.
 */
@Freeze
class CacheWithFreeze {


    /**
     * Method used to generate a method using freeze.
     */
    fun cacheFunction1(name: String): String {
        return "name$name"

    }

    /**
     * Method used to generate a method using freeze.
     */
    fun cacheFunction2(age: Int): String {
        return "age$age"
    }
}