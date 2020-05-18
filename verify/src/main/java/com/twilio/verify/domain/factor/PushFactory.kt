/*
 * Copyright (c) 2020, Twilio Inc.
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
import com.twilio.verify.threading.execute

internal const val PUBLIC_KEY_KEY = "public_key"
internal const val FCM_PUSH_TYPE = "fcm"
internal const val SDK_VERSION_KEY = "sdk_version"
internal const val APP_ID_KEY = "app_id"
internal const val NOTIFICATION_PLATFORM_KEY = "notification_platform"
internal const val NOTIFICATION_TOKEN_KEY = "notification_token"
internal const val ALG_KEY = "alg"
internal const val DEFAULT_ALG = "ES256"

internal class PushFactory(
  private val factorProvider: FactorProvider,
  private val keyStorage: KeyStorage,
  private val context: Context
) {
  fun create(
    jwt: String,
    friendlyName: String,
    pushToken: String,
    serviceSid: String,
    identity: String,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    execute(success, error) { onSuccess, onError ->
      try {
        val alias = generateKeyPairAlias()
        val publicKey = keyStorage.create(alias)
        val binding = binding(publicKey)
        val config = config(pushToken)
        val factorBuilder = CreateFactorPayload(
            friendlyName, PUSH, serviceSid,
            identity, config, binding, jwt
        )

        fun onFactorCreated(factor: Factor) {
          (factor as? PushFactor?)?.apply {
            keyPairAlias = alias
          }
              ?.let { pushFactor ->
                pushFactor.takeUnless { it.keyPairAlias.isNullOrEmpty() }?.let {
                  factorProvider.save(pushFactor)
                  onSuccess(pushFactor)
                } ?: run {
                  keyStorage.delete(alias)
                  onError(
                      TwilioVerifyException(
                          IllegalStateException("Key pair not set"), KeyStorageError
                      )
                  )
                }
              }
        }

        factorProvider.create(factorBuilder, ::onFactorCreated) { exception ->
          keyStorage.delete(alias)
          onError(exception)
        }
      } catch (e: TwilioVerifyException) {
        onError(e)
      }
    }
  }

  fun verify(
    sid: String,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    execute(success, error) { onSuccess, onError ->
      fun verifyFactor(pushFactor: PushFactor) {
        pushFactor.keyPairAlias?.let { keyPairAlias ->
          val payload = keyStorage.sign(keyPairAlias, sid)
          factorProvider.verify(pushFactor, payload, onSuccess, onError)
        } ?: run {
          onError(TwilioVerifyException(IllegalStateException("Alias not found"), KeyStorageError))
        }
      }

      try {
        val factor = factorProvider.get(sid) as? PushFactor
        factor?.let(::verifyFactor) ?: run {
          throw TwilioVerifyException(StorageException("Factor not found"), StorageError)
        }
      } catch (e: TwilioVerifyException) {
        onError(e)
      }
    }
  }

  fun update(
    sid: String,
    pushToken: String,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    execute(success, error) { onSuccess, onError ->
      fun updateFactor(pushFactor: PushFactor) {
        val updateFactorPayload = UpdateFactorPayload(
            pushFactor.friendlyName, PUSH, pushFactor.serviceSid, pushFactor.entityIdentity,
            config(pushToken), pushFactor.sid
        )
        factorProvider.update(pushFactor, updateFactorPayload, onSuccess, onError)
      }
      try {
        val factor = factorProvider.get(sid) as? PushFactor
        factor?.let(::updateFactor) ?: run {
          throw TwilioVerifyException(StorageException("Factor not found"), StorageError)
        }
      } catch (e: TwilioVerifyException) {
        onError(e)
      }
    }
  }

  fun delete(
    sid: String,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    execute(success, error) { onSuccess, onError ->
      fun deleteFactor(pushFactor: PushFactor) {
        factorProvider.delete(pushFactor, {
          pushFactor.keyPairAlias?.let { keyStorage.delete(it) }
          onSuccess()
        }, onError)
      }
      try {
        val factor = factorProvider.get(sid) as? PushFactor
        factor?.let(::deleteFactor) ?: run {
          throw TwilioVerifyException(StorageException("Factor not found"), StorageError)
        }
      } catch (e: TwilioVerifyException) {
        onError(e)
      }
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