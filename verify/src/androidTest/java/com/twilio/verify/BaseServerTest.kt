package com.twilio.verify

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.platform.app.InstrumentationRegistry
import com.twilio.verify.TwilioVerify.Builder
import com.twilio.verify.api.Action
import com.twilio.verify.data.provider
import com.twilio.verify.domain.factor.sharedPreferencesName
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.internal.TlsUtil.localhost
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import java.security.KeyStore
import javax.net.ssl.HttpsURLConnection

/*
 * Copyright (c) 2020, Twilio Inc.
 */

open class BaseServerTest {

  private val authentication = object : Authentication {
    override fun generateJWE(
      identity: String,
      factorSid: String?,
      challengeSid: String?,
      serviceSid: String,
      action: Action,
      success: (token: String) -> Unit,
      error: (Exception) -> Unit
    ) {
      success("authToken")
    }

  }
  lateinit var context: Context
  lateinit var twilioVerify: TwilioVerify
  lateinit var mockWebServer: MockWebServer
  lateinit var sharedPreferences: SharedPreferences
  lateinit var keyStore: KeyStore
  protected val idlingResource = CountingIdlingResource(this.javaClass.simpleName)

  @Before
  open fun before() {
    val sslSocketFactory = localhost().sslSocketFactory()
    HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory)
    mockWebServer = MockWebServer()
    mockWebServer.useHttps(sslSocketFactory, false)
    mockWebServer.start()
    sharedPreferences = ApplicationProvider.getApplicationContext<Context>()
      .getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
    context = InstrumentationRegistry.getInstrumentation()
      .targetContext
    keyStore = KeyStore.getInstance(provider)
      .apply {
        load(null)
      }
    twilioVerify = Builder(context, authentication)
      .baseUrl(mockWebServer.url("/").toString())
      .build()
  }

  @After
  open fun tearDown() {
    mockWebServer.shutdown()
    sharedPreferences.edit()
      .clear()
      .apply()
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
  times: Int = 20
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