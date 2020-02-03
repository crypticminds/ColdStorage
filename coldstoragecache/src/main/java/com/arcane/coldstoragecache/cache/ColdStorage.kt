package com.arcane.coldstoragecache.cache

import android.util.Log
import com.arcane.coldstoragecache.model.ColdStorageModel
import java.util.concurrent.ConcurrentHashMap

/**
 * The caching logic used by the generated class if annotations
 * are used to handle caching.
 *
 * @author Anurag
 */
class ColdStorage {

    companion object {

        /**
         * The cache map where the cached data is stored.
         *
         * Unlike the Cache class , this cache map can store
         * any object.
         */
        private val cacheMap: ConcurrentHashMap<String, ColdStorageModel> = ConcurrentHashMap()

        /**
         * TAG for logging.
         */
        private const val TAG = "COLD_STORAGE"

        /**
         * Method to get a value from the cache.
         *
         * @param key The key for which the value needs to be fetched.
         */
        fun get(key: String): Any? {
            if (cacheMap.containsKey(key)) {
                val coldStorageModel = cacheMap[key] ?: return null
                return if (Cache.isDataStale(
                        coldStorageModel.timeToLive,
                        coldStorageModel.timestamp
                    )
                ) {
                    Log.i(TAG, "data is stale")
                    null
                } else {
                    Log.i(TAG, "Cache hit")
                    coldStorageModel.objectToCache
                }
            }
            return null
        }

        /**
         * Method to put the key value pair into the cache.
         */
        fun put(key: String, value: Any?, timeToLive: Long?) {
            cacheMap[key] = ColdStorageModel(value!!, System.currentTimeMillis(), timeToLive)
            Log.i(TAG, "Putting value in cache")
        }

    }
}