package com.arcane.coldstoragecache.cache

import android.app.Activity
import android.content.Context
import android.util.Log
import com.arcane.coldstoragecache.callback.OnValueFetchedCallback
import com.arcane.coldstoragecache.converter.IConverter
import com.arcane.coldstoragecache.model.CachedDataModel
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
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
         * The maximum number of threads that can be spawned
         */
        private var maxThread: Int = 10


        /**
         * The executor service that will be used by cold storage for
         * all concurrent operations.
         */
        var executorService: ExecutorService = Executors
            .newFixedThreadPool(maxThread)


        private var generatedBindClass: Class<*>? = null


        /**
         * The name of the generated bind class.
         */
        private const val BIND_CLASS_NAME = "com.arcane.generated.BindingClass"

        /**
         * The log message for cache miss.
         */
        private const val CACHE_MISS_LOG = "Cache miss due to stale data"

        /**
         * The tag for logging.
         */
        private const val TAG = "COLD_STORAGE"

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
         * The generated map by the annotation processor.
         */
        private var generatedReferenceMap: Map<String, String> = hashMapOf()

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
         * @param maxBackgroundThreads The maximum number of threads that will be spawned
         * during downloads
         *
         */
        fun initialize(
            context: Context,
            maxAllocatedMemory: Int = 1024 * 1024 * 20,
            timeToLive: Int? = null,
            maxBackgroundThreads: Int = 10
        ) {
            maxAllocateCachedMemory = maxAllocatedMemory
            maxTimeToLive = timeToLive
            executorService = Executors.newFixedThreadPool(maxBackgroundThreads)
            //running entire operation on a separate thread to not block
            //the UI thread.
            thread {
                val sharedPreferences =
                    context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                val cachedData =
                    sharedPreferences.all.keys

                //removing stale objects from the cache
                cachedData.forEach { key ->
                    val cachedDataModel = objectMapper.readValue(
                        sharedPreferences.getString(key, ""),
                        CachedDataModel::class.java
                    )
                    if (!isDataStale(
                            cachedDataModel.timeToLive,
                            cachedDataModel.timestamp
                        )
                    ) {
                        cache[key] = cachedDataModel
                    }
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

        /**
         * Method that checks if the data is stale.
         *
         * @param cachedDataModel the cached data.
         */
        fun isDataStale(timeToLive: Long?, timestamp: Long): Boolean {
            val current = System.currentTimeMillis()
            if (timeToLive != null) {
                val difference = current - timestamp
                if (difference > timeToLive) {
                    Log.i(TAG, CACHE_MISS_LOG)
                    return true
                }
                return false
            } else if (maxTimeToLive != null) {
                val differenceGlobal = current - timestamp
                if (differenceGlobal > maxTimeToLive!!) {
                    Log.i(TAG, CACHE_MISS_LOG)
                    return true
                }
                Log.i(TAG, "Cache hit")
                return false
            }
            Log.i(TAG, "Cache hit")
            return false

        }

        /**
         * Method to clear the cache.
         */
        fun clearCache() {
            cache.clear()
        }

        /**
         * Method that can be used to cache an object if the get method is not used
         * directly.This can be used when the data fetching logic cannot be implemented
         * inside the update method or the cache needs to be updated
         * from a different async process.
         *
         * @param key the key for which the object needs to be cached
         *
         * @param objectToCache the object that needs to be cached.This object
         * should be serializable so that it can be converted to a string.
         *
         * @param timeToLive the time after which the object will be considered stale.
         */
        fun <Value> addToCache(key: String, objectToCache: Value, timeToLive: Long? = null) {
            val objectAsString = objectMapper.writeValueAsString(objectToCache)
            cache[key] = CachedDataModel(
                objectAsString,
                System.currentTimeMillis(), timeToLive
            )
        }

        /**
         * Method to get a value from the cache.
         *
         * @param key The key for which the value is fetched.
         * If the key is not present in the cache null is returned.
         */
        fun get(key: String): CachedDataModel? {
            return if (cache.containsKey(key)) {
                if (isDataStale(cache[key]!!.timeToLive, cache[key]!!.timestamp)) {
                    null
                } else {
                    cache[key]
                }
            } else {
                null
            }
        }

        /**
         * Method used to bind annotated views in a activity to the cache.
         */
        fun bind(activity: Activity) {
            try {
                if (generatedBindClass == null) {
                    generatedBindClass = Class.forName(BIND_CLASS_NAME)
                }
                generatedBindClass!!.getMethod(
                    "bind${activity.javaClass.simpleName}",
                    activity.javaClass
                ).invoke(generatedBindClass!!.newInstance(), activity)
            } catch (e: Exception) {
                Log.e(TAG, "Unable to bind elements to cache in class ${activity.javaClass.name}")
            }
            //from the generated class pass values into the views.
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
    fun <Output> get(
        key: String,
        onValueFetchedCallback: OnValueFetchedCallback<Output?>,
        converter: IConverter<Output?>,
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
        trimData()
        cache.forEach { entry ->
            if (!isDataStale(entry.value.timeToLive, entry.value.timestamp)) {
                val stringValue = objectMapper.writeValueAsString(entry.value)
                sharedPreferences.edit().putString(entry.key, stringValue).apply()
            }
        }
    }


    /**
     * Method that will return the value from cache if present but it will
     * not make a call to the update method for updating the cache.
     * The caller is responsible for updating the cache if there is a cache miss
     * using the addToCache method.
     *
     * @param key The key for which the value needs to be fetched.
     *
     * @param converter An optional converter that will transform the string
     * into the required model. If the converter is not passed , the method will
     * return a string or a null value if the cache does not contain the data or
     * if the data is stale.
     */
    fun getWithoutUpdate(key: String, converter: IConverter<Any?>? = null): Any? {
        if (cache.containsKey(key)) {
            //if data is stale null is returned.
            if (isDataStale(cache[key]!!.timeToLive, cache[key]!!.timestamp)) {
                return null
            }
            val cachedString = cache[key]!!.objectToCache
            if (converter != null) {
                return converter.convert(cachedString)
            }
            return cachedString
        }
        return null
    }


    private fun fetchFromCache(key: String): CachedDataModel? {
        return if (cache.containsKey(key)) {
            val cachedDataModel = cache[key]!!
            if (isDataStale(cachedDataModel.timeToLive, cachedDataModel.timestamp)) {
                return null
            }
            return cachedDataModel
        } else {
            null
        }
    }

    /**
     * Method that can be used to cache an object if the get method is not used
     * directly.This can be used when the data fetching logic cannot be implemented
     * inside the update method or the cache needs to be updated
     * from a different async process.
     *
     * @param key the key for which the object needs to be cached
     *
     * @param objectToCache the object that needs to be cached.This object
     * should be serializable so that it can be converted to a string.
     *
     * @param timeToLive the time after which the object will be considered stale.
     */
    @Deprecated("Use the static method in place of this.")
    fun addToCache(key: String, objectToCache: Any, timeToLive: Long? = null) {
        val objectAsString = objectMapper.writeValueAsString(objectToCache)
        cache[key] = CachedDataModel(
            objectAsString,
            System.currentTimeMillis(), timeToLive
        )
    }

    /**
     * The update function needs to be implemented. This method should
     * specify from where the data needs to be fetched if the key is
     * not found inside the cache.
     *
     * @param key the key for which the value was not found in the cache.
     *
     * @return the serialized value.
     */
    abstract fun update(key: String): String?

}