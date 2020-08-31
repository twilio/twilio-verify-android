package com.twilio.security.storage.key

import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.cipher.fromByteArray
import com.twilio.security.crypto.key.cipher.toByteArray
import com.twilio.security.crypto.key.template.CipherTemplate

class SecretKeyCipher(
  private val template: CipherTemplate,
  private val keyManager: KeyManager
) : SecretKeyProvider {

  override fun create() {
    keyManager.cipher(template.templateForCreation())
  }

  override fun encrypt(data: ByteArray): ByteArray {
    return keyManager.cipher(template)
      .encrypt(data)
      .let {
        toByteArray(it)
      }
  }

  override fun decrypt(data: ByteArray): ByteArray {
    val encryptedData = fromByteArray(data)
    return keyManager.cipher(template)
      .decrypt(encryptedData)
  }

  override fun delete() {
    keyManager.delete(template.alias)
  }
}
