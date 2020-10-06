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

package com.twilio.verify.api

import android.content.Context
import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
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
    fun getService(retries: Int = retryTimes) {
      try {
        val authToken = authentication.generateJWT(factor)
        val requestHelper = RequestHelper(
          context,
          BasicAuthorization(AUTHENTICATION_USER, authToken)
        )
        val request = Request.Builder(requestHelper, getServiceURL(serviceSid))
          .httpMethod(Get)
          .build()
        networkProvider.execute(
          request,
          {
            success(JSONObject(it.body))
          },
          { exception ->
            validateException(exception, ::getService, retries, error)
          }
        )
      } catch (e: TwilioVerifyException) {
        error(e)
      } catch (e: Exception) {
        Logger.log(Level.ERROR, e.toString(), e)
        error(TwilioVerifyException(NetworkException(e), NetworkError))
      }
    }
    getService()
  }

  private fun getServiceURL(
    serviceSid: String
  ) = "$baseUrl$getServiceURL".replace(SERVICE_SID_PATH, serviceSid, true)
}
