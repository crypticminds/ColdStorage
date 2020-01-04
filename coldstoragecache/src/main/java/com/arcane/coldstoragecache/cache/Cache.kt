package com.arcane.coldstoragecache.cache

import android.content.Context
import android.util.Log
import com.arcane.coldstoragecache.callback.OnValueFetchedCallback
import com.arcane.coldstoragecache.converter.IConverter
import com.arcane.coldstoragecache.model.CachedDataModel
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * The cache class.
 * To create a cache layer ,this class needs to be
 * extended and the update methods needs to be implemented with
 * the logic to fetch the data if the data is not present in the
 * cache.
 *
 * @author Anurag.
 */
abstract class Cache {

    /**
     * The static methods and variables of the class.
     */
    companion object {

        /**
         * An instance of object mapper.
         */
        private val objectMapper = jacksonObjectMapper()

        /**
         * The shared preference name where all the objects are
         * cached/
         */
        private const val SHARED_PREF_NAME = "coldstoragesharedpref"

        /**
         * The in memory cache.
         */
        private val cache: ConcurrentHashMap<String, CachedDataModel> =
            ConcurrentHashMap()

        /**
         * The max amount of data that can be stored in app memory.
         * The default value is 20 Mb
         */
        private var maxAllocateCachedMemory: Int = 1024 * 1024 * 20

        /**
         * The max time before a cached data will be considered stale.
         * This is global value specified for the entire cache.
         * If null , then no data will be considered stale.
         * A TTL value can also be provided for each data that is
         * being stored in the cache.The object specific TTL will
         * override the global TTL.
         */
        private var maxTimeToLive: Int? = null

        /**
         * The initialize function is responsible for loading the
         * data from the shared preferences and storing them in the
         * memory.
         *
         * @param context The context (application or activity) that will
         * be used to fetch the shared preferences
         *
         * @param maxAllocatedMemory It specifies the maximum amount of
         * data (in bytes) that should be stored in app memory.
         * The default value is 20 mb.
         *
         * @param timeToLive The global time to live for the cache. (In milliseconds)
         *
         */
        fun initialize(
            context: Context,
            maxAllocatedMemory: Int = 1024 * 1024 * 20,
            timeToLive: Int? = null
        ) {
            maxAllocateCachedMemory = maxAllocatedMemory
            maxTimeToLive = timeToLive
            //running entire operation on a separate thread to not block
            //the UI thread.
            thread {
                val sharedPreferences =
                    context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                val cachedData =
                    sharedPreferences.all.keys

                Log.e("cold_storage", cachedData.toString())
                //removing stale objects from the cache
                cachedData.forEach { key ->
                    val current = System.currentTimeMillis()
                    val cachedDataModel = objectMapper.readValue(
                        sharedPreferences.getString(key, ""),
                        CachedDataModel::class.java
                    )
                    if (cachedDataModel.timeToLive != null) {
                        val difference = current - cachedDataModel.timestamp
                        if (difference < cachedDataModel.timeToLive) {
                            cache[key] = cachedDataModel
                        }
                    } else if (maxTimeToLive != null) {
                        val differenceGlobal = current - cachedDataModel.timestamp
                        if (differenceGlobal < maxTimeToLive!!) {
                            cache[key] = cachedDataModel
                        }
                    } else {
                        cache[key] = cachedDataModel
                    }

                    trimData()
                }
            }
        }

        /**
         * Method that will remove data from the cache if it exceeds the max in memory size.
         * The updated data will be stored in shared preferences.
         * The method only trims on the basis of the object that has been stored and
         * does not calculate the exact amount of memory occupied by the
         * cache.
         */
        private fun trimData() {
            var totalMemory = 0
            cache.forEach { entry ->
                // an estimate of the size of the object
                val sizeOfObject = 36 + (entry.value.objectToCache.length * 2)
                totalMemory += sizeOfObject
            }
            while (totalMemory > maxAllocateCachedMemory) {
                val minEntry = cache.minBy { entry -> entry.value.timestamp }

                if (minEntry != null) {
                    totalMemory -= ((minEntry.value.objectToCache.length * 2) + 36)
                    cache.remove(minEntry.key)
                } else {
                    break
                }
            }


        }

    }

