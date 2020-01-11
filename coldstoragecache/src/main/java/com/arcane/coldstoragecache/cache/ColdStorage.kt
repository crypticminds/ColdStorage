package com.arcane.coldstoragecache.cache

import com.arcane.coldstoragecache.model.ColdStorageModel
import java.util.concurrent.ConcurrentHashMap

class ColdStorage {

    companion object {

        private val cacheMap: ConcurrentHashMap<String, ColdStorageModel> = ConcurrentHashMap()

        /**
         * Method to get a value from the cache.
         */
        fun get(key: String): Any? {
            if (cacheMap.containsKey(key)) {
                val coldStorageModel = cacheMap[key] ?: return null
                return if (Cache.isDataStale(coldStorageModel.timeToLive,
                                coldStorageModel.timestamp)) {
                    null
                } else {
                    coldStorageModel.objectToCache
                }
            }
            return null
        }

        /**
         * Method to put the key value pair into the cache.
         */
        fun put(key: String, value: Any, timeToLive: Long?) {
            cacheMap[key] = ColdStorageModel(value, System.currentTimeMillis(), timeToLive)
        }

    }
}