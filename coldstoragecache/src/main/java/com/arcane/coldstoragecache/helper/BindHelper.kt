package com.arcane.coldstoragecache.helper

import android.app.Activity
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment

/**
 * Utility class that helps with binding views to the resource ids.
 *
 * @author Anurag.
 *
 */
class BindHelper {

    companion object {

        /**
         * Binds an ImageView present inside a different view to the given resource id.g
         */
        fun bindViewToResource(anyObject: Any, parentView: Int, resourceId: Int): ImageView {

            return when (anyObject) {
                is Activity -> {
                    bindView(bindView(anyObject, parentView), resourceId) as ImageView
                }
                is View -> {
                    bindView(bindView(anyObject, parentView), resourceId) as ImageView
                }
                is Fragment -> {
                    bindView(bindView(anyObject, parentView), resourceId) as ImageView
                }
                else -> {
                    throw Exception("Only views , activities and fragments are supported for the annotation")
                }
            }

        }

        /**
         * Method that will bind views to the resource ids.
         */
        fun bindViewToResource(anyObject: Any, resourceId: Int): ImageView {
            return when (anyObject) {
                is Activity -> {
                    bindView(anyObject, resourceId) as ImageView
                }
                is View -> {
                    bindView(anyObject, resourceId) as ImageView
                }
                is Fragment -> {
                    bindView(anyObject, resourceId) as ImageView
                }
                else -> {
                    throw Exception("Only views , activities and fragments are supported for the annotation")
                }
            }
        }

        private fun bindView(fragment: Fragment, resourceId: Int): View {
            if (fragment.view != null) {
                return fragment.view!!.findViewById(resourceId)
            } else {
                throw Exception("Unable to get the root view of the fragment ${fragment.javaClass.simpleName}")
            }
        }

        private fun bindView(activity: Activity, resourceId: Int): View {
            return activity.window.decorView.findViewById(resourceId)
        }


        private fun bindView(view: View, resourceId: Int): View {
            return view.findViewById(resourceId)
        }
    }
}