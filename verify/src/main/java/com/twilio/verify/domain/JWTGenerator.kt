/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain

import android.util.Base64
import android.util.Base64.NO_PADDING
import android.util.Base64.NO_WRAP
import android.util.Base64.URL_SAFE
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.domain.factor.DEFAULT_ALG
import org.json.JSONObject

internal const val ALGORITHM_KEY = "alg"
internal const val FLAGS = URL_SAFE or NO_PADDING or NO_WRAP

class JWTGenerator(private val keyStorage: KeyStorage) {
  fun generateJWT(
    alias: String,
    header: JSONObject,
    payload: JSONObject
  ): String {
    header.put(ALGORITHM_KEY, DEFAULT_ALG)
    val message = "${Base64.encodeToString(
        header.toString()
            .toByteArray(), FLAGS
    )}.${Base64.encodeToString(
        payload.toString()
            .toByteArray(), FLAGS
    )}"
    val signature = keyStorage.sign(alias, message, FLAGS)
    return "$message.$signature"
  }
}