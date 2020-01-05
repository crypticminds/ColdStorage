# ColdStorage

A lightweight caching library for android written in Kotlin.

## Usage

Add the library to your build.gradle file

``` 
implementation 'com.github.crypticminds:ColdStorage:1.0.1'
 ```
 ***Check the latest release to get the newest features.***
 
 You need to initialize the cache when the application starts. The initialization takes care of pulling previously cached data and loading them into the memory . 
 
 -  Create an application class and initialize the cache in the onCreate() method.
 

    
    import android.app.Application
    import com.arcane.coldstoragecache.cache.Cache
    
    class Application : Application() {
    
        /**
         * The cache needs to be initialized here allowing the
         * cache to pull any available data from shared preference and
         * load it into the memory.
         */
        override fun onCreate() {
            super.onCreate()
            Cache.initialize(context = applicationContext)
        }
    
    }
-  Register your application in the android manifest file by providing the **android:name** attribute


    <application
            android:allowBackup="true"
            android:icon="@mipmap/ic_launcher"
            android:label="@string/app_name"
            android:roundIcon="@mipmap/ic_launcher_round"
            android:supportsRtl="true"
            android:theme="@style/AppTheme"
            android:name=".application.Application">
    	
    </application>application>
 Create your cache layer by implementing the Cache class. You will have to implement the update method.
 The update method should take care of fetching the data when the data is stale or is not present in the cache.
 
 

 
    
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
	
	
**The update method should return the value to be cahced in form of a string. If you are planning to store complex objects , serialize them into a string and return them from the method.**

Your cache is now ready. To use the cache create an instance of it and call the **get** method of the cache.

The get method accepts the key that needs to be fetched from the cache and a callback which will be used to return the result to the main thread. **The cache performs all operations in a background thread and will never block the UI thread.**

Optionally you can also pass a time to live value and a converter. They are explained in detail below.



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
    

**The rime to  live value in the get method to specify how long a data needs to be stored in the cache.**

**The converter object takes care of deserializing the string into the object you need. It is an optional parameter. If the converter is not passed the cache will return the value as string.**


[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)




