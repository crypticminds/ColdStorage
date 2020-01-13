package com.arcane.coldstoragecache.model

/**
 * The model that is used to store the cached data in the
 * shared preferences.
 *
 * @author Anurag
 */
data class ColdStorageModel(

    /**
     * The object to be cached.
     */
    val objectToCache: Any,

    /**
     * The time when the object was last updated.
     */
    val timestamp: Long,

    /**
     * An optional value that will determine how long
     * this object needs to be cached before considered stale.
     */
    val timeToLive: Long?
)