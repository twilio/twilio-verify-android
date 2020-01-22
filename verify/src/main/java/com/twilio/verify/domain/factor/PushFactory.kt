/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import android.content.Context
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.TwilioVerifyException.ErrorCode.KeyStorageError
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.data.KeyStorageException
import com.twilio.verify.data.KeyStoreAdapter
import com.twilio.verify.domain.factor.models.FactorPayload
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.domain.factor.models.toEnrollmentJWT
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorType.Push
import com.twilio.verify.networking.Authorization

internal const val pushTokenKey = "push_token"
internal const val publicKeyKey = "public_key"

internal class PushFactory(
  context: Context,
  authorization: Authorization,
  private val factorProvider: FactorProvider = FactorRepository(context, authorization),
  private val keyStorage: KeyStorage = KeyStoreAdapter()
) {
  fun create(
    jwt: String,
    friendlyName: String,
    pushToken: String,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val enrollmentJWT = toEnrollmentJWT(jwt)
      if (enrollmentJWT.authyGrant.factorType != Push.factorTypeName) {
        throw TwilioVerifyException(IllegalArgumentException("Invalid factor type"), InputError)
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
                success(pushFactor)
              } ?: run {
                error(
                    TwilioVerifyException(KeyStorageException("Key pair not set"), KeyStorageError)
                )
              }
            }
      }, error)
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
}