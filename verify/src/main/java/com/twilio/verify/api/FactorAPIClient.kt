package com.twilio.verify.api

import android.content.Context
import com.twilio.verify.Authentication
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.api.Action.DELETE
import com.twilio.verify.api.Action.UPDATE
import com.twilio.verify.domain.factor.models.CreateFactorPayload
import com.twilio.verify.domain.factor.models.UpdateFactorPayload
import com.twilio.verify.models.Factor
import com.twilio.verify.networking.BasicAuthorization
import com.twilio.verify.networking.HttpMethod.Delete
import com.twilio.verify.networking.HttpMethod.Post
import com.twilio.verify.networking.NetworkAdapter
import com.twilio.verify.networking.NetworkException
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.networking.Request
import com.twilio.verify.networking.RequestHelper
import org.json.JSONObject

/*
 * Copyright (c) 2020, Twilio Inc.
 */
internal const val SERVICE_SID_PATH = "{ServiceSid}"
internal const val ENTITY_PATH = "{EntityIdentity}"
internal const val FACTOR_SID_PATH = "{FactorSid}"

internal const val AUTH_PAYLOAD_PARAM = "AuthPayload"

internal const val CREATE_FACTOR_URL =
  "Services/$SERVICE_SID_PATH/Entities/$ENTITY_PATH/Factors"
internal const val VERIFY_FACTOR_URL =
  "Services/$SERVICE_SID_PATH/Entities/$ENTITY_PATH/Factors/$FACTOR_SID_PATH"
internal const val UPDATE_FACTOR_URL =
  "Services/$SERVICE_SID_PATH/Entities/$ENTITY_PATH/Factors/$FACTOR_SID_PATH"
internal const val DELETE_FACTOR_URL =
  "Services/$SERVICE_SID_PATH/Entities/$ENTITY_PATH/Factors/$FACTOR_SID_PATH"

internal const val FRIENDLY_NAME_KEY = "FriendlyName"
internal const val FACTOR_TYPE_KEY = "FactorType"
internal const val BINDING_KEY = "Binding"
internal const val CONFIG_KEY = "Config"
internal const val JWT_AUTHENTICATION_USER = "token"

