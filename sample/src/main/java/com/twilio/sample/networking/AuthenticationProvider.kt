package com.twilio.sample.networking

import com.twilio.verify.Authentication
import com.twilio.verify.api.Action
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class AuthenticationProvider(private val url: String) : Authentication {

  override fun generateJWE(
    identity: String,
    factorSid: String?,
    challengeSid: String?,
    serviceSid: String?,
    action: Action,
    success: (token: String) -> Unit,
    error: (Exception) -> Unit
  ) {
    val jsonObject = JSONObject().apply {
      put("identity", identity)
      put("factorSid", factorSid)
      put("challengeSid", challengeSid)
      put("serviceSid", serviceSid)
      put("action", action.value)
    }
    val request = Request.Builder()
        .url("$url/auth")
        .post(jsonObject.toString().toRequestBody())
        .build()
    val okHttpClient = okHttpClient()

    okHttpClient.newCall(request)
        .enqueue(object : Callback {
          override fun onResponse(
            call: Call,
            response: Response
          ) {
            response.takeIf { it.isSuccessful }
                ?.body?.string()
                ?.let { JSONObject(it) }
                ?.getString("token")
                ?.let { success(it) }
          }

          override fun onFailure(
            call: Call,
            e: IOException
          ) {
            error(e)
          }
        })
  }
}