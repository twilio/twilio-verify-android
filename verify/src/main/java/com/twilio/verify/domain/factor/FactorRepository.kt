/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.StorageError
import com.twilio.verify.api.FactorAPIClient
import com.twilio.verify.data.StorageException
import com.twilio.verify.data.StorageProvider
import com.twilio.verify.domain.factor.models.CreateFactorPayload
import com.twilio.verify.domain.factor.models.UpdateFactorPayload
import com.twilio.verify.models.Factor
import org.json.JSONObject

internal class FactorRepository(
  private val apiClient: FactorAPIClient,
  private val storage: StorageProvider,
  private val factorMapper: FactorMapper = FactorMapper()
) : FactorProvider {
  override fun create(
    createFactorPayload: CreateFactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun updateFactor(response: JSONObject) {
      try {
        val factor = save(factorMapper.fromApi(response, createFactorPayload))
        success(factor)
      } catch (e: TwilioVerifyException) {
        error(e)
      }
    }
    apiClient.create(createFactorPayload, ::updateFactor, error)
  }

  override fun verify(
    factor: Factor,
    payload: String,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun updateFactor(response: JSONObject) {
      try {
        factor.status = factorMapper.status(response)
        val updatedFactor = save(factor)
        success(updatedFactor)
      } catch (e: TwilioVerifyException) {
        error(e)
      }
    }
    apiClient.verify(factor, payload, ::updateFactor, error)
  }

  override fun update(
    updateFactorPayload: UpdateFactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun updateFactor(response: JSONObject) {
      try {
        success(factorMapper.fromApi(response, updateFactorPayload))
      } catch (e: TwilioVerifyException) {
        error(e)
      }
    }

    val factor = get(updateFactorPayload.factorSid) ?: throw TwilioVerifyException(
        StorageException("Factor not found"), StorageError
    )
    apiClient.update(factor, updateFactorPayload, ::updateFactor, error)
  }

  override fun delete(
    factor: Factor,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun deleteFactor() {
      storage.remove(factor.sid)
      success()
    }
    apiClient.delete(factor, ::deleteFactor, error)
  }

  @Throws(TwilioVerifyException::class)
  override fun get(sid: String): Factor? =
    storage.get(sid)
        ?.let {
          factorMapper.fromStorage(it)
        }

  @Throws(TwilioVerifyException::class)
  override fun save(factor: Factor): Factor {
    storage.save(factor.sid, factorMapper.toJSON(factor))
    return get(factor.sid) ?: throw TwilioVerifyException(
        StorageException("Factor not found"), StorageError
    )
  }

  @Throws(TwilioVerifyException::class)
  override fun getAll(): List<Factor> =
    storage.getAll()
        .mapNotNull { factorMapper.fromStorage(it) }
}