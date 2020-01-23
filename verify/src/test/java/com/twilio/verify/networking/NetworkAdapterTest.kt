package com.twilio.verify.networking

import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/*
 * Copyright (c) 2020, Twilio Inc.
 */
@RunWith(RobolectricTestRunner::class)
class NetworkAdapterTest {

  private lateinit var networkAdapter: NetworkAdapter
  lateinit var request: Request

  @Before
  fun setup() {
    networkAdapter = NetworkAdapter()
    request = mock()
  }

  @Test
  fun `Request with success response code should return expected response`() {
    val url: URL = mock()
    val urlConnection: HttpsURLConnection = mock()
    val expectedResponse = "Response"
    val inputStream: InputStream = ByteArrayInputStream(expectedResponse.toByteArray())
    val outputStream: OutputStream = mock()

    `when`(request.url).thenReturn(url)
    `when`(request.httpMethod).thenReturn(HttpMethod.Post)
    `when`(request.getParams()).thenReturn("Params")
    `when`(url.openConnection()).thenReturn(urlConnection)
    `when`(urlConnection.outputStream).thenReturn(outputStream)
    `when`(urlConnection.responseCode).thenReturn(200)
    `when`(urlConnection.inputStream).thenReturn(inputStream)
    networkAdapter.execute(request, {
      assertEquals(expectedResponse, it)
    }, {
      fail()
    })
  }

  @Test
  fun `Request with failure response code should return error`() {
    val url: URL = mock()
    val urlConnection: HttpsURLConnection = mock()
    `when`(request.url).thenReturn(url)
    `when`(request.httpMethod).thenReturn(HttpMethod.Post)
    `when`(url.openConnection()).thenReturn(urlConnection)
    `when`(urlConnection.responseCode).thenReturn(400)
    val expectedResponse = "Error"
    val inputStream: InputStream = ByteArrayInputStream(expectedResponse.toByteArray())
    `when`(urlConnection.inputStream).thenReturn(inputStream)
    networkAdapter.execute(request, { fail() }, { exception ->
      assertTrue(exception.message?.contains(urlConnection.responseCode.toString()) == true)
    })
  }

  @Test
  fun `Request with failure opening connection should return error`() {
    val url: URL = mock()
    `when`(request.url).thenReturn(url)
    `when`(request.httpMethod).thenReturn(HttpMethod.Post)
    val expectedException: IOException = mock()
    `when`(url.openConnection()).thenThrow(expectedException)
    networkAdapter.execute(request, { fail() }, { exception ->
      assertEquals(expectedException, exception.cause)
    })
  }
}