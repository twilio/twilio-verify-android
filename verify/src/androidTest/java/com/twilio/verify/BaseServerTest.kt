package com.twilio.verify

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.platform.app.InstrumentationRegistry
import com.twilio.verify.TwilioVerify.Builder
import com.twilio.verify.data.provider
import com.twilio.verify.domain.factor.sharedPreferencesName
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.NetworkAdapter
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore

/*
 * Copyright (c) 2020, Twilio Inc.
 */

open class BaseServerTest {

  lateinit var authorization: Authorization
  lateinit var context: Context
  lateinit var twilioVerify: TwilioVerify
  lateinit var mockWebServer: MockWebServer
  lateinit var httpsURLConnection: HttpURLConnection
  lateinit var sharedPreferences: SharedPreferences
  var keyPairAlias: String? = null
  lateinit var keyStore: KeyStore

  @Before
  open fun before() {
    mockWebServer = MockWebServer()
    mockWebServer.start()
    httpsURLConnection =
      URL(mockWebServer.url("/").toString()).openConnection() as HttpURLConnection
    sharedPreferences = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
    context = InstrumentationRegistry.getInstrumentation()
        .targetContext
    authorization = Authorization("accountSid", "authToken")
    twilioVerify = Builder(context, authorization)
        .networkProvider(NetworkAdapter(httpsURLConnection))
        .build()
    keyStore = KeyStore.getInstance(provider)
        .apply {
          load(null)
        }
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
    sharedPreferences.edit()
        .clear()
        .apply()
    keyPairAlias?.let { alias ->
      keyStore.deleteEntry(alias)
    }
    keyStore.aliases()
        .toList()
        .forEach {
          keyStore.deleteEntry(it)
        }
  }

  fun enqueueMockResponse(
    code: Int,
    fileContent: String? = null
  ) {
    val mockResponse = MockResponse()
    mockResponse.setResponseCode(code)
    if (fileContent != null) {
      mockResponse.setBody(fileContent)
    }
    mockWebServer.enqueue(mockResponse)
  }
}

fun CountingIdlingResource.waitForResource(
  waitFor: Long = 100,
  times: Int = 10
) {
  for (i in 0..times) {
    if (!isIdleNow) {
      Thread.sleep(waitFor)
    } else {
      break
    }
  }
  assertTrue(isIdleNow)
}