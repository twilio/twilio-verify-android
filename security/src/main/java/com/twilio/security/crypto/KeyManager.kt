/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import com.twilio.security.crypto.key.encrypter.Encrypter
import com.twilio.security.crypto.key.signer.Signer
import com.twilio.security.crypto.key.template.EncrypterTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import java.security.KeyStore

interface KeyManager {
  @Throws(KeyException::class)
  fun signer(template: SignerTemplate): Signer

  @Throws(KeyException::class)
  fun encrypter(template: EncrypterTemplate): Encrypter

  @Throws(KeyException::class)
  fun delete(alias: String)

  fun contains(alias: String): Boolean
}

internal const val providerName = "AndroidKeyStore"

fun keyManager(): KeyManager =
  AndroidKeyManager(KeyStore.getInstance(providerName).apply { load(null) }, providerName)