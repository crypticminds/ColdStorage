package com.arcane.coldstoragecache.cache

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.arcane.coldstoragecache.callback.OnValueFetchedCallback
import com.arcane.coldstoragecache.model.CachedDataModel
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

/**
 * The test for cache layer.
 */
class CacheTest {

    private lateinit var cacheWithString: Cache

    private val testKey = "key"

    /**
     * Method tp reset all values in the cache.
     */
    @Before
    fun resetValues() {
        cacheWithString = CacheWithStringValue()
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        Cache.clearCache()
    }

    /**
     * Test cache hit.
     */
    @Test
    fun testGetFromCacheWithCacheHit() {
        Cache.addToCache(testKey, "testValue")
        val onValueFetchedCallback = object : OnValueFetchedCallback<String?> {
            override fun valueFetched(output: String?) {
                Assert.assertNotNull(output)
                Assert.assertEquals(output!!.toString(), "testValue")
            }
        }
        cacheWithString.get(testKey, onValueFetchedCallback)
    }

    /**
     * Test cache miss.
     */
    @Test
    fun testGetFromCacheWithCacheMiss() {
        val onValueFetchedCallback = object : OnValueFetchedCallback<String?> {
            override fun valueFetched(output: String?) {
                Assert.assertNotNull(output)
                Assert.assertEquals(output!!, "key2")
            }
        }
        cacheWithString.get(testKey, onValueFetchedCallback)
    }

    private val testObject = "TestObject"

    /**
     * Test for cache initialize.
     */
    @Test
    fun testInitializeMethod() {
        val allMap = hashMapOf<String, String>()
        val applicationContext = Mockito.mock(Context::class.java)
        val sharedPreferences = Mockito.mock(SharedPreferences::class.java)
        val imageSharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(
            applicationContext.getSharedPreferences(
                "coldstoragesharedpref",
                Context.MODE_PRIVATE
            )
        ).thenReturn(sharedPreferences)
        Mockito.`when`(
            applicationContext.getSharedPreferences(
                "image-pref",
                Context.MODE_PRIVATE
            )
        ).thenReturn(imageSharedPreferences)
        mockImagesDataInSharedPref(imageSharedPreferences)
        mockDataInSharedPref(sharedPreferences, allMap)
        Cache.initialize(applicationContext)
        Thread.sleep(1000)

        for (i in allMap.keys) {
            val value = cacheWithString.getWithoutUpdate(i)
            if (i.contains("Present")) {
                Assert.assertEquals(i, testObject, value)
            } else {
                Assert.assertNull(value)
            }
        }
    }

    /**
     * Test for checking commit to shared preferences.
     */
    @Test
    fun testCommitToSharedPreferences() {
        val allMap = hashMapOf<String, String>()
        val applicationContext = Mockito.mock(Context::class.java)
        val sharedPreferences = Mockito.mock(SharedPreferences::class.java)
        val editor = Mockito.mock(SharedPreferences.Editor::class.java)
        val imageSharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(
            applicationContext.getSharedPreferences(
                "coldstoragesharedpref",
                Context.MODE_PRIVATE
            )
        ).thenReturn(sharedPreferences)
        Mockito.`when`(
            applicationContext.getSharedPreferences(
                "image-pref",
                Context.MODE_PRIVATE
            )
        ).thenReturn(imageSharedPreferences)
        mockImagesDataInSharedPref(imageSharedPreferences)
        Mockito.`when`(sharedPreferences.edit()).thenReturn(editor)
        Mockito.`when`(editor.putString(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(editor)
        Mockito.doNothing().`when`(editor).apply()
        mockDataInSharedPref(sharedPreferences, allMap)
        Cache.initialize(applicationContext)
        Thread.sleep(500)
        cacheWithString.commitToSharedPref(applicationContext)
        Mockito.verify(sharedPreferences, Mockito.times(5))
            .edit()
    }

    private fun mockImagesDataInSharedPref(sharedPreferences: SharedPreferences) {
        Mockito.`when`(sharedPreferences.all).thenReturn(HashMap<String, Any?>())

    }


    private fun mockDataInSharedPref(
        sharedPreferences: SharedPreferences,
        allMap: HashMap<String, String>
    ) {

        val mapper = jacksonObjectMapper()
        for (i in 1..5) {
            val cachedDataModel = CachedDataModel(
                testObject,
                System.currentTimeMillis(), 2000
            )
            val modelAsString = mapper.writeValueAsString(cachedDataModel)
            Mockito.`when`(
                sharedPreferences.getString(
                    "Present$i",
                    ""
                )
            ).thenReturn(modelAsString)
            allMap["Present$i"] = modelAsString

        }
        for (i in 1..10) {
            val cachedDataModel = CachedDataModel(
                testObject,
                System.currentTimeMillis(), 10
            )
            val modelAsString = mapper.writeValueAsString(cachedDataModel)
            Mockito.`when`(
                sharedPreferences.getString(
                    "Absent$i",
                    ""
                )
            ).thenReturn(modelAsString)
            allMap["Absent$i"] = modelAsString
        }
        Mockito.`when`(sharedPreferences.all).thenReturn(allMap)
    }


    /**
     * An implementation of cache for testing purpose.
     */
    class CacheWithStringValue : Cache() {
        override fun update(key: String): String? {
            return "key2"
        }

    }


}