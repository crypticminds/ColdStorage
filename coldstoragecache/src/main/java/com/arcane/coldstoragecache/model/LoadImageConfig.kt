package com.arcane.coldstoragecache.model

/**
 * A model that stores the config for the annotated image views.
 *
 * @author Anurag
 */
class LoadImageConfig(

    /**
     * The url from where the image will be downloaded.
     */
    val url: String,

    /**
     * The placeholder image resource id.
     */
    val placeHolder: Int,

    /**
     * Boolean to indicate if loading animation should be enabled.
     */
    val enableLoadingAnimation: Boolean,

    /**
     * The resource id of the image view that is annotated.
     */
    val imageViewResourceId: Int,

    /**
     * Boolean to indicate if the image needs to be persisted to disk.
     */
    val persistImageToDisk: Boolean
)