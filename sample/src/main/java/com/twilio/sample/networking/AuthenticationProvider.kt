package com.twilio.sample.networking

import com.twilio.verify.Authentication
import com.twilio.verify.api.Action
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/*
 * Copyright (c) 2020, Twilio Inc.
 */
private const val identityKey = "identity"
private const val factorSidKey = "factorSid"
private const val challengeSidKey = "challengeSid"
private const val serviceSidKey = "serviceSid"
private const val actionKey = "action"
private const val tokenKey = "token"
internal const val authenticationEndpoint = "/auth"

class AuthenticationProvider(
  private val url: String,
  private val okHttpClient: OkHttpClient = okHttpClient()
) : Authentication {

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
      put(identityKey, identity)
      put(factorSidKey, factorSid)
      put(challengeSidKey, challengeSid)
      put(serviceSidKey, serviceSid)
      put(actionKey, action.value)
    }
    val request = Request.Builder()
        .url("$url$authenticationEndpoint")
        .post(jsonObject.toString().toRequestBody())
        .build()

    okHttpClient.newCall(request)
        .enqueue(object : Callback {
          override fun onResponse(
            call: Call,
            response: Response
          ) {
            try {
              response.takeIf { it.isSuccessful }
                  ?.body?.string()
                  ?.let { JSONObject(it) }
                  ?.getString(tokenKey)
                  ?.let { success(it) }
            } catch (e: Exception) {
              error(e)
            }
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