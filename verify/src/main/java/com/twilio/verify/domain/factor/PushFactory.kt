/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import android.content.Context
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.data.KeyStoreAdapter
import com.twilio.verify.domain.factor.models.FactorBuilder
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
    success: (Factor?) -> Unit
  ) {
    val enrollmentJWT = toEnrollmentJWT(jwt) ?: return
    if (enrollmentJWT.authyGrant.factorType != Push.factorTypeName) {
      return
    }
    val alias = generateKeyPairAlias()
    val publicKey = keyStorage.create(alias) ?: return
    val factorBuilder = FactorBuilder().friendlyName(friendlyName)
        .serviceSid(enrollmentJWT.authyGrant.serviceSid)
        .entityId(enrollmentJWT.authyGrant.entityId)
        .binding(mapOf(pushTokenKey to pushToken, publicKeyKey to publicKey))
        .type(Push)
    factorProvider.create(factorBuilder) { factor ->
      (factor as? PushFactor?)?.apply {
        keyPairAlias = alias
      }
          ?.let { factorProvider.update(it) }
      success(factor)
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