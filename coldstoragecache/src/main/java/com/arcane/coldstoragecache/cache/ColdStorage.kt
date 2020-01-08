package com.arcane.coldstoragecache.cache

import com.arcane.coldstoragecache.callback.OnOperationSuccessfulCallback
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.concurrent.thread
import kotlin.reflect.jvm.reflect

class ColdStorage {

    companion object {

        /**
         * The object mapper instance.
         */
        private val mapper = jacksonObjectMapper()


        /**
         * Method to get the value from cache.This method converts the cached string
         * data into the model expected by the caller.
         *
         * @param key the key for which the value needs to be fetched.
         *
         * @param output the class of the expected output model.
         *
         * @return the output object.
         */
        private fun <Output> getFromCache(key: String, output: Class<Output>): Output? {
            val cachedDataModel = Cache.get(key)
            return when {
                cachedDataModel == null -> {
                    null
                }
                Cache.isDataStale(cachedDataModel) -> {
                    null
                }
                else -> {
                    mapper.readValue(cachedDataModel.objectToCache, output)
                }
            }
        }

        /**
         * The method to fetch the data from the cache. This method returns the
         * string form of whatever object that is stored in the cache.
         * If the key is a string , the string would be kept as a key or else
         * the hashcode of the object will be kept as the key
         *
         * @param key the key for which the value must be fetched from the cache.
         *
         * @return the serialized object.
         */
        private fun getFromCache(key: String): String? {
            val cachedDataModel = Cache.get(key)
            return when {
                cachedDataModel == null -> {
                    null
                }
                Cache.isDataStale(cachedDataModel) -> {
                    null
                }
                else -> {
                    cachedDataModel.objectToCache
                }
            }
        }


        /**
         * The higher order function that can be used to wrap a method whose
         * value needs to be cached.
         * This method is suitable for wrapping functions with one parameter.
         * The value of the parameter will be the key of the cache.
         * If the key is a string , the string would be kept as a key or else
         * the hashcode of the object will be kept as the key
         *
         *  @param lambda the function whose output needs to be cached.
         *
         *  @param input the input to be provided to the method.
         *
         *  @param onOperationSuccessfulCallback the callback used
         *  to pass the value to the main thread.
         *
         */
        fun <Input> cache(
            lambda: (Input) -> String,
            input: Input,
            onOperationSuccessfulCallback: OnOperationSuccessfulCallback<String>
        ) {
            thread {
                val key = if (input is String) {
                    input
                } else {
                    input.hashCode().toString()
                }
                val cachedValue = getFromCache(key)
                val value = if (cachedValue == null) {
                    val refreshedValue = lambda.invoke(input)
                    Cache.addToCache(key, refreshedValue)
                    refreshedValue
                } else {
                    cachedValue
                }
                print(lambda.reflect()!!.name)
                onOperationSuccessfulCallback.operationSuccessful(
                    value,
                    getOperationFromAnnotation(lambda.reflect()!!.annotations)
                )
            }
        }

        /**
         * The higher order function that can be used to wrap a method whose
         * value needs to be cached.
         * This method is suitable for wrapping functions with one parameter.
         * The value of the parameter will be the key of the cache.
         *
         *  @param lambda the function whose output needs to be cached.
         *
         *  @param input the input to be provided to the method.
         *
         *  @param onOperationSuccessfulCallback the callback used
         *  to pass the value to the main thread.
         */

        fun <Input, Output> cache(
            lambda: (Input) -> Output,
            input: Input,
            output: Class<Output>,
            onOperationSuccessfulCallback: OnOperationSuccessfulCallback<Output>
        ) {
            thread {
                val key = if (input is String) {
                    input
                } else {
                    input.hashCode().toString()
                }
                val cachedValue = getFromCache(key, output)
                val value = if (cachedValue == null) {
                    val refreshedValue = lambda.invoke(input)
                    Cache.addToCache(key, refreshedValue)
                    refreshedValue
                } else {
                    cachedValue
                }
                onOperationSuccessfulCallback.operationSuccessful(
                    value,
                    getOperationFromAnnotation(lambda.reflect()!!.annotations)
                )
            }
        }

        private fun getOperationFromAnnotation(annotations: List<Annotation>): String {

            return ""
        }
    }
}