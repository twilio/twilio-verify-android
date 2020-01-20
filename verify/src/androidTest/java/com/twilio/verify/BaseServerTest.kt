package com.twilio.verify

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

/*
 * Copyright (c) 2020, Twilio Inc.
 */

open class BaseServerTest {
  lateinit var mockWebServer: MockWebServer
  lateinit var httpsURLConnection: URLConnection

  @Before
  open fun before() {
    mockWebServer = MockWebServer()
    mockWebServer.start()
    httpsURLConnection = URL(mockWebServer.url("/").toString()).openConnection()
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

  fun waitResponse() {
    try {
      Thread.sleep(100)
    } catch (e: InterruptedException) {
      e.printStackTrace()
    }
  }

//  @Test
//  fun asd() {
//    mockWebServer.enqueue(MockResponse().setBody("Test"))
//    mockWebServer.start()
//    val urlConnection: HttpURLConnection =
//      URL(mockWebServer.url("/").toString()).openConnection() as HttpURLConnection
//    urlConnection.addRequestProperty("A", "value1")
//    urlConnection.addRequestProperty("A", "value2")
//    assertEquals("value2", urlConnection.getRequestProperty("A"))
//  }
}