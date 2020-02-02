package com.arcane.coldstorageannotation

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class LoadImage(
    val url: String,

    val placeHolder: Int = -1,

    val height: Int = -1,

    val width: Int = -1,

    val ttl: Int = -1,

    val persist: Boolean = false
)