internal class FactorAPIClient(
  private val networkProvider: NetworkProvider = NetworkAdapter(),
  private val context: Context,
  private val authentication: Authentication,
  private val baseUrl: String
) {

  fun create(
    createFactorPayload: CreateFactorPayload,
    success: (response: JSONObject) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val requestHelper =
        RequestHelper(
          context,
          BasicAuthorization(JWT_AUTHENTICATION_USER, createFactorPayload.jwt)
        )
      val request = Request.Builder(
        requestHelper,
        createFactorURL(createFactorPayload)
      )
        .httpMethod(Post)
        .body(createFactorBody(createFactorPayload))
        .build()
      networkProvider.execute(request, {
        success(JSONObject(it))
      }, { exception ->
        error(TwilioVerifyException(exception, NetworkError))
      })
    } catch (e: Exception) {
      error(TwilioVerifyException(NetworkException(e), NetworkError))
    }
  }

  fun verify(
    factor: Factor,
    authPayload: String,
    success: (response: JSONObject) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun verifyFactor(authToken: String) {
      try {
        val requestHelper =
          RequestHelper(
            context,
            BasicAuthorization(JWT_AUTHENTICATION_USER, authToken)
          )
        val request = Request.Builder(requestHelper, verifyFactorURL(factor))
          .httpMethod(Post)
          .body(verifyFactorBody(authPayload))
          .build()
        networkProvider.execute(request, {
          success(JSONObject(it))
        }, { exception ->
          error(TwilioVerifyException(exception, NetworkError))
        })
      } catch (e: Exception) {
        error(TwilioVerifyException(NetworkException(e), NetworkError))
      }
    }
    generateToken(
      authentication, identity = factor.entityIdentity, factorSid = factor.sid,
      serviceSid = factor.serviceSid, action = UPDATE,
      success = ::verifyFactor, error = error
    )
  }

  fun update(
    updateFactorPayload: UpdateFactorPayload,
    success: (response: JSONObject) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun updateFactor(authToken: String) {
      try {
        val requestHelper =
          RequestHelper(
            context,
            BasicAuthorization(JWT_AUTHENTICATION_USER, authToken)
          )
        val request =
          Request.Builder(requestHelper, updateFactorURL(updateFactorPayload))
            .httpMethod(Post)
            .body(updateFactorBody(updateFactorPayload))
            .build()
        networkProvider.execute(request, {
          success(JSONObject(it))
        }, { exception ->
          error(TwilioVerifyException(exception, NetworkError))
        })
      } catch (e: Exception) {
        error(TwilioVerifyException(NetworkException(e), NetworkError))
      }
    }
    generateToken(
      authentication, identity = updateFactorPayload.entity,
      factorSid = updateFactorPayload.factorSid, serviceSid = updateFactorPayload.serviceSid,
      action = UPDATE,
      success = ::updateFactor, error = error
    )
  }

  fun delete(
    factor: Factor,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun deleteFactor(authToken: String) {
      try {
        val requestHelper =
          RequestHelper(
            context,
            BasicAuthorization(JWT_AUTHENTICATION_USER, authToken)
          )
        val request = Request.Builder(requestHelper, deleteFactorURL(factor))
          .httpMethod(Delete)
          .build()
        networkProvider.execute(request, {
          success()
        }, { exception ->
          error(TwilioVerifyException(exception, NetworkError))
        })
      } catch (e: Exception) {
        error(TwilioVerifyException(NetworkException(e), NetworkError))
      }
    }
    generateToken(
      authentication, identity = factor.entityIdentity,
      factorSid = factor.sid, serviceSid = factor.serviceSid, action = DELETE,
      success = ::deleteFactor, error = error
    )
  }

  private fun createFactorURL(createFactorPayload: CreateFactorPayload): String =
    "$baseUrl$CREATE_FACTOR_URL".replace(SERVICE_SID_PATH, createFactorPayload.serviceSid, true)
      .replace(
        ENTITY_PATH, createFactorPayload.entity, true
      )

  private fun verifyFactorURL(factor: Factor): String =
    "$baseUrl$VERIFY_FACTOR_URL".replace(SERVICE_SID_PATH, factor.serviceSid, true)
      .replace(
        ENTITY_PATH, factor.entityIdentity, true
      ).replace(FACTOR_SID_PATH, factor.sid)

  private fun deleteFactorURL(factor: Factor): String =
    "$baseUrl$DELETE_FACTOR_URL".replace(SERVICE_SID_PATH, factor.serviceSid, true)
      .replace(
        ENTITY_PATH, factor.entityIdentity, true
      ).replace(FACTOR_SID_PATH, factor.sid)

  private fun updateFactorURL(
    updateFactorPayload: UpdateFactorPayload
  ): String =
    "$baseUrl$UPDATE_FACTOR_URL".replace(SERVICE_SID_PATH, updateFactorPayload.serviceSid, true)
      .replace(
        ENTITY_PATH, updateFactorPayload.entity, true
      ).replace(
        FACTOR_SID_PATH, updateFactorPayload.factorSid
      )

  private fun createFactorBody(
    createFactorPayload: CreateFactorPayload
  ): Map<String, String?> =
    mapOf(
      FRIENDLY_NAME_KEY to createFactorPayload.friendlyName,
      FACTOR_TYPE_KEY to createFactorPayload.type.factorTypeName,
      BINDING_KEY to JSONObject(createFactorPayload.binding).toString(),
      CONFIG_KEY to JSONObject(createFactorPayload.config).toString()
    )

  private fun verifyFactorBody(authPayload: String): Map<String, String?> =
    mapOf(AUTH_PAYLOAD_PARAM to authPayload)

  private fun updateFactorBody(
    updateFactorPayload: UpdateFactorPayload
  ): Map<String, String?> =
    mapOf(
      FRIENDLY_NAME_KEY to updateFactorPayload.friendlyName,
      CONFIG_KEY to JSONObject(updateFactorPayload.config).toString()
    )
}

fun jwe() {

}