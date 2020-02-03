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
import com.squareup.picasso.Picasso

/**
 * The mainactivity for the example app.
 * This shows the example of @LoadImage annotation.
 *
 * @author Anurag
 */
class MainActivity : AppCompatActivity(), OnValueFetchedCallback<Bitmap?> {


    /**
     * An instance of image cache.
     */
    private val imageCache: ImageCache = ImageCache()

    /**
     * The image view where the images will be displayed.
     */
    @LoadImage(
        R.id.image_view,
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS-GsnyKLBNYVfl0Uuq-PGreWRTwdAIRGL_BOtG24SayIS9HDwq&s"
    )
    lateinit var imageView: ImageView

    @LoadImage(
        R.id.image_view_2,
        "https://www.ddfl.org/wp-content/uploads/2018/03/bunnies-easter.png",
        R.drawable.test_load,
        enableLoadingAnimation = true
    )
    lateinit var imageView2: ImageView

    // @LoadImage("https://www.ddfl.org/wp-content/uploads/2018/03/bunnies-easter.png")
    lateinit var imageView3: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView3 = findViewById(R.id.imageView2)


        Cache.bind(this)

        Picasso.get()
            .load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS-GsnyKLBNYVfl0Uuq-PGreWRTwdAIRGL_BOtG24SayIS9HDwq&s")
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
