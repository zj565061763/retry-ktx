package com.sd.demo.retry_ktx

import com.sd.lib.retry.ktx.RetryMaxCountException
import com.sd.lib.retry.ktx.fRetry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RetryTest {
   @Test
   fun `test success`() = runTest {
      val result = fRetry {
         "success"
      }
      assertEquals("success", result.getOrThrow())
   }

   @Test
   fun `test error`() = runTest {
      val result = fRetry(maxCount = 99) {
         error("error $currentCount")
      }
      val exception = result.exceptionOrNull() as RetryMaxCountException
      assertEquals("error 99", exception.cause?.message)
   }

   @Test
   fun `test cancel`() = runTest {
      launch {
         fRetry {
            throw CancellationException()
         }
      }.also { job ->
         job.join()
         assertEquals(true, job.isCompleted)
         assertEquals(true, job.isCancelled)
      }
   }

   @Test
   fun `test count`() = runTest {
      val events = mutableListOf<String>()
      fRetry(maxCount = 5) {
         events.add(currentCount.toString())
         error("error")
      }
      assertEquals("1|2|3|4|5", events.joinToString("|"))
   }

   @Test
   fun `test onFailure`() = runTest {
      val events = mutableListOf<String>()
      fRetry(
         maxCount = 3,
         onFailure = {
            assertEquals(true, it.message == "error $currentCount")
            events.add(it.message!!)
         },
      ) {
         error("error $currentCount")
      }
      assertEquals("error 1|error 2|error 3", events.joinToString("|"))
   }
}