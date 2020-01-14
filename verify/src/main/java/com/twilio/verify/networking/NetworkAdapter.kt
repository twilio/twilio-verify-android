package com.twilio.verify.networking

import java.net.HttpURLConnection
import java.net.URL

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class NetworkAdapter() : NetworkProvider {

  override fun execute(
    request: Request,
    success: (response: String) -> Unit,
    failure: (exception: Exception) -> Unit
  ) {
    val urlConnection = URL(request.url).openConnection() as HttpURLConnection
    urlConnection.requestMethod = request.httpMethod.method
    for ((key, value) in request.headers) {
      urlConnection.setRequestProperty(key, value)
    }
    try {
      val response = urlConnection.inputStream.bufferedReader()
          .use { it.readText() }
      success(response)
    } catch (e: Exception) {
      failure(e)
    } finally {
      urlConnection.disconnect()
    }

  }

}