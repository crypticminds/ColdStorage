package com.arcane.coldstoragecache.helper

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import java.net.URL

/**
 * Utility class for handling image related operations.
 *
 * @author Anurag.
 */
class ImageHelper {

    companion object {

        private const val TAG: String = "IMAGE_HELPER"

        /**
         * Method that starts the animation of the imageView.
         *
         * @param imageView the view on which the animation will be performed
         *
         * @param animator the animator class.
         */
        fun startAnimation(imageView: ImageView, animator: Animator) {
            imageView.post {
                animator.start()
            }
        }

        /**
         * Method that sets the bitmap into the imageView and cancels any ongoing
         * loading animation.
         *
         * @param imageView the imageView where the bitmap will be set.
         *
         * @param bitmap the image that should be set into the bitmap.
         *
         * @param animator the ongoing animation.
         */
        fun setImageInView(imageView: ImageView, bitmap: Bitmap, animator: ValueAnimator) {
            imageView.post {
                animator.cancel()
                imageView.rotation = 0f
                imageView.setImageBitmap(bitmap)
            }
        }


        /**
         * Method that sets the bitmap into the imageView.
         *
         * @param imageView the imageView where the bitmap will be set.
         *
         * @param bitmap the image that should be set into the bitmap.
         *
         */
        fun setImageInView(imageView: ImageView, bitmap: Bitmap) {
            imageView.post { imageView.setImageBitmap(bitmap) }
        }

        /**
         * Method that downloads the image from a URL.
         *
         * @param stringURL URL from which the image needs to be downloaded.
         */
        fun downloadImage(stringURL: String): Bitmap? {
            return try {
                val url = URL(stringURL)
                val connection = url.openConnection()
                connection.doInput = true
                connection.connect()
                val input = connection.getInputStream()
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                Log.e(TAG, "Image download failed", e)
                null
            }
        }

        /**
         * Method that creates the default animation for the imageView.
         *
         * @param imageView the imageVIew where the animation will be applied.
         */
        fun animateImageView(imageView: ImageView): ValueAnimator {
            val animator = ValueAnimator.ofFloat(0f, 360f)
            animator.duration = 2000
            animator.addUpdateListener {
                val animatedValue = it.animatedValue as Float
                imageView.rotation = animatedValue
            }
            animator.repeatCount = ValueAnimator.INFINITE
            return animator
        }
    }
}