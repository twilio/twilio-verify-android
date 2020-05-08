package com.twilio.sample.networking

import com.twilio.sample.model.EnrollmentResponse
import com.twilio.verify.models.FactorType
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

  fun enrollment(
    url: String,
    identity: String,
    onSuccess: (EnrollmentResponse) -> Unit,
    onError: (Exception) -> Unit
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      try {
        val enrollmentResponse = enrollment(url, identity)
        onSuccess(enrollmentResponse)
      } catch (e: Exception) {
        onError(e)
      }
    }
  }

  suspend fun enrollment(
    url: String,
    identity: String,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
  ): EnrollmentResponse = withContext(dispatcher) {
    return@withContext suspendCancellableCoroutine<EnrollmentResponse> { cont ->
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
                  ?.let { cont.resume(toEnrollmentResponse(JSONObject(it))) }
                  ?: cont.resumeWithException(IOException("Invalid response: ${response.code}"))
            }
          })
    }
  }

  private fun toEnrollmentResponse(jsonResponse: JSONObject): EnrollmentResponse {
    val token = jsonResponse.getString("token")
    val serviceSid = jsonResponse.getString("serviceSid")
    val identity = jsonResponse.getString("identity")
    val factorType = jsonResponse.getString("factorType")
    return EnrollmentResponse(
        token, serviceSid, identity,
        FactorType.values().associateBy(FactorType::factorTypeName)[factorType]
            ?: throw IllegalArgumentException("Invalid response")
    )
  }
}