package com.twilio.security.storage.key

import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.encrypter.EncryptedData
import com.twilio.security.crypto.key.template.EncrypterTemplate
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

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
          var objectOutputStream: ObjectOutputStream? = null
          try {
            val outputStream = ByteArrayOutputStream()
            objectOutputStream = ObjectOutputStream(outputStream)
            objectOutputStream.writeObject(it)
            outputStream.toByteArray()
          } finally {
            objectOutputStream?.close()
          }
        }
  }

  override fun decrypt(data: ByteArray): ByteArray {
    val encryptedData = data.let {
      var objectInputStream: ObjectInputStream? = null
      try {
        val inputStream = ByteArrayInputStream(data)
        objectInputStream = ObjectInputStream(inputStream)
        objectInputStream.readObject() as EncryptedData
      } finally {
        objectInputStream?.close()
      }
    }
    return keyManager.encrypter(template)
        .decrypt(encryptedData)
  }

  override fun delete() {
    keyManager.delete(template.alias)
  }
}