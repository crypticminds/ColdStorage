package com.arcane.coldstoragecache.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CacheTest {

    @Test
    fun testCacheInit() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Cache.initialize(context = appContext)
    }
}