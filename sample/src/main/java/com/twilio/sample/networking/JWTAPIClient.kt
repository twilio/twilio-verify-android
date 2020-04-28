package com.twilio.sample.networking

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody.Builder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/*
 * Copyright (c) 2020, Twilio Inc.
 */
class SampleBackendAPIClient(private val okHttpClient: OkHttpClient) {

  fun getJwt(
    url: String,
    identity: String,
    onSuccess: (String) -> Unit,
    onError: (Exception) -> Unit
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      try {
        val jwt: String = getJwt(url, identity)
        onSuccess(jwt)
      } catch (e: Exception) {
        onError(e)
      }
    }
  }

  suspend fun getJwt(
    url: String,
    identity: String,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
  ): String = withContext(dispatcher) {
    return@withContext suspendCancellableCoroutine<String> { cont ->
      val request = Request.Builder()
          .url("$url/auth")
          .post(Builder().add("identity", identity).build())
          .build()
      okHttpClient.newCall(request)
          .enqueue(object : Callback {
            override fun onFailure(
              call: Call,
              e: IOException
            ) {
              cont.resumeWithException(e)
            }

            override fun onResponse(
              call: Call,
              response: Response
            ) {
              response.takeIf { it.isSuccessful }
                  ?.body?.string()
                  ?.let { JSONObject(it) }
                  ?.getString("token")
                  ?.let { cont.resume(it) }
                  ?: cont.resumeWithException(IOException("Invalid response: ${response.code}"))
            }

          })
    }
  }
}