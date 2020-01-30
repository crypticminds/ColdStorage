

# ColdStorage [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) ![Downloads](https://jitpack.io/v/crypticminds/ColdStorage/month.svg)

**A lightweight caching library for android written in Kotlin.**

![Logo](https://i.imgur.com/rEE8hUO.jpg)

## Usage with annotation

> Check the post for an indepth tutorial on how to  use @Refrigerate
> annotation to cache data :-
> https://medium.com/@crypticmindscom_5258/caching-made-easy-in-android-with-kotlin-part-2-61bb476063b4
> Check out the post for usage of @Freeze annotation
> https://medium.com/@crypticmindscom_5258/caching-made-easy-on-android-with-kotlin-part-3-3d4cfcb57df0
> Examples can be found here :-
> https://github.com/crypticminds/coldstorageexamples

## Setup

* Add kotlin-kapt gradle plugin to **app build.gradle** file

	    apply plugin: "kotlin-kapt"

* Add the dependencies

        implementation "com.github.crypticminds.ColdStorage:coldstoragecache:3.0.1"  
	    kapt "com.github.crypticminds.ColdStorage:coldstoragecompiler:3.0.1"  
    	implementation "com.github.crypticminds.ColdStorage:coldstorageannotation:3.0.1"
    
     ***Check the latest release to get the newest features.***
     
 You need to initialize the cache when the application starts. The initialization takes care of pulling previously cached data and loading them into the memory . 
 
 -  Create an application class and initialize the cache in the onCreate() method.
 
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

-  Register your application in the android manifest file by providing the **android:name** attribute

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
```

## @Freeze Annotation

Annotate your class using the freeze annotation to apply caching logic on top of all the methods present in the class.

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

This will generate a class called "MyBeautifulCacheLayer" . You can use this class to call the methods.

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
Annotate your functions using this to keep the output of the function in the cache for a given set of inputs .

### For releases > 2.1.0 

Now you can use @CacheKey annotation to declare parameters as the keys of the cache

```kotlin
    @Refrigerate(timeToLive : 2000, operation = "cacheImage")
    fun downloadImage(@CacheKey url : String , @CacheKey someOtherVariable : String , variableThatIsNotAKey : String) : Bitmap {
    .....
    }
```

### For releases < 2.1.0 

If you are using the old version , you will need to use the keys parameter to define your cache keys.

```kotlin
    @Refrigerate(timeToLive : 2000, operation = "cacheImage" , keys=["url"])
    fun downloadImage(url : String) : Bitmap {
    .....
    }
```
This will keep the bitmap in the cache for a unique URL for 2 seconds.
The **keys** parameter will decide which parameters of the function will be the key of the cache.

```kotlin
    @Refrigerate(timeToLive : 20000, operation = "remoteServiceCall" , keys=["url","data"])
        fun callRemoteService(url : String, data : String , someRandomVariable : String) : String {
        .....
        }
```
 In the above example **url** and **data** will together form the key of the cache , such that if it determines that the same url and data is passed to the function (until the value expires in the cache) irrespective of the value of "**someRandomVariable**" it will 
 return the data from the cache. Using the **keys** parameter you can decide which variables should form the key of the cache for the annotated function.

During compilation the annotations will generate a class "**GeneratedCacheLayer**
and instead of using your annotated functions directly you will use them via this class.

To invoke the above functions you will call :-

```kotlin
    GeneratedCacheLayer.downloadImage("myurl", objectOfTheClassWhereTheMethodBelongs , callback)

    GeneratedCacheLayer.callRemoteService("myurl", "mydata" , "myrandomVariable" , objectOfTheClassWhereTheMethodBelongs , callback)
```


The generated method will have the same name and accept the same variables as the original method but with two extra parameters , one is the object of the class where the original annotated method is present in and the second is the callback (**OnOperationsuccessfulCallback**) which will be used to pass the cached data to the main thread from the background thread. (All cache operations take place on a separate thread). For more information about the usage check the example below or the [article](https://medium.com/@crypticmindscom_5258/caching-made-easy-in-android-with-kotlin-part-2-61bb476063b4) 

> After applying the annotation you can try running your application on AVD so that the GeneratedCacheLayer file is created. Then use it just like
> a regular class in your project. Your IDE will be able to index it
> after it is generated and you will be able see the parameters the generated functions will accept. The generated file will change when you change your annotated functions.

 

## Example 

https://github.com/crypticminds/coldstorageexamples


## Basic Usage (Without annotations)

Add the library to your build.gradle file

``` 
 implementation "com.github.crypticminds.ColdStorage:coldstoragecache:2.0.1"
 ```
 ***Check the latest release to get the newest features.***
 

    

   
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
	
	
**The update method should return the value to be cahced in form of a string. If you are planning to store complex objects , serialize them into a string and return them from the method.**

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


## Other usage

* You can update the cache manually using the **addToCache** method. Use this method if you need to update the cache from a sperate async task . You will need to pass the key , and the value (the value needs to be serializable). You can also pass an optional time to live value.
* You can persist your application cache into the shared preferences for future use by calling the method **commitToSharedPref** . 
* You can fetch the data from cache without it internally calling the update method if the data is stale or missing by using the method **getWithoutUpdate** . If you are using this method then you do not have to implement the update method of your cache. You will also have to manually fetch the data and update the cache using **addToCache** method when there is a cache miss.




