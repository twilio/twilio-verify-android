package com.twilio.sample

import com.twilio.sample.networking.okHttpClient
import com.twilio.verify.Authentication
import com.twilio.verify.models.FactorType
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class AuthenticationProvider : Authentication {
  override fun generateJWE(
    factorType: FactorType,
    factorConfig: Any?,
    identity: String,
    factorSid: String?,
    challengeSid: String?,
    success: (token: String) -> Unit,
    error: (Exception) -> Unit
  ) {
    val request = Request.Builder()
        .url("url")
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