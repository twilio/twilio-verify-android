package com.twilio.verify.networking

import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import javax.net.ssl.HttpsURLConnection

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class NetworkAdapter : NetworkProvider {

  override fun execute(
    request: Request,
    success: (response: String) -> Unit,
    error: (NetworkException) -> Unit
  ) {
    var httpUrlConnection: HttpURLConnection? = null
    try {
      httpUrlConnection = request.url.openConnection() as HttpsURLConnection
      httpUrlConnection.requestMethod = request.httpMethod.method
      for ((key, value) in request.headers) {
        httpUrlConnection.setRequestProperty(key, value)
      }
      if (request.getParams()?.isNotEmpty() == true) {
        httpUrlConnection.doOutput = true
        val os: OutputStream = httpUrlConnection.outputStream
        val writer = BufferedWriter(
            OutputStreamWriter(os, "UTF-8")
        )
        writer.write(request.getParams())
        writer.flush()
        writer.close()
        os.close()
      }
      val responseCode = httpUrlConnection.responseCode
      if (responseCode < 300) {
        val response = httpUrlConnection.inputStream.bufferedReader()
            .use { it.readText() }
        success(response)
      } else {
        val errorResponse = httpUrlConnection.errorStream.bufferedReader()
            .use { it.readText() }
        error(NetworkException(responseCode, errorResponse))
      }
    } catch (e: Exception) {
      error(NetworkException(e))
    } finally {
      httpUrlConnection?.disconnect()
    }
  }
}