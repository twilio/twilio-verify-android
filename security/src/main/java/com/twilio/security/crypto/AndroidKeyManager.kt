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
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry
import java.security.KeyStore.SecretKeyEntry
import javax.crypto.KeyGenerator

class AndroidKeyManager(
  private val keyStore: KeyStore,
  private val provider: String = keyStore.provider.name
) : KeyManager {

  @Throws(KeyException::class)
  override fun signer(template: SignerTemplate): Signer {
    try {
      val entry = if (!keyStore.containsAlias(template.alias)) {
        createSignerKeyPair(template)
      } else {
        getPrivateKeyEntry(template.alias)
      }
      return when (template) {
        is ECP256SignerTemplate -> ECSigner(entry, template.signatureAlgorithm)
      }
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }

  private fun createSignerKeyPair(template: SignerTemplate): PrivateKeyEntry {
    val keyPairGenerator = KeyPairGenerator.getInstance(
        template.algorithm, provider
    )
    keyPairGenerator.initialize(template.keyGenParameterSpec)
    val keyPair = keyPairGenerator.generateKeyPair()
    return getPrivateKeyEntry(
        template.alias
    ).takeIf { keyPair?.public?.encoded?.contentEquals(it.certificate.publicKey.encoded) == true }
        ?: run { throw IllegalArgumentException("New private key entry not found") }
  }

  private fun getPrivateKeyEntry(alias: String): PrivateKeyEntry {
    return keyStore.getEntry(alias, null) as? PrivateKeyEntry?
        ?: run { throw IllegalArgumentException("Private key entry not found") }
  }

  @Throws(KeyException::class)
  override fun encrypter(template: EncrypterTemplate): Encrypter {
    try {
      val entry = if (!keyStore.containsAlias(template.alias)) {
        createEncrypterKey(template)
      } else {
        getSecretKeyEntry(template.alias)
      }
      return when (template) {
        is AESGCMNoPaddingEncrypterTemplate -> AESEncrypter(
            entry, template.cipherAlgorithm, template.parameterSpecClass
        )
      }
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }

  private fun getSecretKeyEntry(alias: String): SecretKeyEntry {
    return keyStore.getEntry(
        alias, null
    ) as? SecretKeyEntry
        ?: run { throw IllegalArgumentException("Secret key entry not found") }
  }

  private fun createEncrypterKey(template: EncrypterTemplate): SecretKeyEntry {
    val keyGenerator = KeyGenerator.getInstance(
        template.algorithm, provider
    )
    keyGenerator.init(template.keyGenParameterSpec)
    val key = keyGenerator.generateKey()
    return getSecretKeyEntry(
        template.alias
    ).takeIf {
      key != null && key == it.secretKey
    } ?: run { throw IllegalArgumentException("New secret key entry not found") }
  }
}