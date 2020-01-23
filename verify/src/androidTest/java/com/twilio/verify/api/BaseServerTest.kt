package com.twilio.verify.api

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import java.net.HttpURLConnection
import java.net.URL

/*
 * Copyright (c) 2020, Twilio Inc.
 */

open class BaseServerTest {
  lateinit var mockWebServer: MockWebServer
  lateinit var httpsURLConnection: HttpURLConnection

  @Before
  open fun before() {
    mockWebServer = MockWebServer()
    mockWebServer.start()
    httpsURLConnection =
      URL(mockWebServer.url("/").toString()).openConnection() as HttpURLConnection
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
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