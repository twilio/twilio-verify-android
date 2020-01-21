package com.twilio.verify.networking

import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import javax.net.ssl.HttpsURLConnection

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class NetworkAdapter(private var urlConnection: HttpURLConnection? = null) : NetworkProvider {

  override fun execute(
    request: Request,
    success: (response: String) -> Unit,
    error: () -> Unit
  ) {
    try {
      if (urlConnection == null) {
        urlConnection = request.url.openConnection() as HttpsURLConnection
      }
      urlConnection?.let { httpUrlConnection ->
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
          error()
        }
      } ?: run(error)
    } catch (e: Exception) {
      error()
    } finally {
      urlConnection?.disconnect()
    }
  }
}