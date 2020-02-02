package com.arcane.coldstoragecache.helper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL

class ImageHelper {

    companion object {

        fun downloadImage(stringURL: String): Bitmap? {
            return try {
                val url = URL(stringURL)
                val connection = url.openConnection()
                connection.doInput = true
                connection.connect()
                val input = connection.getInputStream()
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                null
            }
        }
    }
}