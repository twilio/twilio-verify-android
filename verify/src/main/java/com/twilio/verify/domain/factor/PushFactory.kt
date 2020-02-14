/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.TwilioVerifyException.ErrorCode.KeyStorageError
import com.twilio.verify.TwilioVerifyException.ErrorCode.StorageError
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.data.KeyStoreAdapter
import com.twilio.verify.data.StorageException
import com.twilio.verify.domain.factor.models.FactorPayload
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.domain.factor.models.toEnrollmentJWT
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorType.Push
import com.twilio.verify.threading.execute

internal const val pushTokenKey = "address"
internal const val publicKeyKey = "public_key"

internal class PushFactory(
  private val factorProvider: FactorProvider,
  private val keyStorage: KeyStorage = KeyStoreAdapter()
) {
  fun create(
    jwt: String,
    friendlyName: String,
    pushToken: String,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    execute(success, error) { onSuccess, onError ->
      try {
        val enrollmentJWT = toEnrollmentJWT(jwt)
        if (enrollmentJWT.authyGrant.factorType != Push.factorTypeName) {
          throw TwilioVerifyException(
              IllegalArgumentException("Invalid factor type"),
              InputError
          )
        }
        val alias = generateKeyPairAlias()
        val publicKey = keyStorage.create(alias)
        val factorBuilder = FactorPayload(
            friendlyName, Push, mapOf(pushTokenKey to pushToken, publicKeyKey to publicKey),
            enrollmentJWT.authyGrant.serviceSid, enrollmentJWT.authyGrant.entityId
        )
        factorProvider.create(factorBuilder, { factor ->
          (factor as? PushFactor?)?.apply {
            keyPairAlias = alias
          }
              ?.let { pushFactor ->
                pushFactor.takeUnless { it.keyPairAlias.isNullOrEmpty() }?.let {
                  factorProvider.update(pushFactor)
                  onSuccess(pushFactor)
                } ?: run {
                  keyStorage.delete(alias)
                  throw TwilioVerifyException(
                      IllegalStateException("Key pair not set"), KeyStorageError
                  )
                }
              }
        }, { exception ->
          keyStorage.delete(alias)
          onError(exception)
        })
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
      try {
        val factor = factorProvider.get(sid) as? PushFactor
        factor?.let { pushFactor ->
          pushFactor.keyPairAlias?.let { keyPairAlias ->
            val payload = keyStorage.sign(
                keyPairAlias, pushFactor.sid
            )
            factorProvider.verify(factor, payload, onSuccess, onError)
          } ?: run {
            throw TwilioVerifyException(IllegalStateException("Alias not found"), KeyStorageError)
          }
        } ?: run {
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
}