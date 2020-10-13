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

package com.twilio.verify.domain.factor

import android.content.Context
import com.twilio.verify.BuildConfig
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.KeyStorageError
import com.twilio.verify.TwilioVerifyException.ErrorCode.StorageError
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.data.StorageException
import com.twilio.verify.domain.factor.models.CreateFactorPayload
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.domain.factor.models.UpdateFactorPayload
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorType.PUSH

internal const val PUBLIC_KEY_KEY = "PublicKey"
internal const val FCM_PUSH_TYPE = "fcm"
internal const val SDK_VERSION_KEY = "SdkVersion"
internal const val APP_ID_KEY = "AppId"
internal const val NOTIFICATION_PLATFORM_KEY = "NotificationPlatform"
internal const val NOTIFICATION_TOKEN_KEY = "NotificationToken"
internal const val ALG_KEY = "Alg"
internal const val DEFAULT_ALG = "ES256"

internal class PushFactory(
  private val factorProvider: FactorProvider,
  private val keyStorage: KeyStorage,
  private val context: Context
) {
  fun create(
    accessToken: String,
    friendlyName: String,
    pushToken: String,
    serviceSid: String,
    identity: String,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val alias = generateKeyPairAlias()
      val publicKey = keyStorage.create(alias)
      val binding = binding(publicKey)
      val config = config(pushToken)
      val factorBuilder = CreateFactorPayload(
        friendlyName, PUSH, serviceSid,
        identity, config, binding, accessToken
      )

      fun onFactorCreated(factor: Factor) {
        (factor as? PushFactor?)?.apply {
          keyPairAlias = alias
        }
          ?.let { pushFactor ->
            pushFactor.takeUnless { it.keyPairAlias.isNullOrEmpty() }
              ?.let {
                factorProvider.save(pushFactor)
                success(pushFactor)
              } ?: run {
              keyStorage.delete(alias)
              error(
                TwilioVerifyException(
                  IllegalStateException("Key pair not set"), KeyStorageError
                )
              )
            }
          }
      }

      factorProvider.create(factorBuilder, ::onFactorCreated) { exception ->
        keyStorage.delete(alias)
        error(exception)
      }
    } catch (e: TwilioVerifyException) {
      error(e)
    }
  }

  fun verify(
    sid: String,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun verifyFactor(pushFactor: PushFactor) {
      pushFactor.keyPairAlias?.let { keyPairAlias ->
        val payload = keyStorage.signAndEncode(keyPairAlias, sid)
        factorProvider.verify(pushFactor, payload, success, error)
      } ?: run {
        error(TwilioVerifyException(IllegalStateException("Alias not found"), KeyStorageError))
      }
    }

    try {
      val factor = factorProvider.get(sid) as? PushFactor
      factor?.let(::verifyFactor) ?: run {
        throw TwilioVerifyException(StorageException("Factor not found"), StorageError)
      }
    } catch (e: TwilioVerifyException) {
      error(e)
    }
  }

  fun update(
    sid: String,
    pushToken: String,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun updateFactor(pushFactor: PushFactor) {
      val updateFactorPayload = UpdateFactorPayload(
        pushFactor.friendlyName, PUSH, pushFactor.serviceSid, pushFactor.identity,
        config(pushToken), pushFactor.sid
      )
      factorProvider.update(updateFactorPayload, success, error)
    }
    try {
      val factor = factorProvider.get(sid) as? PushFactor
      factor?.let(::updateFactor) ?: run {
        throw TwilioVerifyException(StorageException("Factor not found"), StorageError)
      }
    } catch (e: TwilioVerifyException) {
      error(e)
    }
  }

  fun delete(
    sid: String,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun deleteFactor(pushFactor: PushFactor) {
      factorProvider.delete(
        pushFactor,
        {
          pushFactor.keyPairAlias?.let { keyStorage.delete(it) }
          success()
        },
        error
      )
    }
    try {
      val factor = factorProvider.get(sid) as? PushFactor
      factor?.let(::deleteFactor) ?: run {
        throw TwilioVerifyException(StorageException("Factor not found"), StorageError)
      }
    } catch (e: TwilioVerifyException) {
      error(e)
    }
  }

  private fun generateKeyPairAlias(): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..15)
      .map { kotlin.random.Random.nextInt(0, charPool.size) }
      .map(charPool::get)
      .joinToString("")
  }

  private fun binding(
    publicKey: String
  ): Map<String, String> = mapOf(PUBLIC_KEY_KEY to publicKey, ALG_KEY to DEFAULT_ALG)

  private fun config(
    pushToken: String
  ): Map<String, String> = mapOf(
    SDK_VERSION_KEY to BuildConfig.VERSION_NAME,
    APP_ID_KEY to context.applicationInfo.packageName,
    NOTIFICATION_PLATFORM_KEY to FCM_PUSH_TYPE, NOTIFICATION_TOKEN_KEY to pushToken
  )
}
