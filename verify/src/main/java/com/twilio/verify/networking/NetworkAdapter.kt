package com.twilio.verify.networking

import java.io.BufferedWriter
import java.io.InputStream
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
    success: (response: Response) -> Unit,
    syncTime: (() -> Unit)?,
    error: (NetworkException) -> Unit
  ) {
    var httpUrlConnection: HttpURLConnection? = null
    try {
      httpUrlConnection = request.url.openConnection() as HttpsURLConnection
      httpUrlConnection.requestMethod = request.httpMethod.method
      for ((key, value) in request.headers) {
        httpUrlConnection.setRequestProperty(key, value)
      }
      if (request.getParams()
              ?.isNotEmpty() == true
      ) {
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
      when {
        responseCode < 300 -> {
          val response = httpUrlConnection.inputStream.bufferedReader()
              .use { it.readText() }
          success(Response(body = response, headers = httpUrlConnection.headerFields))
        }
        responseCode == 401 -> syncTime?.let { it() } ?: run {
          error(errorResponse(responseCode, httpUrlConnection.errorStream))
        }
        else -> {
          error(errorResponse(responseCode, httpUrlConnection.errorStream))
        }
      }
    } catch (e: Exception) {
      error(NetworkException(e))
    } finally {
      httpUrlConnection?.disconnect()
    }
  }

  private fun errorResponse(
    responseCode: Int,
    errorStream: InputStream
  ): NetworkException =
    NetworkException(responseCode, errorStream.bufferedReader()
        .use { it.readText() })
}