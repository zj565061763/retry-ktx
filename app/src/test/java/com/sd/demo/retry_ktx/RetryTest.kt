package com.sd.demo.retry_ktx

import com.sd.lib.retry.ktx.FRetryExceptionMaxCount
import com.sd.lib.retry.ktx.fRetry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RetryTest {
    @Test
    fun `test success`(): Unit = runBlocking {
        val result = fRetry {
            "success"
        }
        assertEquals("success", result.getOrThrow())
    }

    @Test
    fun `test error`(): Unit = runBlocking {
        val result = fRetry<String>(
            maxCount = 3,
            interval = 100,
        ) {
            error("error")
        }
        val exception = result.exceptionOrNull() as FRetryExceptionMaxCount
        assertEquals("error", exception.cause?.message)
    }

    @Test
    fun `test cancel`(): Unit = runBlocking {
        val job = launch {
            fRetry { throw CancellationException() }
        }.also {
            it.join()
        }
        assertEquals(true, job.isCompleted)
        assertEquals(true, job.isCancelled)
    }

    @Test
    fun `test count`(): Unit = runBlocking {
        val events = mutableListOf<String>()
        fRetry<String>(
            maxCount = 3,
            interval = 100,
        ) {
            events.add(currentCount.toString())
            error("error")
        }
        assertEquals(listOf("1", "2", "3"), events)
    }
}