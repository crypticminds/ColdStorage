package com.arcane.coldstoragecache.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * The metadata for each image that is stored in the
 * disk.
 *
 * @author Anurag
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ImageMetadata(

    /**
     * The name of the image file.
     */
    val imageFileName: String,

    /**
     * The size of the image.
     */
    val sizeOfImage: Long,

    /**
     * The timestamp when the image was stored in the disk.
     */
    val timestamp: Long
)