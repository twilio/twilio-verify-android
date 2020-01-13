package com.twilio.verify.networking

import android.util.Base64

private const val AuthorizationHeader = "Authorization"

data class Authorization internal constructor(private val accountSid: String, private val authToken: String) {
    val header: Pair<String, String>
        get() {
            val encodedAuthorization = Base64.encodeToString(
                "${this.accountSid}:${this.authToken}".toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )
            return AuthorizationHeader to "Basic $encodedAuthorization"
        }
}