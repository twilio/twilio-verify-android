package com.twilio.security.storage

import android.content.SharedPreferences
import android.util.Base64
import android.util.Base64.DEFAULT
import com.twilio.security.storage.key.SecretKeyProvider
import java.io.*
import kotlin.reflect.KClass

class EncryptedPreferences(
  override val secretKeyProvider: SecretKeyProvider,
  private val preferences: SharedPreferences
) : EncryptedStorage {
  @Throws(StorageException::class)
  override fun <T : Serializable> put(
    key: String,
    value: T
  ) {
    var objectOutputStream: ObjectOutputStream? = null
    try {
      val outputStream = ByteArrayOutputStream()
      objectOutputStream = ObjectOutputStream(outputStream)
      objectOutputStream.writeObject(value)
      val rawValue = outputStream.toByteArray()
      val encrypted = secretKeyProvider.encrypt(rawValue)
      preferences.edit()
          .putString(key, Base64.encodeToString(encrypted, DEFAULT))
          .apply()
    } catch (e: Exception) {
      throw StorageException(e)
    } finally {
      objectOutputStream?.close()
    }
  }

  @Throws(StorageException::class)
  override fun <T : Serializable> get(
    key: String,
    kClass: KClass<T>
  ): T {
    return try {
      getValue(key, kClass) ?: throw IllegalArgumentException(
          "Illegal decrypted data"
      )
    } catch (e: Exception) {
      throw StorageException(e)
    }
  }

  @Throws(StorageException::class)
  override fun <T : Serializable> getAll(kClass: KClass<T>): Map<String, T> =
    preferences.all.filterValues { it is String }.mapNotNull { entry ->
      getValue(
          entry.key, kClass
      )?.let { entry.key to it }
    }.toMap()

  override fun contains(key: String): Boolean = preferences.contains(key)

  override fun remove(key: String) {
    preferences.edit()
        .remove(key)
        .apply()
  }

  override fun clear() {
    preferences.edit()
        .clear()
        .apply()
  }

  private fun <T : Serializable> getValue(
    key: String,
    kClass: KClass<T>
  ): T? {
    var objectInputStream: ObjectInputStream? = null
    try {
      return preferences.getString(key, null)
          ?.let {
            secretKeyProvider.decrypt(Base64.decode(it, DEFAULT))
                .let { decryptedData ->
                  val inputStream = ByteArrayInputStream(decryptedData)
                  objectInputStream = ObjectInputStream(inputStream)
                  val value = objectInputStream?.readObject()
                  if (!kClass.javaObjectType.isInstance(value)) {
                    return null
                  }
                  value as? T ?: throw IllegalArgumentException(
                      "Illegal decrypted data"
                  )
                }
          } ?: throw IllegalArgumentException("key not found")
    } finally {
      objectInputStream?.close()
    }
  }
}