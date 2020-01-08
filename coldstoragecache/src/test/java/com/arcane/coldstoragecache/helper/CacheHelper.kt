package com.arcane.coldstoragecache.helper

import com.arcane.coldstorageannotaions.annotation.Refrigerate


class CacheHelper() {

    @Refrigerate
    fun longRunningFunctionThatReturnsAValue(string: String): String {
        val method =
            this::class.java.getMethod(
                "longRunningFunctionThatReturnsAValue",
                String::class.java
            )
        print(method.annotations)
        print(method.name)
        return "test"
    }
}