package com.arcane.coldstorage.cache

import com.arcane.coldstorageannotation.Freeze

@Freeze
class CacheWithFreeze {


    fun cacheFunction1(name: String): String {
        return "name$name"

    }

    fun cacheFunction2(age: Int): String {
        return "age$age"
    }
}