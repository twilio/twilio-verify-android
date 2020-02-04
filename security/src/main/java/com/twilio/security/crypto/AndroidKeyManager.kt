/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import com.twilio.security.crypto.key.encryptor.Encryptor
import com.twilio.security.crypto.key.signer.Signer
import com.twilio.security.crypto.key.template.EncryptorTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import java.security.KeyStore

internal const val provider = "AndroidKeyStore"

class AndroidKeyManager(
  private val keyStore: KeyStore = KeyStore.getInstance(provider)
) : KeyManager {
  init {
    keyStore.load(null)
  }

  @Throws(KeyException::class)
  override fun signer(template: SignerTemplate): Signer {
    TODO(
        "not implemented"
    ) //To change body of created functions use File | Settings | File Templates.
  }

  @Throws(KeyException::class)
  override fun encryptor(template: EncryptorTemplate): Encryptor {
    TODO(
        "not implemented"
    ) //To change body of created functions use File | Settings | File Templates.
  }
}