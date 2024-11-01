package com.sd.lib.retry.ktx

import com.sd.lib.network.fNetwork
import java.net.SocketTimeoutException

/**
 * 网络已连接的情况下才执行，执行逻辑参考[fRetry]
 */
suspend inline fun <T> fNetRetry(
   maxCount: Int = 3,
   getDelay: RetryScope.() -> Long = { 5_000 },
   onFailure: RetryScope.(Throwable) -> Boolean = { it is SocketTimeoutException },
   block: RetryScope.() -> T,
): Result<T> {
   return fRetry(
      maxCount = maxCount,
      getDelay = getDelay,
      onFailure = onFailure,
      block = {
         fNetwork()
         block()
      },
   )
}