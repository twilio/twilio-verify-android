/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

import android.util.Base64.NO_WRAP
import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import com.twilio.security.crypto.keyManager
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.KeyStorageError

internal const val provider = "AndroidKeyStore"

internal class KeyStoreAdapter(private val manager: KeyManager = keyManager()) :
  KeyStorage {
  override fun create(alias: String): String {
    return try {
      encodeToBase64UTF8String(
        manager.signer(getSignerTemplate(alias, false))
          .getPublic(),
        NO_WRAP
      )
    } catch (e: Exception) {
      throw TwilioVerifyException(e, KeyStorageError)
    }
  }

  override fun sign(
    alias: String,
    message: String
  ): ByteArray {
    return try {
      manager.signer(getSignerTemplate(alias))
        .sign(message.toByteArray())
    } catch (e: Exception) {
      throw TwilioVerifyException(e, KeyStorageError)
    }
  }

  override fun signAndEncode(
    alias: String,
    message: String
  ): String {
    return try {
      encodeToBase64UTF8String(sign(alias, message), NO_WRAP)
    } catch (e: TwilioVerifyException) {
      throw e
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

internal fun getSignerTemplate(
  alias: String,
  shouldExist: Boolean = true
): SignerTemplate = ECP256SignerTemplate(alias, shouldExist = shouldExist)
