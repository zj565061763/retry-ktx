[![](https://jitpack.io/v/zj565061763/retry-ktx.svg)](https://jitpack.io/#zj565061763/retry-ktx)

```kotlin
/**
 * 执行重试逻辑，如果[block]发生异常，会延迟[interval]之后继续调用[block]重试，
 * 如果达到最大重试次数[maxCount]，则返回的[Result]异常为[FRetryExceptionRetryMaxCount]并携带最后一次失败的异常，
 * 注意：[block]发生的[CancellationException]异常不会被捕获
 */
suspend fun <T> fRetry(
    /** 最多执行几次 */
    maxCount: Int = Int.MAX_VALUE,

    /** 执行间隔（毫秒） */
    interval: Long = 3_000,

    /** 执行回调 */
    block: suspend FRetryScope.() -> T,
): Result<T>


interface FRetryScope {
    /** 当前重试的次数 */
    val retryCount: Int
}

/**
 * 达到最大重试次数
 */
class FRetryExceptionRetryMaxCount(cause: Throwable) : Exception(cause)
```