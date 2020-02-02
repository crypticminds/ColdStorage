package com.arcane.coldstorage

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.arcane.coldstorage.cache.ImageCache
import com.arcane.coldstorageannotation.LoadImage
import com.arcane.coldstoragecache.cache.Cache
import com.arcane.coldstoragecache.callback.OnValueFetchedCallback
import com.arcane.coldstoragecache.converter.impl.StringToBitmapConverter
import kotlin.random.Random

/**
 * The mainactivity for the example app.
 * This shows the example of a custom cache layer.
 *
 * @author Anurag
 */
class MainActivity : AppCompatActivity(), OnValueFetchedCallback<Bitmap?> {

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
    @LoadImage("https://images.unsplash.com/photo-1452857297128-d9c29adba80b?ixlib=rb-1.2.1&w=1000&q=80")
    lateinit var imageView: ImageView

    /**
     * The button used to change the image.
     */
    private lateinit var changeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.image_view)
        changeButton = findViewById(R.id.change)
//        checkImageCaching()
//        changeButton.setOnClickListener {
//            checkImageCaching()
//        }
        Cache.bind(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun checkImageCaching() {
        val converter = StringToBitmapConverter()
        imageCache.get(
            URLS.shuffled(Random(10)).take(1)[0],
            this,
            converter
        )

    }

    /**
     * When the image is downloaded , adding the image to
     * the image view.
     */
    override fun valueFetched(output: Bitmap?) {
        imageCache.commitToSharedPref(applicationContext)
        runOnUiThread {
            val outputAsBitmap = output as Bitmap
            imageView.setImageBitmap(outputAsBitmap)
        }
    }
}
