/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import com.twilio.security.crypto.key.encryptor.Encryptor
import com.twilio.security.crypto.key.signer.Signer
import com.twilio.security.crypto.key.template.EncryptorTemplate
import com.twilio.security.crypto.key.template.SignerTemplate

interface KeyManager {
  @Throws(KeyException::class)
  fun signer(template: SignerTemplate): Signer

  @Throws(KeyException::class)
  fun encryptor(template: EncryptorTemplate): Encryptor
}

fun keyManager(): KeyManager = AndroidKeyManager()