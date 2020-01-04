package com.arcane.coldstoragecache.converter

/**
 * The converter interface can be used to convert the
 * string into the required object.
 *
 * @author Anurag
 */
interface IConverter<Output> {

    /**
     * The result is passed into the method once
     * it is available once the background task is over.
     */
    fun convert(cachedString: String): Output
}