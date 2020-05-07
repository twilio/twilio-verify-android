package com.twilio.verify.sample.push

import com.google.firebase.messaging.RemoteMessage
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.twilio.sample.TwilioVerifyAdapter
import com.twilio.sample.push.FirebasePushService
import com.twilio.sample.push.challengeSidKey
import com.twilio.sample.push.challengeType
import com.twilio.sample.push.factorSidKey
import com.twilio.sample.push.typeKey
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/*
 * Copyright (c) 2020, Twilio Inc.
 */
@RunWith(RobolectricTestRunner::class)
class FirebasePushServiceTest {

  private val twilioVerifyAdapter: TwilioVerifyAdapter = mock()
  private val firebasePushService = FirebasePushService()

  @Before
  fun setup() {
    firebasePushService.twilioVerifyAdapter = twilioVerifyAdapter
  }

  @Test
  fun `New message received with challenge sid, factor sid and challenge type should call getChallenge`() {
    val challengeSid = "challengeSid"
    val factorSid = "factorSid"
    val pushData =
      mapOf(typeKey to challengeType, factorSidKey to factorSid, challengeSidKey to challengeSid)
    val remoteMessage: RemoteMessage = mock() {
      on { data } doReturn pushData
    }
    firebasePushService.onMessageReceived(remoteMessage)
    verify(twilioVerifyAdapter).getChallenge(challengeSid, factorSid)
  }

  @Test
  fun `New message received with challenge type without challenge sid, factor sid should not call getChallenge`() {
    val pushData =
      mapOf(typeKey to challengeType)
    val remoteMessage: RemoteMessage = mock() {
      on { data } doReturn pushData
    }
    firebasePushService.onMessageReceived(remoteMessage)
    verify(twilioVerifyAdapter, never()).getChallenge(any(), any())
  }

  @Test
  fun `New message received without challenge type should not call getChallenge`() {
    val pushData =
      mapOf(typeKey to "otherType", factorSidKey to "factorSid", challengeSidKey to "challengeSid")
    val remoteMessage: RemoteMessage = mock() {
      on { data } doReturn pushData
    }
    firebasePushService.onMessageReceived(remoteMessage)
    verify(twilioVerifyAdapter, never()).getChallenge(any(), any())
  }
}