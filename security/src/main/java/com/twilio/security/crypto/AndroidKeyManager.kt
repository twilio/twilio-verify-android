/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import com.twilio.security.crypto.key.encrypter.AESEncrypter
import com.twilio.security.crypto.key.encrypter.Encrypter
import com.twilio.security.crypto.key.signer.ECSigner
import com.twilio.security.crypto.key.signer.Signer
import com.twilio.security.crypto.key.template.AESGCMNoPaddingEncrypterTemplate
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.security.crypto.key.template.EncrypterTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AndroidKeyManager(
  private val keyStore: KeyStore,
  private val provider: String
) : KeyManager {

  @Throws(KeyException::class)
  override fun signer(template: SignerTemplate): Signer {
    try {
      val keyPair = if (!keyStore.containsAlias(template.alias)) {
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
    val privateKey =
      keyStore.getKey(alias, null) as? PrivateKey ?: throw IllegalArgumentException(
          "Private key not found"
      )
    val certificate = keyStore.getCertificate(alias) ?: throw IllegalArgumentException(
        "Certificate not found"
    )
    return KeyPair(certificate.publicKey, privateKey)
  }

  @Throws(KeyException::class)
  override fun encrypter(template: EncrypterTemplate): Encrypter {
    try {
      val key = if (!keyStore.containsAlias(template.alias)) {
        if (template.shouldExist) {
          throw IllegalStateException("The alias does not exist")
        }
        createEncrypterKey(template)
      } else {
        getEncrypterKey(template.alias)
      }
      return when (template) {
        is AESGCMNoPaddingEncrypterTemplate -> AESEncrypter(
            key, template.cipherAlgorithm, template.parameterSpecClass
        )
      }
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }

  @Throws(KeyException::class)
  override fun delete(alias: String) {
    try {
      if (keyStore.containsAlias(alias)) {
        keyStore.deleteEntry(alias)
      }
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }

  private fun getEncrypterKey(alias: String): SecretKey {
    return keyStore.getKey(
        alias, null
    ) as? SecretKey
        ?: throw IllegalArgumentException("Secret key not found")
  }

  private fun createEncrypterKey(template: EncrypterTemplate): SecretKey {
    val keyGenerator = KeyGenerator.getInstance(
        template.algorithm, provider
    )
    keyGenerator.init(template.keyGenParameterSpec)
    val key = keyGenerator.generateKey()
    return getEncrypterKey(
        template.alias
    ).takeIf {
      key != null && key == it
    } ?: throw IllegalArgumentException("New secret key not found")
  }
}