    /**
     * The method that is used to fetch the value from the
     * cache.It will first check the in memory map to see if the
     * key is present or not . If the key is present then it will return
     * the value.
     * If the key is absent, it will call the
     * update method to fetch the value and store it into the in memory
     * cache.The update method runs on a separate thread and the OnValueFetchedCallback
     * @see OnValueFetchedCallback , will be used to pass the result as string to the main
     * thread.
     *
     * @param key The key for which the value needs to be fetched.
     *
     * @param onValueFetchedCallback The callback that will be used to
     * pass the value into the main thread.
     *
     * @param timeToLive An optional value that decides when the value
     * needs to be considered stale.(In milliseconds)
     *
     */
    fun get(
        key: String,
        onValueFetchedCallback: OnValueFetchedCallback<String?>,
        timeToLive: Long? = null
    ) {
        thread {
            val cachedData = fetchFromCache(key)
            if (cachedData != null) {
                onValueFetchedCallback.valueFetched(cachedData.objectToCache)
                return@thread
            }
            val result = update(key)
            if (result != null) {
                val cachedDataModel =
                    CachedDataModel(result, System.currentTimeMillis(), timeToLive)
                cache[key] = cachedDataModel
                onValueFetchedCallback.valueFetched(cachedDataModel.objectToCache)
                return@thread
            }
            onValueFetchedCallback.valueFetched(null)
        }
    }

    /**
     * The method that is used to fetch the value from the
     * cache.It will first check the in memory map to see if the
     * key is present or not . If the key is present then it will return
     * the value.
     * If the key is absent, it will call the
     * update method to fetch the value and store it into the in memory
     * cache.The update method runs on a separate thread and the OnValueFetchedCallback
     * @see OnValueFetchedCallback , will be used to pass the result as string to the main
     * thread.
     *
     * @param key The key for which the value needs to be fetched.
     *
     * @param onValueFetchedCallback The callback that will be used to
     * pass the value into the main thread.
     *
     * @param converter A custom converter that is responsible for converting
     * the string to the required object.
     * @see  com.arcane.coldstoragecache.converter.impl.StringToBitmapConverter
     *
     * @param timeToLive An optional value that decides when the value
     * needs to be considered stale.(In milliseconds)
     *
     */
    fun get(
        key: String,
        onValueFetchedCallback: OnValueFetchedCallback<Any?>,
        converter: IConverter<Any?>,
        timeToLive: Long? = null
    ) {
        thread {
            val cachedData = fetchFromCache(key)
            if (cachedData != null) {
                onValueFetchedCallback.valueFetched(
                    converter
                        .convert(cachedData.objectToCache)
                )
                return@thread
            }

            val result = update(key)
            if (result != null) {
                val cachedDataModel =
                    CachedDataModel(result, System.currentTimeMillis(), timeToLive)
                cache[key] = cachedDataModel
                onValueFetchedCallback.valueFetched(
                    converter.convert(
                        cachedDataModel.objectToCache
                    )
                )
                return@thread
            }
            onValueFetchedCallback.valueFetched(null)
        }
    }


    /**
     * Method to fetch the data from the cache.
     *
     * @param key The key for which the data needs to be fetched.
     */
    private fun fetchFromCache(key: String): CachedDataModel? {
        return if (cache.containsKey(key)) {
            val current = System.currentTimeMillis()
            val cachedDataModel = cache[key]!!
            if (cachedDataModel.timeToLive != null) {
                val difference = current - cachedDataModel.timestamp
                if (difference > cachedDataModel.timeToLive) {
                    Log.i("COLD_STORAGE", "Cache miss due to stale data")
                    return null
                }
                Log.i("COLD_STORAGE", "Cache hit")
                return cachedDataModel
            } else if (maxTimeToLive != null) {
                val differenceGlobal = current - cachedDataModel.timestamp
                if (differenceGlobal > maxTimeToLive!!) {
                    Log.i("COLD_STORAGE", "Cache miss due to stale data")
                    return null
                }
                Log.i("COLD_STORAGE", "Cache hit")
                return cachedDataModel
            }
            Log.i("COLD_STORAGE", "Cache hit")
            return cache[key]
        } else {
            Log.i("COLD_STORAGE", "Cache miss")
            null
        }
    }


    /**
     * Method that is used to persist the current cache into the
     * shared preferences.
     * This method can be used to persist the data in cases when the
     * app is being closed or when the activity is being destroyed.
     *
     * @param context the context which will be used to
     * fetch the shared preferences.
     *
     */
    fun commitToSharedPref(context: Context) {
        val sharedPreferences = context
            .getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        cache.forEach { entry ->
            val stringValue = objectMapper.writeValueAsString(entry.value)
            sharedPreferences.edit().putString(entry.key, stringValue).apply()
        }
    }

    /**
     * The update function needs to be implemented. This method should
     * specify from where the data needs to be fetched if the key is
     * not found inside the cache.
     */
    abstract fun update(key: String): String?
}