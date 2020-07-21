package com.twilio.verify.api

import android.content.Context
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.data.DateAdapter
import com.twilio.verify.data.DateProvider
import com.twilio.verify.models.Factor
import com.twilio.verify.networking.Authentication
import com.twilio.verify.networking.BasicAuthorization
import com.twilio.verify.networking.HttpMethod.Get
import com.twilio.verify.networking.NetworkAdapter
import com.twilio.verify.networking.NetworkException
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.networking.Request
import com.twilio.verify.networking.RequestHelper
import com.twilio.verify.storagePreferences
import org.json.JSONObject

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal const val getServiceURL = "Services/$SERVICE_SID_PATH"

internal class ServiceAPIClient(
  private val networkProvider: NetworkProvider = NetworkAdapter(),
  private val context: Context,
  private val authentication: Authentication,
  private val baseUrl: String,
  dateProvider: DateProvider = DateAdapter(
      storagePreferences(context)
  )
) : BaseAPIClient(dateProvider) {
  fun get(
    serviceSid: String,
    factor: Factor,
    success: (response: JSONObject) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun getService() {
      try {
        val authToken = authentication.generateJWT(factor)
        val requestHelper = RequestHelper(
            context,
            BasicAuthorization(AUTHENTICATION_USER, authToken)
        )
        val request = Request.Builder(requestHelper, getServiceURL(serviceSid))
            .httpMethod(Get)
            .build()
        networkProvider.execute(request, {
          success(JSONObject(it.body))
        }, { exception ->
          validateException(exception, ::getService, error)
        })
      } catch (e: TwilioVerifyException) {
        error(e)
      } catch (e: Exception) {
        error(TwilioVerifyException(NetworkException(e), NetworkError))
      }
    }
    getService()
  }

  private fun getServiceURL(
    serviceSid: String
  ) = "$baseUrl$getServiceURL".replace(SERVICE_SID_PATH, serviceSid, true)
}