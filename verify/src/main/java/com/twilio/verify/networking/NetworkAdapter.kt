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
      httpUrlConnection = request.url.openConnection() as HttpsURLConnection
      httpUrlConnection.requestMethod = request.httpMethod.method
      request.headers.forEach { (key, value) ->
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
        else -> {
          val errorBody = httpUrlConnection.errorStream.bufferedReader()
            .use { it.readText() }
          error(
            NetworkException(
              FailureResponse(responseCode, errorBody, httpUrlConnection.headerFields)
            )
          )
        }
      }
    } catch (e: Exception) {
      error(NetworkException(e))
    } finally {
      httpUrlConnection?.disconnect()
    }
  }
}
