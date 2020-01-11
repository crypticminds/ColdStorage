package com.arcane.coldstorageannotations.annotation

/**
 * The annotation that is used over methods
 * to cache the output of a method. The annotation will take
 * care of returning the cached output if the input is found to be
 * the same.
 *
 * @param operation The operation for which the cache is being defined.
 * A cache can have multiple values for the same operation.The operation
 * is used to identify a particular function.
 *
 *
 * @param keys Specify the names of the variables whose values will determine
 * the output of the function. If no variable names are specified here it
 * will default to picking all the variables
 *
 * @param timeToLive Optional value to determine when the cached result will be
 * considered stale.
 *
 * Usage ->
 *
 * @Refrigerate(key = "url" , timeToLive = 2000 , operation = "DOWNLOAD_IMAGE")
 * fun downloadImageFromSomeSource (val url : String) : Bitmap {
 *
 * }
 *
 * @author Anurag.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Refrigerate(
        val operation: String,
        val keys: Array<String> = [],
        val timeToLive: Long = -1)