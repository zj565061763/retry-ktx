package com.sd.lib.retry.ktx

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive

/**
 * 执行[block]，[block]发生异常之后会被捕获并通知[onFailure] ([CancellationException]异常除外)，
 * 如果[onFailure]返回false则停止重试并返回失败结果；
 * 如果[onFailure]返回true则继续执行后面的逻辑，如果未达到最大执行次数[maxCount]，延迟[getInterval]之后继续执行[block]；
 * 如果达到最大执行次数[maxCount]，则返回失败结果，异常为[FRetryExceptionMaxCount]并携带最后一次的异常。
 */
suspend inline fun <T> fRetry(
   /** 最大执行次数 */
   maxCount: Int = 3,

   /** 获取执行间隔(毫秒) */
   getInterval: FRetryScope.() -> Long = { 5_000 },

   /** 失败回调，返回false停止重试 */
   onFailure: FRetryScope.(Throwable) -> Boolean = { true },

   /** 执行回调 */
   block: FRetryScope.() -> T,
): Result<T> {
   require(maxCount > 0)
   with(RetryScopeImpl()) {
      while (true) {
         // 增加次数
         increaseCount()

         val result = runCatching {
            block()
         }.onFailure { e ->
            // 如果是取消异常，则抛出
            if (e is CancellationException) throw e
         }

         currentCoroutineContext().ensureActive()
         if (result.isSuccess) {
            return result
         }

         val exception = checkNotNull(result.exceptionOrNull())
         val shouldContinue = onFailure(exception).also { currentCoroutineContext().ensureActive() }
         if (!shouldContinue) {
            return result
         }

         if (currentCount >= maxCount) {
            // 达到最大执行次数
            return Result.failure(FRetryExceptionMaxCount(exception))
         } else {
            // 延迟后继续执行
            delay(getInterval())
            continue
         }
      }
   }
}

interface FRetryScope {
   /** 当前执行次数 */
   val currentCount: Int
}

@PublishedApi
internal class RetryScopeImpl : FRetryScope {
   private var _count = 0

   override val currentCount: Int
      get() = _count

   fun increaseCount() {
      _count++
   }
}

/**
 * 达到最大执行次数
 */
class FRetryExceptionMaxCount(cause: Throwable) : Exception(cause)