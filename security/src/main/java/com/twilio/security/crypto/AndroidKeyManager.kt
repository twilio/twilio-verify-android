/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import com.twilio.security.crypto.key.cipher.AESCipher
import com.twilio.security.crypto.key.cipher.Cipher
import com.twilio.security.crypto.key.signer.ECSigner
import com.twilio.security.crypto.key.signer.Signer
import com.twilio.security.crypto.key.template.AESGCMNoPaddingCipherTemplate
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.security.crypto.key.template.CipherTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.util.concurrent.TimeUnit
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AndroidKeyManager(
  private val keyStore: KeyStore,
  override val provider: String
) : KeyManager {

  @Throws(KeyException::class)
  override fun signer(template: SignerTemplate): Signer {
    try {
      val keyPair = if (!contains(template.alias)) {
        if (template.shouldExist) {
          throw IllegalStateException("The alias does not exist")
        }
        createSignerKeyPair(template)
      } else {
        getSignerKeyPair(template.alias)
      }
      return when (template) {
        is ECP256SignerTemplate -> ECSigner(keyPair, template.signatureAlgorithm)
      }
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }

  private fun createSignerKeyPair(template: SignerTemplate): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance(
        template.algorithm, provider
    )
    keyPairGenerator.initialize(template.keyGenParameterSpec)
    val keyPair = keyPairGenerator.generateKeyPair()
    return getSignerKeyPair(
        template.alias
    ).takeIf { keyPair?.public?.encoded?.contentEquals(it.public.encoded) == true }
        ?: throw IllegalArgumentException("New private key not found")
  }

  private fun getSignerKeyPair(alias: String): KeyPair {
    val privateKey = retryToGetValue { getPrivateKey(alias) } ?: throw IllegalArgumentException(
        "Private key not found"
    )
    val certificate = retryToGetValue { getCertificate(alias) } ?: throw IllegalArgumentException(
        "Certificate not found"
    )
    return KeyPair(certificate.publicKey, privateKey)
  }

  private fun getCertificate(alias: String): Certificate? {
    if (!contains(alias)) {
      throw IllegalArgumentException("alias not found")
    }
    return keyStore.getCertificate(alias)
  }

  private fun getPrivateKey(alias: String): PrivateKey? {
    if (!contains(alias)) {
      throw IllegalArgumentException("alias not found")
    }
    return keyStore.getKey(alias, null) as? PrivateKey
  }

  @Throws(KeyException::class)
  override fun cipher(template: CipherTemplate): Cipher {
    try {
      val key = if (!contains(template.alias)) {
        if (template.shouldExist) {
          throw IllegalStateException("The alias does not exist")
        }
        createCipherKey(template)
      } else {
        getCipherKey(template.alias)
      }
      return when (template) {
        is AESGCMNoPaddingCipherTemplate -> AESCipher(key, template.cipherAlgorithm)
      }
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }

  @Throws(KeyException::class)
  override fun delete(alias: String) {
    try {
      if (contains(alias)) {
        keyStore.deleteEntry(alias)
      }
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }

  override fun contains(alias: String): Boolean = keyStore.containsAlias(alias)

  private fun getCipherKey(alias: String): SecretKey {
    return retryToGetValue { getSecretKey(alias) } ?: throw IllegalArgumentException(
        "Secret key not found"
    )
  }

  private fun getSecretKey(alias: String): SecretKey? {
    if (!contains(alias)) {
      throw IllegalArgumentException("alias not found")
    }
    return keyStore.getKey(alias, null) as? SecretKey
  }

  private fun createCipherKey(template: CipherTemplate): SecretKey {
    val keyGenerator = KeyGenerator.getInstance(
        template.algorithm, provider
    )
    keyGenerator.init(template.keyGenParameterSpec)
    val key = keyGenerator.generateKey()
    return getCipherKey(
        template.alias
    ).takeIf {
      key != null && key == it
    } ?: throw IllegalArgumentException("New secret key not found")
  }

  private fun <T> retryToGetValue(
    times: Int = 3,
    delayInMillis: Long = 100,
    block: () -> T?
  ): T? {
    repeat(times - 1) {
      val result = block()
      if (result == null) {
        TimeUnit.MILLISECONDS.sleep(delayInMillis)
      } else {
        return result
      }
    }
    return block()
  }
}