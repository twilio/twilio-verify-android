/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import com.twilio.security.crypto.key.cipher.Cipher
import com.twilio.security.crypto.key.signer.Signer
import com.twilio.security.crypto.key.template.CipherTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import java.security.KeyStore

interface KeyManager {
  @Throws(KeyException::class)
  fun signer(template: SignerTemplate): Signer

  @Throws(KeyException::class)
  fun cipher(template: CipherTemplate): Cipher

  @Throws(KeyException::class)
  fun delete(alias: String)

  fun contains(alias: String): Boolean
}

internal const val providerName = "AndroidKeyStore"

fun keyManager(): KeyManager =
  AndroidKeyManager(
      AndroidKeyStore(
          KeyStore.getInstance(providerName)
              .apply { load(null) }
      )
  )