package com.sd.lib.retry.ktx

import com.sd.lib.network.FNetwork
import com.sd.lib.network.fNetwork
import java.net.SocketTimeoutException

/**
 * 网络已连接的情况下才执行，执行逻辑参考[fRetry]
 */
suspend fun <T> fNetRetry(
   maxCount: Int = 3,
   getDelay: RetryScope.() -> Long = { 5_000 },
   onFailure: RetryScope.(Throwable) -> Boolean = { shouldRetry(it) },
   block: suspend RetryScope.() -> T,
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

private fun shouldRetry(throwable: Throwable): Boolean {
   if (throwable is SocketTimeoutException) return true
   return !FNetwork.currentNetwork.isConnected
}