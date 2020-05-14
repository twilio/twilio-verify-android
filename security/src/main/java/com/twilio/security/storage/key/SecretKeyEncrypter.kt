package com.twilio.security.storage.key

import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.encrypter.fromByteArray
import com.twilio.security.crypto.key.encrypter.toByteArray
import com.twilio.security.crypto.key.template.EncrypterTemplate

internal class SecretKeyEncrypter(
  private val template: EncrypterTemplate,
  private val keyManager: KeyManager
) : SecretKeyProvider {

  override fun create() {
    keyManager.encrypter(template.templateForCreation())
  }

  override fun encrypt(data: ByteArray): ByteArray {
    return keyManager.encrypter(template)
        .encrypt(data)
        .let {
          toByteArray(it)
        }
  }

  override fun decrypt(data: ByteArray): ByteArray {
    val encryptedData = fromByteArray(data)
    return keyManager.encrypter(template)
        .decrypt(encryptedData)
  }

  override fun delete() {
    keyManager.delete(template.alias)
  }
}