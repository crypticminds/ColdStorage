package com.arcane.coldstoragecache.model

/**
 * The model that is used to store the cached data in the
 * shared preferences.
 *
 * @author Anurag
 */
data class CachedDataModel(

    /**
     * The object to be cached needs to be converted to a string
     * so that it can be stored in the shared preference.
     * The string can be converted to the required object using a
     * custom converter.
     * @see com.arcane.coldstoragecache.converter.IConverter
     * @see com.arcane.coldstoragecache.converter.impl.StringToBitmapConverter
     */
    val objectToCache: String,

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