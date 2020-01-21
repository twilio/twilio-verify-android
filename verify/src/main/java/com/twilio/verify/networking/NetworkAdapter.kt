package com.twilio.verify.networking

import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import javax.net.ssl.HttpsURLConnection

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal class NetworkAdapter : NetworkProvider {

  override fun execute(
    request: Request,
    success: (response: String) -> Unit,
    error: (NetworkException) -> Unit
  ) {
    var urlConnection: HttpsURLConnection? = null
    try {
      urlConnection = request.url.openConnection() as HttpsURLConnection
      urlConnection.requestMethod = request.httpMethod.method
      for ((key, value) in request.headers) {
        urlConnection.setRequestProperty(key, value)
      }
      if (request.getParams()?.isNotEmpty() == true) {
        urlConnection.doOutput = true
        val os: OutputStream = urlConnection.outputStream
        val writer = BufferedWriter(
            OutputStreamWriter(os, "UTF-8")
        )
        writer.write(request.getParams())
        writer.flush()
        writer.close()
        os.close()
      }
      val responseCode = urlConnection.responseCode
      if (responseCode < 300) {
        val response = urlConnection.inputStream.bufferedReader()
            .use { it.readText() }
        success(response)
      } else {
        val errorResponse = urlConnection.inputStream.bufferedReader()
            .use { it.readText() }
        error(NetworkException(responseCode, errorResponse))
      }
    } catch (e: Exception) {
      error(NetworkException(e))
    } finally {
      urlConnection?.disconnect()
    }
  }
}