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
        internal val cacheMap: ConcurrentHashMap<String, ColdStorageModel> = ConcurrentHashMap()

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
            trimData()
        }

        /**
         * Method that will remove data from the cache if it exceeds the max in memory size.
         * The updated data will be stored in shared preferences.
         * The method only trims on the basis of the object that has been stored and
         * does not calculate the exact amount of memory occupied by the
         * cache.
         */
        //TODO properly calculate the size of the object.
        internal fun trimData() {
            var totalMemory = 0
            cacheMap.forEach { entry ->
                // an estimate of the size of the object
                val sizeOfObject = 36 + (entry.value.objectToCache.toString().length * 2)
                totalMemory += sizeOfObject
            }
            while (totalMemory > Cache.maxAllocateCachedMemory) {
                val minEntry = cacheMap.minBy { entry -> entry.value.timestamp }

                if (minEntry != null) {
                    totalMemory -= ((minEntry.value.objectToCache.toString().length * 2) + 36)
                    cacheMap.remove(minEntry.key)
                } else {
                    break
                }
            }
        }

    }
}