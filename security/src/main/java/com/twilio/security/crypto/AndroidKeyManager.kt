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

import com.twilio.security.crypto.key.cipher.AESCipher
import com.twilio.security.crypto.key.cipher.Cipher
import com.twilio.security.crypto.key.signer.ECSigner
import com.twilio.security.crypto.key.signer.Signer
import com.twilio.security.crypto.key.template.AESGCMNoPaddingCipherTemplate
import com.twilio.security.crypto.key.template.CipherTemplate
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
import java.security.KeyPair
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey

class AndroidKeyManager(
  private val androidKeyStore: AndroidKeyStore
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
        is ECP256SignerTemplate -> ECSigner(keyPair, template.signatureAlgorithm, androidKeyStore)
      }
    } catch (e: Exception) {
      Logger.log(Level.ERROR, e.toString(), e)
      throw KeyException(e)
    }
  }

  private fun createSignerKeyPair(template: SignerTemplate): KeyPair {
    val keyPair =
      androidKeyStore.createKeyPair(template.algorithm, template.keyGenParameterSpec)
    return getSignerKeyPair(
      template.alias
    ).takeIf { keyPair?.public?.encoded?.contentEquals(it.public.encoded) == true }
      ?: throw IllegalArgumentException("New private key not found")
  }

  private fun getSignerKeyPair(alias: String): KeyPair {
    if (!contains(alias)) {
      throw IllegalArgumentException("alias not found")
    }
    return retryToGetValue { androidKeyStore.getKeyPair(alias) } ?: throw IllegalArgumentException(
      "Key pair not found"
    )
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
        is AESGCMNoPaddingCipherTemplate -> AESCipher(
          key, template.cipherAlgorithm, androidKeyStore
        )
      }
    } catch (e: Exception) {
      Logger.log(Level.ERROR, e.toString(), e)
      throw KeyException(e)
    }
  }

  @Throws(KeyException::class)
  override fun delete(alias: String) {
    try {
      if (contains(alias)) {
        androidKeyStore.deleteEntry(alias)
      }
    } catch (e: Exception) {
      Logger.log(Level.ERROR, e.toString(), e)
      throw KeyException(e)
    }
  }

  override fun contains(alias: String): Boolean = androidKeyStore.contains(alias)

  private fun getCipherKey(alias: String): SecretKey {
    return retryToGetValue { getSecretKey(alias) } ?: throw IllegalArgumentException(
      "Secret key not found"
    )
  }

  private fun getSecretKey(alias: String): SecretKey? {
    if (!contains(alias)) {
      throw IllegalArgumentException("alias not found")
    }
    return androidKeyStore.getSecretKey(alias)
  }

  private fun createCipherKey(template: CipherTemplate): SecretKey {
    val key = androidKeyStore.createKey(template.algorithm, template.keyGenParameterSpec)
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
    repeat(times - 1) { index ->
      val delay = (index + 1) * delayInMillis
      val result = try {
        block()
      } catch (e: Exception) {
        Logger.log(Level.ERROR, e.toString(), e)
        null
      }
      if (result == null) {
        TimeUnit.MILLISECONDS.sleep(delay)
      } else {
        return result
      }
    }
    return block()
  }
}
