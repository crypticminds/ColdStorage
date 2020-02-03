package com.arcane.coldstoragecache.helper

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import java.net.URL

class ImageHelper {

    companion object {

        fun startAnimation(imageView: ImageView, animator: ValueAnimator) {
            imageView.post {
                animator.start()
            }
        }

        fun setImageInView(imageView: ImageView, bitmap: Bitmap, animator: ValueAnimator) {
            imageView.post {
                animator.cancel()
                imageView.rotation = 0f
                imageView.setImageBitmap(bitmap)
            }
        }


        fun setImageInView(imageView: ImageView, bitmap: Bitmap) {
            imageView.post { imageView.setImageBitmap(bitmap) }
        }

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