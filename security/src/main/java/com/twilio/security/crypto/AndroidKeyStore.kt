/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.security.crypto

import android.security.keystore.KeyGenParameterSpec
import com.twilio.security.crypto.key.cipher.AlgorithmParametersSpec
import com.twilio.security.crypto.key.cipher.EncryptedData
import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
import java.security.AlgorithmParameters
import java.security.Key
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry
import java.security.KeyStore.SecretKeyEntry
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.cert.Certificate
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AndroidKeyStore(
  private val keyStore: KeyStore
) : AndroidKeyStoreOperations {

  fun contains(alias: String): Boolean = keyStore.containsAlias(alias)

  @Synchronized
  fun deleteEntry(alias: String) {
    keyStore.deleteEntry(alias).also { Logger.log(Level.Debug, "Deleted entry for $alias") }
  }

  fun getSecretKey(alias: String): SecretKey? {
    return keyStore.getKey(alias, null) as? SecretKey ?: run {
      val entry = keyStore.getEntry(alias, null)
      if (entry !is SecretKeyEntry) {
        throw IllegalStateException("Entry is not a secret key entry")
      }
      entry.secretKey
    }.also { Logger.log(Level.Debug, "Return secret key for $alias") }
  }

  fun getKeyPair(alias: String): KeyPair? {
    val privateKey = getPrivateKey(alias)
    val certificate = getCertificate(alias)
    return if (privateKey != null && certificate != null) {
      KeyPair(certificate.publicKey, privateKey)
    } else {
      Logger.log(
        Level.Debug,
        "Private key ${if (privateKey == null) "is null" else "is not null"} and " +
          "Certificate ${if (certificate == null) "is null" else "is not null"}"
      )
      null
    }.also { Logger.log(Level.Debug, "Return key pair for $alias") }
  }

  @Synchronized
  fun createKeyPair(
    algorithm: String,
    keyGenParameterSpec: KeyGenParameterSpec
  ): KeyPair? {
    val keyPairGenerator = KeyPairGenerator.getInstance(
      algorithm, keyStore.provider.name
    )
    keyPairGenerator.initialize(keyGenParameterSpec)
    val locale = Locale.getDefault()
    try {
      Locale.setDefault(Locale.US)
      return keyPairGenerator.generateKeyPair().also { Logger.log(Level.Debug, "Generated key pair type $algorithm") }
    } finally {
      Locale.setDefault(locale)
    }
  }

  @Synchronized
  fun createKey(
    algorithm: String,
    keyGenParameterSpec: KeyGenParameterSpec
  ): Key? {
    val keyGenerator = KeyGenerator.getInstance(
      algorithm, keyStore.provider.name
    )
    keyGenerator.init(keyGenParameterSpec)
    return keyGenerator.generateKey().also { Logger.log(Level.Debug, "Generated key type $algorithm") }
  }

  @Synchronized
  override fun sign(
    data: ByteArray,
    signatureAlgorithm: String,
    private: PrivateKey
  ): ByteArray {
    return Signature.getInstance(signatureAlgorithm)
      .run {
        initSign(private)
        update(data)
        sign()
      }.also { Logger.log(Level.Debug, "Sign data with $signatureAlgorithm") }
  }

  @Synchronized
  override fun encrypt(
    data: ByteArray,
    cipherAlgorithm: String,
    key: Key
  ): EncryptedData {
    return Cipher.getInstance(cipherAlgorithm)
      .run {
        init(Cipher.ENCRYPT_MODE, key)
        EncryptedData(
          AlgorithmParametersSpec(
            parameters.encoded, parameters.provider.name,
            parameters.algorithm
          ),
          doFinal(data)
        ).also { Logger.log(Level.Debug, "Encrypt data with $cipherAlgorithm and result: $it") }
      }
  }

  @Synchronized
  override fun verify(
    data: ByteArray,
    signature: ByteArray,
    signatureAlgorithm: String,
    public: PublicKey
  ): Boolean {
    return Signature.getInstance(signatureAlgorithm)
      .run {
        initVerify(public)
        update(data)
        verify(signature)
      }.also { Logger.log(Level.Debug, "Verify message with $signatureAlgorithm") }
  }

  @Synchronized
  override fun decrypt(
    data: EncryptedData,
    cipherAlgorithm: String,
    key: Key
  ): ByteArray {
    return Cipher.getInstance(cipherAlgorithm)
      .run {
        val algorithmParameterSpec =
          AlgorithmParameters.getInstance(
            data.algorithmParameters.algorithm, data.algorithmParameters.provider
          )
            .apply {
              init(data.algorithmParameters.encoded)
            }
        init(Cipher.DECRYPT_MODE, key, algorithmParameterSpec)
        doFinal(data.encrypted)
      }.also { Logger.log(Level.Debug, "Decrypt encrypt data $data with $cipherAlgorithm") }
  }

  private fun getCertificate(alias: String): Certificate? {
    return keyStore.getCertificate(alias) ?: run {
      (keyStore.getEntry(alias, null) as? PrivateKeyEntry)?.certificate
    }.also { Logger.log(Level.Debug, "Get certificate for $alias") }
  }

  private fun getPrivateKey(alias: String): PrivateKey? {
    return keyStore.getKey(alias, null) as? PrivateKey ?: run {
      (keyStore.getEntry(alias, null) as? PrivateKeyEntry)?.privateKey
    }.also { Logger.log(Level.Debug, "Get private key for $alias") }
  }
}

interface AndroidKeyStoreOperations {
  fun sign(
    data: ByteArray,
    signatureAlgorithm: String,
    private: PrivateKey
  ): ByteArray

  fun encrypt(
    data: ByteArray,
    cipherAlgorithm: String,
    key: Key
  ): EncryptedData

  fun verify(
    data: ByteArray,
    signature: ByteArray,
    signatureAlgorithm: String,
    public: PublicKey
  ): Boolean

  fun decrypt(
    data: EncryptedData,
    cipherAlgorithm: String,
    key: Key
  ): ByteArray
}
