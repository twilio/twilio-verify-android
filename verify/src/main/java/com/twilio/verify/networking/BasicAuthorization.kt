package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */
import android.util.Base64
import com.twilio.verify.data.encodeToUTF8String

internal const val AuthorizationHeader = "Authorization"
internal const val BasicAuth = "Basic"

internal data class BasicAuthorization constructor(
  private val username: String,
  private val password: String
) {
  val header: Pair<String, String>
    get() {
      val encodedAuthorization = encodeToUTF8String(
          "${this.username}:${this.password}".toByteArray(Charsets.UTF_8),
          Base64.NO_WRAP
      )
      return AuthorizationHeader to "$BasicAuth $encodedAuthorization"
    }
}