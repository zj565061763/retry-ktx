package com.sd.lib.retry.ktx

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive

/**
 * 执行[block]，[block]发生的异常会被捕获([CancellationException]异常除外)并通知[onFailure]，[onFailure]的异常不会被捕获，
 * [block]发生异常之后，如果未达到最大执行次数[maxCount]，延迟[interval]之后继续执行[block]；
 * 如果达到最大执行次数[maxCount]，则返回的[Result]异常为[FRetryExceptionMaxCount]并携带最后一次的异常，
 * [beforeBlock]在[block]回调之前执行，[beforeBlock]发生的异常不会被捕获
 */
suspend fun <T> fRetry(
    /** 最大执行次数 */
    maxCount: Int = Int.MAX_VALUE,

    /** 执行间隔(毫秒) */
    interval: Long = 5_000,

    /** 失败回调 */
    onFailure: FRetryScope.(Throwable) -> Unit = {},

    /** 在[block]回调之前执行 */
    beforeBlock: suspend FRetryScope.() -> Unit = {},

    /** 执行回调 */
    block: suspend FRetryScope.() -> T,
): Result<T> {
    require(maxCount > 0)
    require(interval > 0)

    val scope = RetryScopeImpl()

    while (true) {
        // 增加次数
        scope.increaseCount()

        // before block
        with(scope) { beforeBlock() }
        currentCoroutineContext().ensureActive()

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

        val exception = checkNotNull(result.exceptionOrNull())
        with(scope) { onFailure(exception) }

        if (scope.currentCount >= maxCount) {
            // 达到最大执行次数
            return Result.failure(FRetryExceptionMaxCount(exception))
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