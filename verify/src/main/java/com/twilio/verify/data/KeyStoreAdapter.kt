/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature

private const val provider = "AndroidKeyStore"

class KeyStoreAdapter : KeyStorage {
  override fun create(alias: String): String? {
    return try {
      val kpg = KeyPairGenerator.getInstance(
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

      kpg.initialize(parameterSpec)
      val keyPair = kpg.generateKeyPair()

      String(keyPair.public.encoded)
    } catch (e: Exception) {
      null
    }
  }

  override fun sign(
    alias: String,
    message: String
  ): String? {
    val ks = KeyStore.getInstance(provider)
        .apply {
          load(null)
        }
    val entry = ks.getEntry(alias, null)
    if (entry !is KeyStore.PrivateKeyEntry) {
      return null
    }
    return Signature.getInstance("SHA256withECDSA")
        .run {
          initSign(entry.privateKey)
          update(message.toByteArray())
          sign()
        }
        ?.let { String(it) }
  }
}