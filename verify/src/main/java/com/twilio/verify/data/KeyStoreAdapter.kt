/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

import android.util.Base64
import android.util.Base64.NO_WRAP
import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.security.crypto.keyManager
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.KeyStorageError

internal const val provider = "AndroidKeyStore"

internal class KeyStoreAdapter(private val manager: KeyManager = keyManager()) :
    KeyStorage {
  override fun create(alias: String): String {
    return try {
      Base64.encodeToString(manager.signer(ECP256SignerTemplate(alias)).getPublic(), NO_WRAP)
    } catch (e: Exception) {
      throw TwilioVerifyException(e, KeyStorageError)
    }
  }

  override fun sign(
    alias: String,
    message: String
  ): String {
    return try {
      String(
          manager.signer(ECP256SignerTemplate(alias, shouldExist = true)).sign(
              message.toByteArray()
          )
      )
    } catch (e: Exception) {
      throw TwilioVerifyException(e, KeyStorageError)
    }
  }

  override fun delete(alias: String) {
    try {
      manager.delete(alias)
    } catch (e: Exception) {
      throw TwilioVerifyException(e, KeyStorageError)
    }
  }
}