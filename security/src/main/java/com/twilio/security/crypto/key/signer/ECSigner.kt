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

package com.twilio.security.crypto.key.signer

import com.twilio.security.crypto.AndroidKeyStoreOperations
import com.twilio.security.crypto.KeyException
import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
import java.security.KeyPair

class ECSigner(
  internal val keyPair: KeyPair,
  private val signatureAlgorithm: String,
  private val androidKeyStoreOperations: AndroidKeyStoreOperations
) : Signer {
  @Throws(KeyException::class)
  override fun sign(data: ByteArray): ByteArray {
    return try {
      androidKeyStoreOperations.sign(data, signatureAlgorithm, keyPair.private)
    } catch (e: Exception) {
      Logger.log(Level.ERROR, e.toString(), e)
      throw KeyException(e)
    }
  }

  @Throws(KeyException::class)
  override fun verify(
    data: ByteArray,
    signature: ByteArray
  ): Boolean {
    return try {
      return androidKeyStoreOperations.verify(data, signature, signatureAlgorithm, keyPair.public)
    } catch (e: Exception) {
      Logger.log(Level.ERROR, e.toString(), e)
      throw KeyException(e)
    }
  }

  @Throws(KeyException::class)
  override fun getPublic(): ByteArray {
    return try {
      keyPair.public.encoded
    } catch (e: Exception) {
      Logger.log(Level.ERROR, e.toString(), e)
      throw KeyException(e)
    }
  }
}
