package com.arcane.coldstoragecache.cache

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ColdStorageTest {

    @Before
    fun resetValues() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @Test
    fun testCache() {
        Assert.assertNull(ColdStorage.get("key"))
        ColdStorage.put("key", "value", 1000)
        Assert.assertEquals(
            ColdStorage.get("key"), "value"
        )
        Thread.sleep(1000)
        Assert.assertNull(ColdStorage.get("key"))
    }
}