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

package com.twilio.verify.data

import android.util.Base64.NO_WRAP
import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import com.twilio.security.crypto.keyManager
import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.KeyStorageError

internal const val provider = "AndroidKeyStore"

internal class KeyStoreAdapter(private val manager: KeyManager = keyManager()) :
  KeyStorage {
  override fun create(alias: String): String {
    return try {
      encodeToBase64UTF8String(
        manager.signer(getSignerTemplate(alias, false))
          .getPublic(),
        NO_WRAP
      )
    } catch (e: Exception) {
      Logger.log(Level.ERROR, e.toString(), e)
      throw TwilioVerifyException(e, KeyStorageError)
    }
  }

  override fun sign(
    alias: String,
    message: String
  ): ByteArray {
    return try {
      manager.signer(getSignerTemplate(alias))
        .sign(message.toByteArray())
    } catch (e: Exception) {
      Logger.log(Level.ERROR, e.toString(), e)
      throw TwilioVerifyException(e, KeyStorageError)
    }
  }

  override fun signAndEncode(
    alias: String,
    message: String
  ): String {
    return try {
      encodeToBase64UTF8String(sign(alias, message), NO_WRAP)
    } catch (e: TwilioVerifyException) {
      throw e
    } catch (e: Exception) {
      Logger.log(Level.ERROR, e.toString(), e)
      throw TwilioVerifyException(e, KeyStorageError)
    }
  }

  override fun delete(alias: String) {
    try {
      manager.delete(alias)
    } catch (e: Exception) {
      Logger.log(Level.ERROR, e.toString(), e)
      throw TwilioVerifyException(e, KeyStorageError)
    }
  }
}

internal fun getSignerTemplate(
  alias: String,
  shouldExist: Boolean = true
): SignerTemplate = ECP256SignerTemplate(alias, shouldExist = shouldExist)
