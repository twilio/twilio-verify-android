/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.verify.networking

import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import javax.net.ssl.HttpsURLConnection

class NetworkAdapter : NetworkProvider {

  override fun execute(
    request: Request,
    success: (response: Response) -> Unit,
    error: (NetworkException) -> Unit
  ) {
    var httpUrlConnection: HttpURLConnection? = null
    try {
      Logger.log(Level.Info, "Executing ${request.httpMethod.method} to ${request.url}")
      httpUrlConnection = request.url.openConnection() as HttpsURLConnection
      Logger.log(Level.Networking, "--> ${request.httpMethod.method} ${request.url}")
      httpUrlConnection.requestMethod = request.httpMethod.method
      request.headers.forEach { (key, value) ->
        httpUrlConnection.setRequestProperty(key, value)
        Logger.log(Level.Networking, "$key: $value")
      }
      request.getParams()?.takeIf { it.isNotEmpty() }?.apply {
        httpUrlConnection.doOutput = true
        val os: OutputStream = httpUrlConnection.outputStream
        val writer = BufferedWriter(
          OutputStreamWriter(os, "UTF-8")
        )
        writer.write(this)
        writer.flush()
        writer.close()
        os.close()
        Logger.log(Level.Networking, "Request: $this")
      }
      val statusCode = httpUrlConnection.responseCode
      Logger.log(Level.Networking, "Response code: $statusCode")
      when {
        statusCode < 300 -> {
          val response = httpUrlConnection.inputStream.bufferedReader()
            .use { it.readText() }.also { Logger.log(Level.Networking, "Response body: $it") }
          success(Response(body = response, headers = httpUrlConnection.headerFields))
        }
        else -> {
          val errorBody = httpUrlConnection.errorStream.bufferedReader()
            .use { it.readText() }.also { Logger.log(Level.Networking, "Error body: $it") }
          error(
            NetworkException(
              FailureResponse(statusCode, errorBody, httpUrlConnection.headerFields)
            )
          )
        }
      }
    } catch (e: Exception) {
      Logger.log(Level.Error, e.toString(), e)
      error(NetworkException(e))
    } finally {
      httpUrlConnection?.disconnect()
    }
  }
}
