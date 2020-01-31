package com.arcane.coldstorage.application

import android.app.Application
import com.arcane.coldstoragecache.cache.Cache

/**
 * The main application for the example app.
 */
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