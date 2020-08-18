package com.twilio.verify

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.idling.CountingIdlingResource
import com.twilio.verify.TwilioVerify.Builder
import com.twilio.verify.data.provider
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

  lateinit var context: Context
  lateinit var twilioVerify: TwilioVerify
  lateinit var mockWebServer: MockWebServer
  lateinit var sharedPreferences: SharedPreferences
  lateinit var encryptedSharedPreferences: SharedPreferences
  lateinit var keyStore: KeyStore
  protected val idlingResource = CountingIdlingResource(this.javaClass.simpleName)

  @Before
  open fun before() {
    val sslSocketFactory = localhost().sslSocketFactory()
    HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory)
    mockWebServer = MockWebServer()
    mockWebServer.useHttps(sslSocketFactory, false)
    mockWebServer.start()
    context = ApplicationProvider.getApplicationContext()
    val storageName = "${context.packageName}.$VERIFY_SUFFIX"
    sharedPreferences = context
        .getSharedPreferences(storageName, Context.MODE_PRIVATE)
    encryptedSharedPreferences = context
        .getSharedPreferences(
            "$storageName.$ENC_SUFFIX", Context.MODE_PRIVATE
        )
    keyStore = KeyStore.getInstance(provider)
        .apply {
          load(null)
        }
    twilioVerify = Builder(context)
        .baseUrl(
            mockWebServer.url("/")
                .toString()
        )
        .build()
  }

  @After
  open fun tearDown() {
    mockWebServer.shutdown()
    sharedPreferences.edit()
        .clear()
        .apply()
    encryptedSharedPreferences.edit()
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
    fileContent: String? = null,
    headers: Map<String, List<String>> = emptyMap()
  ) {
    val mockResponse = MockResponse()
    mockResponse.setResponseCode(code)
    if (fileContent != null) {
      mockResponse.setBody(fileContent)
    }
    headers.forEach { header ->
      header.value.forEach { value ->
        mockResponse.addHeader(header.key, value)
      }
    }
    mockWebServer.enqueue(mockResponse)
  }
}

fun CountingIdlingResource.waitForResource(
  waitFor: Long = 200,
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