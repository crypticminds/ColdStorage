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

class CacheTest {

    private lateinit var cacheWithString: Cache

    private val testKey = "key"


    @Before
    fun resetValues() {
        cacheWithString = CacheWithStringValue()
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @Test
    fun testGetFromCacheWithCacheHit() {
        cacheWithString.addToCache(testKey, "testValue")
        val onValueFetchedCallback = object : OnValueFetchedCallback<String?> {
            override fun valueFetched(output: String?) {
                Assert.assertNotNull(output)
                Assert.assertEquals(output!!, "testValue")
            }
        }
        cacheWithString.get(testKey, onValueFetchedCallback)
    }

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

    @Test
    fun testInitializeMethod() {
        val allMap = hashMapOf<String, String>()
        val applicationContext = Mockito.mock(Context::class.java)
        val sharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(
            applicationContext.getSharedPreferences(
                Mockito.anyString(),
                Mockito.anyInt()
            )
        ).thenReturn(sharedPreferences)
        mockDataInSharedPref(sharedPreferences, allMap)
        Cache.initialize(applicationContext)
        Thread.sleep(1000)

        for (i in allMap.keys) {
            val value = cacheWithString.getWithoutUpdate(i)
            if (i.contains("Present")) {
                Assert.assertEquals(i, "TestObject", value)
            } else {
                Assert.assertNull(value)
            }

        }
    }


    private fun mockDataInSharedPref(
        sharedPreferences: SharedPreferences,
        allMap: HashMap<String, String>
    ) {

        val mapper = jacksonObjectMapper()
        for (i in 1..5) {
            val cachedDataModel = CachedDataModel(
                "TestObject",
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
                "TestObject",
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


    class CacheWithStringValue : Cache() {
        override fun update(key: String): String? {
            return "key2"
        }

    }


}