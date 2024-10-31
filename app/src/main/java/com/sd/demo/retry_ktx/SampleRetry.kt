package com.sd.demo.retry_ktx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.sd.demo.retry_ktx.theme.AppTheme
import com.sd.lib.network.fNetwork
import com.sd.lib.retry.ktx.fRetry
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

class SampleRetry : ComponentActivity() {
   private var _retryJob: Job? = null

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContent {
         AppTheme {
            ContentView(
               onClickStart = {
                  if (_retryJob == null) {
                     _retryJob = lifecycleScope.launch {
                        retry()
                     }
                  }
               },
               onClickCancel = {
                  cancelRetry()
               },
            )
         }
      }
   }

   private suspend fun retry() {
      val uuid = UUID.randomUUID().toString()
      logMsg { "$uuid start" }
      fRetry(
         maxCount = 5,
         getDelay = { 3_000 },
         onFailure = {
            logMsg { "onFailure:$it" }
            true
         },
      ) {
         logMsg { "retry $currentCount" }
         // 检查网络连接
         fNetwork()
         if (currentCount >= 4) {
            "hello"
         } else {
            error("failure $currentCount")
         }
      }.onSuccess { data ->
         logMsg { "$uuid onSuccess $data" }
      }.onFailure { error ->
         logMsg { "$uuid onFailure $error" }
      }
   }

   private fun cancelRetry() {
      _retryJob?.cancel()
      _retryJob = null
   }
}

@Composable
private fun ContentView(
   modifier: Modifier = Modifier,
   onClickStart: () -> Unit,
   onClickCancel: () -> Unit,
) {
   Column(
      modifier = modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
   ) {
      Button(onClick = onClickStart) {
         Text(text = "Start")
      }

      Button(onClick = onClickCancel) {
         Text(text = "Cancel")
      }
   }
}