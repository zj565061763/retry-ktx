package com.sd.lib.retry.ktx

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive

/**
 * 执行[block]，如果[block]发生异常，会延迟[interval]之后继续执行[block]，
 * 如果达到最大执行次数[maxCount]，则返回的[Result]异常为[FRetryExceptionMaxCount]并携带最后一次执行[block]的异常，
 * 注意：[block]抛出的[CancellationException]异常不会被捕获
 */
suspend fun <T> fRetry(
    /** 最大执行次数 */
    maxCount: Int = Int.MAX_VALUE,

    /** 执行间隔(毫秒) */
    interval: Long = 5_000,

    /** 执行回调 */
    block: suspend FRetryScope.() -> T,
): Result<T> {
    require(maxCount > 0)
    require(interval > 0)

    val scope = RetryScopeImpl()

    while (true) {
        // 增加次数
        scope.increaseCount()

        // block
        val result = runCatching {
            with(scope) { block() }
        }.onFailure { e ->
            // 如果是取消异常，则抛出
            if (e is CancellationException) throw e
        }

        currentCoroutineContext().ensureActive()
        if (result.isSuccess) {
            return result
        }

        if (scope.currentCount >= maxCount) {
            // 达到最大执行次数
            val cause = checkNotNull(result.exceptionOrNull())
            val exception = FRetryExceptionMaxCount(cause)
            return Result.failure(exception)
        } else {
            // 延迟后继续执行
            delay(interval)
            continue
        }
    }
}

interface FRetryScope {
    /** 当前执行次数 */
    val currentCount: Int
}

private class RetryScopeImpl : FRetryScope {
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