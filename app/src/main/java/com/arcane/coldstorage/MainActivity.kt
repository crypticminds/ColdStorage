package com.arcane.coldstorage

import android.content.Intent
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
import com.squareup.picasso.Picasso
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
    @LoadImage(
        "https://www.peta.org/wp-content/uploads/2010/06/iStock_000008440542XSmall1.jpg",
        R.drawable.ic_launcher_background
    )
    lateinit var imageView: ImageView

    @LoadImage(
        "https://www.ddfl.org/wp-content/uploads/2018/03/bunnies-easter.png",
        R.drawable.test_load,
        enableLoadingAnimation = true
    )
    lateinit var imageView2: ImageView

    // @LoadImage("https://www.ddfl.org/wp-content/uploads/2018/03/bunnies-easter.png")
    lateinit var imageView3: ImageView


    /**
     * The button used to change the image.
     */
    private lateinit var changeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.image_view)
        imageView2 = findViewById(R.id.image_view_2)
        imageView3 = findViewById(R.id.imageView2)


        Cache.bind(this)

        Picasso.get()
            .load("https://www.ddfl.org/wp-content/uploads/2018/03/bunnies-easter.png")
            .into(imageView3)

        findViewById<Button>(R.id.button).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
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
