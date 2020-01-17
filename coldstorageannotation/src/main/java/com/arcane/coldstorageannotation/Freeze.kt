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
 * This cache will generate a class with the original class name prefixed with
 * "cacheLayer" with all the methods wrapped with the caching logic.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Freeze(val timeToLive: Long = -1, val maxSize: Long = -1)