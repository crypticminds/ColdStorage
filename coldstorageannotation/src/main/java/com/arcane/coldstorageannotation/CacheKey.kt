package com.arcane.coldstorageannotation

/**
 * Annotation to mark a method parameter as the key of the cache.
 * This can be used to mark multiple parameters and all of them together
 * will form the key for the cache for that particular method.
 *
 * If no key is specified then all the parameters will together form
 * the key of the cache.
 *
 * @author Anurag
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class CacheKey