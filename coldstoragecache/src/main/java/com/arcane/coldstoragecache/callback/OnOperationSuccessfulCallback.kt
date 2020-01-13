package com.arcane.coldstoragecache.callback

/**
 * The interface that should be implemented while getting the
 * result from the background thread that is used to fetch or refresh
 * the result to the main thread.
 * This method should be implemented if higher order functions
 * are used for caching data.
 * The callback returns the value that is fetched for each operation
 * defined in the cache.
 * For details on operation see the refrigerate annotation.
 * @see com.arcane.coldstoragecache.annotations.Refrigerate
 *
 * @author Anurag
 */
interface OnOperationSuccessfulCallback<Output> {

    /**
     * The result is passed into this function
     * when it is available.
     */
    fun onSuccess(output: Output?, operation: String)
}