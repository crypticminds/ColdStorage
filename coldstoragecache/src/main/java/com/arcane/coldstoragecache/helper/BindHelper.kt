package com.arcane.coldstoragecache.helper

import android.animation.ValueAnimator
import android.widget.ImageView

class BindHelper {

    companion object {

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