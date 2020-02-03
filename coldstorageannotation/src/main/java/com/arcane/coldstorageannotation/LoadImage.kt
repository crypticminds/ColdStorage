package com.arcane.coldstorageannotation

/**
 * An annotation to load images directly into a image view.
 *
 * @param imageViewResourceId The resource id of the imageView.
 *
 * @param url The url from where the image will be downloaded.
 *
 * @param placeHolder The resource id of the placeholder image that should be displayed
 * til the image has been downloaded. (optional)
 *
 * @param enableLoadingAnimation Boolean representing if loading animation should
 * be enabled. The default animation is rotating the image. The animation will work
 * only if a placeHolder resource is passed to the annotation.
 * For example setting the value of placeholder to a circular loading icon with transparent
 * background and enabling the animation will rotate the image which will give the impression
 * of a circular progress bar.
 *
 * TODO : Change height , width of the image
 *
 * TODO : Accept custom animation classes.
 *
 * TODO : Accept boolean to decide whether to store the image to disk for future use.
 *
 * @author Anurag
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class LoadImage(

    val imageViewResourceId: Int,

    val url: String,

    val placeHolder: Int = -1,

    val enableLoadingAnimation: Boolean = false

)

