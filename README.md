
# ColdStorage 
<p align="center">
 <img src="https://i.imgur.com/rEE8hUO.jpg"/>
</p>

**A lightweight data loading and caching library for android**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) ![Downloads](https://jitpack.io/v/crypticminds/ColdStorage/month.svg) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/946075aa2cc14be3af73eb8a9fc2352b)](https://www.codacy.com/manual/crypticminds/ColdStorage?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=crypticminds/ColdStorage&amp;utm_campaign=Badge_Grade) [![Build Status](https://travis-ci.com/crypticminds/ColdStorage.svg?branch=master)](https://travis-ci.com/crypticminds/ColdStorage) [![Gitter](https://badges.gitter.im/ColdStorageCache/community.svg)](https://gitter.im/ColdStorageCache/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

# Quicklinks

* [**Feature requests**](https://github.com/crypticminds/ColdStorage/issues/new?assignees=&labels=&template=feature_request.md&title=): Got a new requirement? Request it here and it will be delivered.
* [**Bugs**](https://github.com/crypticminds/ColdStorage/issues/new?assignees=&labels=&template=bug_report.md&title=): Report a bug  here and it will be squashed.
* [**Questions**](https://gitter.im/ColdStorageCache/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge): Get your questions about contributing, usage or anything related to the library answered here.
* [**Examples**](https://github.com/crypticminds/coldstorageexamples) : Check out various examples here.
* [**Suggestions/Complaints/Feedbacks**](https://github.com/crypticminds/ColdStorage/issues/new): Got something in your mind regarding this library? Please share and help us improve.
* [**Upcoming features**](https://github.com/crypticminds/ColdStorage/wiki/Upcoming-features) : If you are interested to see all the exciting features lined up for delivery.

# Articles
* [Caching in general](https://medium.com/@crypticmindscom_5258/caching-made-easy-with-kotlin-part-1-53433c756978)
* [@Refrigerate annotation usage](https://medium.com/@crypticmindscom_5258/caching-made-easy-in-android-with-kotlin-part-2-61bb476063b4)
* [@Freeze annotation usage](https://medium.com/@crypticmindscom_5258/caching-made-easy-on-android-with-kotlin-part-3-3d4cfcb57df0)
* [@LoadImage annotation usage](https://medium.com/@crypticmindscom_5258/caching-made-easy-on-android-with-kotlin-part-4-18e7b066e9c2)
# Setup

*  Add kotlin-kapt gradle plugin to **app build.gradle** file

        apply plugin: "kotlin-kapt"

*  Add the dependencies

        implementation "com.github.crypticminds.ColdStorage:coldstoragecache:4.1.1"
        kapt "com.github.crypticminds.ColdStorage:coldstoragecompiler:4.1.1"
    	implementation "com.github.crypticminds.ColdStorage:coldstorageannotation:4.1.1"
    
***Check the latest release to get the newest features.***
     
 **You need to initialize the cache when the application starts. The initialization takes care of pulling previously cached data and loading them into the memory .** 
 
*  Create an application class and initialize the cache in the onCreate() method.
 
    ```kotlin
	    import android.app.Application
	    import com.arcane.coldstoragecache.cache.Cache
    
	    class Application : Application() {
    
        override fun onCreate() {
            super.onCreate()
            Cache.initialize(context = applicationContext)
	        }
	    }
    ```
	You can configure the cache with additional parameters such as a global time to live, maximum cache size etc. Refer the [wiki]([https://github.com/crypticminds/ColdStorage/wiki/Initialize-cache](https://github.com/crypticminds/ColdStorage/wiki/Initialize-cache)) for more details. 

*  Register your application in the android manifest file by providing the **android:name** attribute

    ```xml
    <application
            android:allowBackup="true"
            android:icon="@mipmap/ic_launcher"
            android:label="@string/app_name"
            android:roundIcon="@mipmap/ic_launcher_round"
            android:supportsRtl="true"
            android:theme="@style/AppTheme"
            android:name=".application.Application">
    </application>
# Quick guide

### This guide will only provide a basic usage guide. For detailed description of each component, usage and examples check the [wiki](https://github.com/crypticminds/ColdStorage/wiki)

## @LoadImage Annotation 

You can annotate any ImageView present in an Activity , fragement or another view to load images from an URL and cache it for future use.

```kotlin
@LoadImage(
        R.id.image_1,
        "https://images.unsplash.com/photo-1549740425-5e9ed4d8cd34?ixlib=rb-1.2.1&w=1000&q=80",
        placeHolder = R.drawable.loading, enableLoadingAnimation = true, persistImageToDisk = true
    )
    lateinit var imageWithAnimation: ImageView
```

Images can be persisted into the internal storage using the **"persistImageToDisk"** parameter.

After the image views have been annotated , bind the class where the image views are present using the method **Cache.bind(object).**

You can pass the activity, fragement or the view to which the annotated ImageViews belong to.

In an activity, the method should be called after setContentView and in a fragemnt it should be called in **onViewCreated** method.

```kotlin
     override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.load_image_example)
            Cache.bind(this)
        }
```

Currently the cache can only be bound to an Activity , fragment or view.

## @Parent Annotation

An annotation that helps binding a nested view to a resource id.
Suppose you have a layout ** layout_1.xml ** which contains an ImageView. You have added this layout in your main layout using the
** <include/> ** tag.You can now use @LoadImage annotation on the ImageView by :

* Provide an id to the include tag

    ```xml
    <include android:id="@+id/my_included_layout"
    ......
    other attributes
    />
    ```

* Use Parent annotation along with LoadImage annotation

    ```kotlin
        @Parent(R.id.my_included_layout)
        @LoadImage(R.id.my_nested_image_view,"http://url.jpg"
        lateinit var childImageView : ImageView
    ```

## @Freeze Annotation

Annotate your class using the freeze annotation to apply caching logic on top of all the public methods present in the class.

```kotlin
@Freeze(generatedClassName = "MyBeautifulCacheLayer")
class MakeRemoteCallWithFreeze {

    
    fun makeRemoteCallToServiceA(value: String): String {
        val url = "https://httpbin.org/get?param1=$value"
        val textResponse = URL(url).readText()
        return textResponse
    }


    /**
     * Here I am marking the parameters that will together form the cache key
     * with @CacheKey
     */
    fun makeRemoteCallToServiceB(
        @CacheKey parameter1: String,
        @CacheKey parameter2: String,
        parameter3: String
    ): String {
        val url = "https://httpbin.org/get?param1=$parameter1&param2=$parameter2&param3=$parameter3"
        val textResponse = URL(url).readText()
        return textResponse
    }
}
```
This will generate a class called "**MyBeautifulCacheLayer**" . You can use this class to call the methods.
```kotlin 
//you need to implement the OnOperationSuccessfulCallback interface.
val callback = object : OnOperationSuccessfulCallback<String>{
        override fun onSuccess(output: String?, operation: String) {
            //handle the output here.
	    //operation is the name of the method that returns the output. In this case the output
	    //can be "makeRemoteCallToServiceB" or "makeRemoteCallToServiceA" . You can handle the output
	    //based on which method is returning it.
        }
    }

val cacheLayer = MyBeautifulCacheLayer()

cacheLayer.makeRemoteCallToServiceA("someString" , callback)

cacheLayer.makeRemoteCallToServiceB(.... )

```
## @Refrigerate Annotation
Annotate your functions with refrigerate to cache the output of the function for a given set of inputs.

```kotlin
    @Refrigerate(timeToLive : 2000, operation = "cacheImage")
    fun downloadImage(@CacheKey url : String , @CacheKey data : String , variableThatIsNotAKey : String) : Bitmap {
    .....
    }
```
This will keep the bitmap in the cache for 2 seconds.

 In the above example **url** and **data** will together form the key of the cache , such that if it determines that the same url and data is passed to the function (until the value expires in the cache) irrespective of the value of "**variableThatIsNotAKey**" it will 
 return the data from the cache.

During compilation the annotations will generate a class "**GeneratedCacheLayer**
and instead of using your annotated functions directly you will use them via this class.

To invoke the above functions you will call :-

```kotlin
    GeneratedCacheLayer.downloadImage("myurl", objectOfTheClassWhereTheMethodBelongs , callback)

    GeneratedCacheLayer.callRemoteService("myurl", "mydata" , "myrandomVariable" , objectOfTheClassWhereTheMethodBelongs , callback)
```

The generated method will have the same name and accept the same variables as the original method but with two extra parameters , one is the object of the class where the original annotated method is present in and the second is the callback (**OnOperationsuccessfulCallback**) which will be used to pass the cached data to the main thread from the background thread.

## Create a custom cache layer

 Create your cache layer by extending the **Cache class**. You will have to implement the update method.
 The update method should take care of fetching the data when the data is stale or is not present in the cache.
 
```kotlin
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
```

**The update method should return the value to be cached in form of a string. If you are planning to store complex objects , serialize them into a string and return them from the method.**

Your cache is now ready. To use the cache create an instance of it and call the **get** method of the cache.

The get method accepts the key that needs to be fetched from the cache and a callback which will be used to return the result to the main thread. **The cache performs all operations in a background thread and will never block the UI thread.**
You will need to implement the **OnValueFetchedCallback** interface and pass it to the **get** method of the cache. The cache will fetch the value and pass it to the callback method from where you can access it and use in the UI thread.

Optionally you can also pass a time to live value and a converter. They are explained in detail below.

```kotlin
    import android.graphics.Bitmap
    import android.os.Bundle
    import android.view.Menu
    import android.view.MenuItem
    import android.widget.Button
    import android.widget.ImageView
    import androidx.appcompat.app.AppCompatActivity
    import com.arcane.coldstorage.cache.ImageCache
    import com.arcane.coldstoragecache.callback.OnValueFetchedCallback
    import com.arcane.coldstoragecache.converter.impl.StringToBitmapConverter
    
    class MainActivity : AppCompatActivity(), OnValueFetchedCallback<Any?> {
    
        companion object {
            val URLS = arrayListOf(
                "https://images.unsplash.com/photo-1452857297128-d9c29adba80b?ixlib=rb-1.2.1&w=1000&q=80",
                "https://i.ytimg.com/vi/Pc20_oJQusc/maxresdefault.jpg",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS1HHodR1IgMESyE95LqwLRTRFnfCpmKKw5RQHqnP_kWV9ugKaiIQ&s"
            )
        }
    
        /**
         * An instance of image cache.
         */
        private val imageCache: ImageCache = ImageCache()
    
        /**
         * The image view where the images will be displayed.
         */
        private lateinit var imageView: ImageView
    
        /**
         * The button used to change the image.
         */
        private lateinit var changeButton: Button
    
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            imageView = findViewById(R.id.image_view)
            changeButton = findViewById(R.id.change)
            checkImageCaching()
            changeButton.setOnClickListener {
                checkImageCaching()
            }
        }
    
        /**
         * Method to test image caching.
         */
        private fun checkImageCaching() {
            val converter = StringToBitmapConverter()
            imageCache.get(
                URLS.shuffled().take(1)[0],
                this,
                converter
            )
    
        }
    
        /**
         * When the image is downloaded , adding the image to
         * the image view.
         */
        override fun valueFetched(output: Any?) {
            imageCache.commitToSharedPref(applicationContext)
            runOnUiThread {
                val outputAsBitmap = output as Bitmap
                imageView.setImageBitmap(outputAsBitmap)
            }
        }
    }
    
```

**The time to  live value in the get method to specify how long a data needs to be stored in the cache.**

**The converter object takes care of deserializing the string into the object you need. It is an optional parameter. If the converter is not passed the cache will return the value as string.**

# License
```
Copyright 2020 Anurag Mandal

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Author

Anurag Mandal  [LinkedIn]( https://www.linkedin.com/in/anurag-mandal-86a65b164/)

