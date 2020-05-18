package com.twilio.verify.networking

import com.nhaarman.mockitokotlin2.mock
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.FactorStatus
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/*
 * Copyright (c) 2020, Twilio Inc.
 */

@RunWith(RobolectricTestRunner::class)
class AuthenticationProviderTest {
  private lateinit var authentication: Authentication

  @Before
  fun setup() {
    authentication = AuthenticationProvider()
  }

  @Test
  fun `Generate JWT token should match expected params`() {
    val factorSid = "sid"
    val friendlyName = "friendlyName"
    val accountSid = "accountSid"
    val serviceSid = "serviceSid"
    val entityIdentity = "entityIdentity"
    val status = FactorStatus.Unverified
    val factor =
      PushFactor(factorSid, friendlyName, accountSid, serviceSid, entityIdentity, status, mock())
    val jwt = JSONObject(authentication.generateJWT(factor))
    assertEquals(factor.accountSid, jwt.getString(issKey))
    assertEquals(factor.serviceSid, jwt.getString(subKey))
    assertTrue(jwt.has(expKey))
    assertTrue(jwt.has(iatKey))
    val validFor = jwt.getLong(expKey) - jwt.getLong(iatKey)
    assertEquals(jwtValidFor, validFor / 60)
    jwt.getJSONObject(grantsKey)
        .getJSONObject(verifyPushKey)
        .also {
          assertEquals(factor.sid, it.getString(factorSidKey))
          assertEquals(factor.entityIdentity, it.getString(entitySidKey))
          assertEquals(factor.serviceSid, it.getString(serviceSidKey))
        }
  }
}