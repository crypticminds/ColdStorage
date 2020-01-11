package com.arcane.coldstorage.application

//Add this line in the import.The class will be generated during code compilation.
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
        Cache.initialize(applicationContext)
    }

}