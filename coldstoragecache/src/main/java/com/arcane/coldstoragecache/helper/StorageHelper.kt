package com.arcane.coldstoragecache.helper

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.arcane.coldstoragecache.cache.Cache
import com.arcane.coldstoragecache.cache.ColdStorage
import com.arcane.coldstoragecache.model.ColdStorageModel
import com.arcane.coldstoragecache.model.ImageMetadata
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread

/**
 * Utility class to help with storage related operations.
 *
 * @author Anurag.
 */
class StorageHelper(private val context: Context) {

    companion object {

        /**
         * The shared preference where the metadata of the images will be stored.
         * The actual images will be stored as files in the internal storage and the
         * metadata will be used to store values such as the the size of the image,
         * timeToLive, timestamp when the image was cached into memory.
         */
        const val IMAGE_PREF = "image-pref"

        /**
         * The directory where the images will be stored.
         */
        const val IMAGES_DIR = "cached_images_cs"

        /**
         * Tag for logging.
         */
        const val TAG = "StorageHelper"
    }

    /**
     * Method that loads all the items from the disk into the application memory.
     *
     * TODO : While loading the memory should be trimmed.
     */
    fun loadDataIntoMemory() {
        //this includes images and other cached objects.
        loadImagesIntoMemory()
    }


    /**
     * Method that returns the images from the disk.If the image is
     * not found in the application memory then the disk will be
     * checked and the image will be loaded from the disk.
     *
     * TODO : Put the loaded image into the application memory by swapping
     * a stale or less frequently used image.
     */
    fun getImageFromDisk(key: String): Bitmap? {
        val contextWrapper = ContextWrapper(context)
        val imageMetadataString = getImagePreference().getString(key, "")
        if (imageMetadataString == "") {
            return null
        }
        val imageMetadata = CommonHelper.getObjectMapper()
            .readValue(imageMetadataString, ImageMetadata::class.java)
        val dir = contextWrapper.getDir(IMAGES_DIR, Context.MODE_PRIVATE)
        val imageFile = File(dir, imageMetadata.imageFileName)
        return BitmapFactory.decodeFile(imageFile.path)

    }


    /**
     * Method to write the image into the internal storage.
     */
    fun persistImagesIntoMemory(key: String, bitmap: Bitmap) {
        thread {
            val contextWrapper = ContextWrapper(context)
            val dir = contextWrapper.getDir(IMAGES_DIR, Context.MODE_PRIVATE)
            val file = File(dir, "$key.jpg")
            if (file.exists()) {
                file.delete()
            }
            file.parentFile!!.mkdirs()
            file.createNewFile()
            try {
                val fileOutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                writeImageMetadataToSharedPref(file, key)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to store images into memory", e)
            }

        }
    }


    private fun writeImageMetadataToSharedPref(file: File, key: String) {
        val imageMetadata =
            ImageMetadata("$key.jpg", file.length(), System.currentTimeMillis())
        val imageMetadataAsString = CommonHelper.getObjectMapper().writeValueAsString(imageMetadata)
        val sharedPreferences = context.getSharedPreferences(IMAGE_PREF, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(key, imageMetadataAsString).apply()
    }


    private fun loadImagesIntoMemory() {
        val imagesPreferences = getImagePreference()
        imagesPreferences.all.entries.forEach { entry ->
            if (!isStoredImageIsStale(entry.value as String)) {
                val imageFromDisk = getImageFromDisk(entry.key)
                if (imageFromDisk != null) {
                    //putting images into the cache map
                    ColdStorage.cacheMap[entry.key] =
                        ColdStorageModel(imageFromDisk, -1, -1)
                }
            } else {
                removeImageFromDisk(entry.key)
            }
        }
    }


    private fun getImagePreference(): SharedPreferences {
        return context.getSharedPreferences(IMAGE_PREF, Context.MODE_PRIVATE)
    }

    private fun isStoredImageIsStale(imageMetadataString: String): Boolean {
        val imageMetadata = CommonHelper.getObjectMapper()
            .readValue(imageMetadataString, ImageMetadata::class.java)
        val timeDifference = System.currentTimeMillis() - imageMetadata.timestamp
        val timeDifferenceInDays = (timeDifference.toDouble() / (1000 * 60 * 60 * 24))
        return Cache.ttlForDiskStorage < timeDifferenceInDays
    }


    private fun removeImageFromDisk(key: String) {
        getImagePreference().edit().remove(key).apply()
        thread {
            val contextWrapper = ContextWrapper(context)
            val dir = contextWrapper.getDir(IMAGES_DIR, Context.MODE_PRIVATE)
            val file = File(dir, "$key.jpg")
            if (file.exists()) {
                file.delete()
            }
        }
    }
}