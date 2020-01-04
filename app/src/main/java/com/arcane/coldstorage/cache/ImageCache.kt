package com.arcane.coldstorage.cache

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.arcane.coldstoragecache.cache.Cache
import java.io.ByteArrayOutputStream
import java.net.URL


/**
 * An example class that is used to cache images
 * after downloading them from a given url.
 *
 */
class ImageCache : Cache() {

    /**
     * The update function is only required here to specify
     * where to fetch the data from if the key is not present in the
     * cache.
     *
     * @param key Here the key is the url from where the
     * image needs to be downloaded.
     */
    override fun update(key: String): String? {
        return try {
            val url = URL(key)
            val connection = url.openConnection()
            connection.doInput = true
            connection.connect()
            val input = connection.getInputStream()
            val myBitmap = BitmapFactory.decodeStream(input)
            bitMapToString(myBitmap)
        } catch (e: Exception) {
            Log.e("EXCEPTION", "Unable to download image", e)
            return null
        }
    }

    /**
     * A transformer to convert the bitmap to string.
     */
    private fun bitMapToString(bitmap: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }
}