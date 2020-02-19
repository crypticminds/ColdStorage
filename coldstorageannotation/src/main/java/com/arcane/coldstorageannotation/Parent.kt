package com.arcane.coldstorageannotation

/**
 * An annotation that can be used to declare the parent of a view.
 * This annotation is used in conjunction with other data loading and
 * caching annotation such as @LoadImage.
 *
 * Usage
 *
 * @LoadImage(R.id.my_image_view,"myurl")
 * @Parent(R.id.my_custom_view)
 * lateinit var myImageViewInsideCustomView : ImageView
 *
 * This will load the image from "myrul" into the "my_image_view" inside "my_custom_view"
 *
 * @param resourceId the resource id of the parent view.
 *
 * @author Anurag.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class Parent(val resourceId: Int)