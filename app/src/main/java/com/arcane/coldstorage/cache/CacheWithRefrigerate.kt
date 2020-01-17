package com.arcane.coldstorage.cache

import com.arcane.coldstorageannotation.Refrigerate

class CacheWithRefrigerate {


    @Refrigerate(operation = "serviceA")
    fun makeCallToSomeService(): String {
        return "B"
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