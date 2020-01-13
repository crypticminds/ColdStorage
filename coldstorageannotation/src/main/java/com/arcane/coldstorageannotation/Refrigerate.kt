package com.arcane.coldstorageannotation

/**
 * The annotation that will cache the data during the
 * runtime.
 *
 * @param timeToLive The amount of time the cache will be considered
 * valid. After this time , the cache will expire.
 *
 * @param keys the variable names that should be considered the keys
 * for the operation . For example for an image download the key will be
 * the url from which the image needs to be downloaded from.
 * If no key is present in the array , all the parameters for the function
 * will be considered as keys
 *
 * @param operation the unique name for the operation.This name will be
 * returned using the callback along with the result.
 *
 * @author Anurag
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Refrigerate(
    val timeToLive: Long = -1,
    val keys: Array<String> = [],
    val operation: String
)