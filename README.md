[![](https://jitpack.io/v/zj565061763/retry-ktx.svg)](https://jitpack.io/#zj565061763/retry-ktx)

```kotlin
/**
 * 执行[block]，如果[block]发生异常，会延迟[interval]之后继续执行[block]，
 * 如果达到最大执行次数[maxCount]，则返回的[Result]异常为[FRetryExceptionMaxCount]并携带最后一次失败的异常，
 * 注意：[block]抛出的[CancellationException]异常不会被捕获
 */
suspend fun <T> fRetry(
    /** 最多执行次数 */
    maxCount: Int = Int.MAX_VALUE,

    /** 执行间隔(毫秒) */
    interval: Long = 5_000,

    /** 执行回调 */
    block: suspend FRetryScope.() -> T,
): Result<T>
```

```kotlin
interface FRetryScope {
    /** 当前执行次数 */
    val retryCount: Int
}
```

```kotlin
/**
 * 达到最大执行次数
 */
class FRetryExceptionRetryMaxCount(cause: Throwable) : Exception(cause)
```