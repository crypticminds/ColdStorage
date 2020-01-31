package com.arcane.coldstoragecache.converter.impl

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.arcane.coldstoragecache.converter.IConverter

/**
 * An example converter that will convert the stored string
 * into a bitmap.
 *
 * @author Anurag
 */
class StringToBitmapConverter : IConverter<Bitmap?> {

    /**
     * The function will convert the string stored in the cache
     * into the required bitmap.
     */
    override fun convert(cachedString: String): Bitmap? {
        val encodeByte: ByteArray = Base64.decode(cachedString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)

    }
}