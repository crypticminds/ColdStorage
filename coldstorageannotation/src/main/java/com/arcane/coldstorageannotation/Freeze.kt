package com.arcane.coldstorageannotation

/**
 * The annotation is used on top of a class to specify that the output
 * of all the methods will be cached.
 *
 * @param timeToLive the time in milliseconds after which the data will be
 * considered stale.
 *
 * @param maxSize the maximum size allocated for the class to store data in
 * the cache.
 *
 * @param generatedClassName the name of the cache class that will be generated.
 * If a value is not provided here a prefix "Generated" will be added to the original
 * class.
 *
 * This cache will generate a class with the original class name prefixed with
 * "cacheLayer" with all the methods wrapped with the caching logic.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Freeze(val timeToLive: Long = -1,
                        val maxSize: Long = -1,
                        val generatedClassName : String = "")