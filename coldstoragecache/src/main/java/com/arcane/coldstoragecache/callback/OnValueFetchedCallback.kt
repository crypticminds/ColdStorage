package com.arcane.coldstoragecache.callback

/**
 * The interface that should be implemented to
 * get the callback from the cache once the
 * result is available.
 * The cache fetches the results for a key from
 * a background thread and this callback can be used to
 * handle the result once it is available.
 *
 * @author Anurag.
 */
interface OnValueFetchedCallback<Output> {

    /**
     * The result is passed into this function
     * when it is available.
     */
    fun valueFetched(output: Output)
}