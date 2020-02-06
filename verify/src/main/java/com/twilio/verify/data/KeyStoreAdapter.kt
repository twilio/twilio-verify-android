/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Base64.NO_WRAP
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.KeyStorageError
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature

internal const val provider = "AndroidKeyStore"

internal class KeyStoreAdapter(private val androidKeyStore: AndroidKeyStore = AndroidKeyStore()) :
    KeyStorage {
  override fun create(alias: String): String {
    return try {
      Base64.encodeToString(androidKeyStore.createKeyPair(alias).public.encoded, NO_WRAP)
    } catch (e: Exception) {
      throw TwilioVerifyException(KeyStorageException(e), KeyStorageError)
    }
  }

  override fun sign(
    alias: String,
    message: String
  ): String {
    return try {
      String(androidKeyStore.sign(alias, message))
    } catch (e: Exception) {
      throw TwilioVerifyException(KeyStorageException(e), KeyStorageError)
    }
  }
}

class KeyStorageException private constructor(
  message: String?,
  exception: Exception?
) : Exception(message, exception) {
  constructor(exception: Exception) : this(exception.message, exception)
  constructor(message: String) : this(message, null)
}

class AndroidKeyStore {
  fun createKeyPair(alias: String): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_EC,
        provider
    )
    val parameterSpec = KeyGenParameterSpec.Builder(
        alias,
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
    )
        .run {
          setDigests(KeyProperties.DIGEST_SHA256)
          build()
        }

    keyPairGenerator.initialize(parameterSpec)
    return keyPairGenerator.generateKeyPair()
  }

  fun sign(
    alias: String,
    message: String
  ): ByteArray {
    val ks = KeyStore.getInstance(provider)
        .apply {
          load(null)
        }
    val entry = ks.getEntry(alias, null)
    if (entry !is KeyStore.PrivateKeyEntry) {
      throw IllegalArgumentException("Key entry not found for alias")
    }
    return Signature.getInstance("SHA256withECDSA")
        .run {
          initSign(entry.privateKey)
          update(message.toByteArray())
          sign()
        }
  }
}