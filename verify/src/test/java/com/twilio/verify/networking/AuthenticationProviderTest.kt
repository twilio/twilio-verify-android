package com.twilio.verify.networking

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.data.DateProvider
import com.twilio.verify.data.jwt.JwtGenerator
import com.twilio.verify.domain.factor.models.Config
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

/*
 * Copyright (c) 2020, Twilio Inc.
 */

@RunWith(RobolectricTestRunner::class)
class AuthenticationProviderTest {
  private val jwtGenerator: JwtGenerator = mock()
  private val dateProvider: DateProvider = mock()
  private lateinit var authentication: Authentication

  @Before
  fun setup() {
    authentication = AuthenticationProvider(jwtGenerator, dateProvider)
  }

  @Test
  fun `Generate JWT token should match expected params`() {
    val factorSid = "sid"
    val friendlyName = "friendlyName"
    val accountSid = "accountSid"
    val serviceSid = "serviceSid"
    val entityIdentity = "entityIdentity"
    val credentialSid = "credentialSid"
    val status = FactorStatus.Unverified
    val factor =
      PushFactor(
          factorSid, friendlyName, accountSid, serviceSid, entityIdentity, status, Date(),
          Config(credentialSid)
      ).apply {
        keyPairAlias = "test"
      }
    authentication.generateJWT(factor)
    verify(jwtGenerator).generateJWT(any(), check {
      assertEquals(credentialSid, it.getString(kidKey))
    }, check { jwt ->
      assertEquals(accountSid, jwt.getString(subKey))
      assertTrue(jwt.has(expKey))
      assertTrue(jwt.has(iatKey))
      val validFor = jwt.getLong(expKey) - jwt.getLong(iatKey)
      assertEquals(jwtValidFor, validFor / 60)
    })
  }

  @Test(expected = TwilioVerifyException::class)
  fun `Generate JWT token with no keypair should throw exception`() {
    val factorSid = "sid"
    val friendlyName = "friendlyName"
    val accountSid = "accountSid"
    val serviceSid = "serviceSid"
    val entityIdentity = "entityIdentity"
    val credentialSid = "credentialSid"
    val status = FactorStatus.Unverified
    val factor =
      PushFactor(
          factorSid, friendlyName, accountSid, serviceSid, entityIdentity, status, Date(),
          Config(credentialSid)
      ).apply {
        keyPairAlias = null
      }
    authentication.generateJWT(factor)
  }

  @Test(expected = TwilioVerifyException::class)
  fun `Generate JWT token with no factor not supported should throw exception`() {
    val factor: Factor = mock()
    authentication.generateJWT(factor)
  }
}