/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import android.content.Context
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.StorageError
import com.twilio.verify.api.FactorAPIClient
import com.twilio.verify.data.Storage
import com.twilio.verify.data.StorageException
import com.twilio.verify.data.StorageProvider
import com.twilio.verify.domain.factor.models.FactorPayload
import com.twilio.verify.models.Factor

private const val sharedPreferencesName = "TwilioVerify"

internal class FactorRepository(
  context: Context,
  private val apiClient: FactorAPIClient,
  private val storage: StorageProvider = Storage(
      context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
  ),
  private val factorMapper: FactorMapper = FactorMapper()
) : FactorProvider {
  override fun create(
    factorPayload: FactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    apiClient.create(factorPayload, { response ->
      try {
        val factor = update(factorMapper.fromApi(response, factorPayload))
        success(factor)
      } catch (e: TwilioVerifyException) {
        error(e)
      }
    }, error)
  }

  override fun verify(
    factor: Factor,
    payload: String,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    apiClient.verify(factor, payload, { response ->
      try {
        factor.status = factorMapper.status(response)
        val updatedFactor = update(factor)
        success(updatedFactor)
      } catch (e: TwilioVerifyException) {
        error(e)
      }
    }, error)
  }

  @Throws(TwilioVerifyException::class)
  override fun get(sid: String): Factor? =
    storage.get(sid)?.let {
      factorMapper.fromStorage(it)
    }

  @Throws(TwilioVerifyException::class)
  override fun update(factor: Factor): Factor {
    storage.save(factor.sid, factorMapper.toJSON(factor))
    return get(factor.sid) ?: throw TwilioVerifyException(
        StorageException("Factor not found"), StorageError
    )
  }
}