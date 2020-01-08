package com.arcane.coldstoragecache.cache

import android.util.Log
import com.arcane.coldstoragecache.callback.OnOperationSuccessfulCallback
import com.arcane.coldstoragecache.helper.CacheHelper
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class HigherOrderCachingTest {

    private val cacheHelper = CacheHelper()


    @Before
    fun resetValues() {
        MockitoAnnotations.initMocks(this)
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        Cache.clearCache()
    }

    @Test
    fun testHigherOrderCaching() {
        someMethodDoingLongRunningWork()
        someMethodDoingLongRunningWork()
    }

    private fun someMethodDoingLongRunningWork() {
        val lambda = cacheHelper::longRunningFunctionThatReturnsAValue
        ColdStorage.cache(
            lambda,
            "",
            object : OnOperationSuccessfulCallback<String> {
                override fun operationSuccessful(output: String, operation: String) {
                    System.out.println(output)
                    System.out.println(operation)
                }
            })
        Thread.sleep(2000)
    }

}