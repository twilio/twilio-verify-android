/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import android.content.Context
import com.twilio.verify.api.FactorAPIClient
import com.twilio.verify.data.Storage
import com.twilio.verify.data.StorageProvider
import com.twilio.verify.domain.factor.models.FactorBuilder
import com.twilio.verify.models.Factor
import com.twilio.verify.networking.Authorization

private const val sharedPreferencesName = "TwilioVerify"

internal class FactorRepository(
  context: Context,
  authorization: Authorization,
  private val apiClient: FactorAPIClient = FactorAPIClient(context, authorization),
  private val storage: StorageProvider = Storage(
      context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
  ),
  private val factorMapper: FactorMapper = FactorMapper()
) : FactorProvider {
  override fun create(
    factorBuilder: FactorBuilder,
    success: (Factor?) -> Unit
  ) {
    apiClient.create(factorBuilder, { response ->
      val factor = factorMapper.fromApi(response, factorBuilder)
          ?.let {
            update(it)
          }
      success(factor)
    }, {})
  }

  override fun get(sid: String): Factor? =
    storage.get(sid)?.let {
      factorMapper.fromStorage(it)
    }

  override fun update(factor: Factor): Factor? {
    storage.save(factor.sid, factorMapper.toJSON(factor))
    return get(factor.sid)
  }
}