package com.arcane.coldstorage.cache

import com.arcane.coldstorageannotation.CacheKey
import com.arcane.coldstorageannotation.Refrigerate

/**
 * Class showing the usage of refrigerate annotation.
 */
class CacheWithRefrigerate {

    /**
     * Method to test refrigerate with no keys.
     */
    @Refrigerate(operation = "serviceA")
    fun makeCallToSomeService(): String {
        return "B"
    }

    /**
     * Method to test refrigerate with multiple keys.
     */
    @Refrigerate(operation = "serviceB")
    fun makeCallToSomeService(@CacheKey key: String,
                              @CacheKey abcd: String,
                              efgh : String,
                              ignorethis : String): String {
        return "C"

    }

    /**
     * This will throw an error since the function is private
    @Refrigerate(operation = "failure")
    private fun failureFunction(): String {
    return "C"
    }


    This will throw an error since there is not return type
    @Refrigerate(operation = "failure2")
    fun failureFunction2() {

    }
     **/
}