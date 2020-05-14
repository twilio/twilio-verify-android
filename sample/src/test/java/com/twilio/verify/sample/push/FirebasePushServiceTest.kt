package com.twilio.verify.sample.push

import androidx.test.core.app.ApplicationProvider
import com.google.firebase.messaging.RemoteMessage
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.twilio.verify.sample.TwilioVerifyAdapter
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner

/*
 * Copyright (c) 2020, Twilio Inc.
 */
@RunWith(RobolectricTestRunner::class)
class FirebasePushServiceTest {

  private lateinit var firebasePushService: FirebasePushService
  private val twilioVerifyAdapter: TwilioVerifyAdapter = mock()

  @Before
  fun setup() {
    val appModule = module {
      single {
        twilioVerifyAdapter
      }
    }
    startKoin {
      androidLogger()
      androidContext(ApplicationProvider.getApplicationContext())
      modules(appModule)
    }
    firebasePushService = FirebasePushService()
  }

  @After
  fun tearDown() {
    stopKoin()
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
    verify(twilioVerifyAdapter).showChallenge(challengeSid, factorSid)
  }

  @Test
  fun `New message received with challenge type without challenge sid, factor sid should not call getChallenge`() {
    val pushData =
      mapOf(typeKey to challengeType)
    val remoteMessage: RemoteMessage = mock() {
      on { data } doReturn pushData
    }
    firebasePushService.onMessageReceived(remoteMessage)
    verify(twilioVerifyAdapter, never()).showChallenge(any(), any())
  }

  @Test
  fun `New message received without challenge type should not call getChallenge`() {
    val pushData =
      mapOf(typeKey to "otherType", factorSidKey to "factorSid", challengeSidKey to "challengeSid")
    val remoteMessage: RemoteMessage = mock() {
      on { data } doReturn pushData
    }
    firebasePushService.onMessageReceived(remoteMessage)
    verify(twilioVerifyAdapter, never()).showChallenge(any(), any())
  }
}