package com.arcane.coldstoragecache.converter.impl

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.arcane.coldstoragecache.converter.IConverter

/**
 * An example converter that will convert the stored string
 * into a bitmap.
 *
 * @author Anurag
 */
class StringToBitmapConverter : IConverter<Any?> {

    /**
     * The function will convert the string stored in the cache
     * into the required bitmap.
     */
    override fun convert(cachedString: String): Bitmap? {
        return try {
            val encodeByte: ByteArray = Base64.decode(cachedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            Log.e("ERROR", "Failed to convert cached string to bitmap")
            return null
        }

    }
}