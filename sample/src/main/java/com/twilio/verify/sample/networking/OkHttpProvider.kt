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

package com.twilio.verify.sample.networking

import com.twilio.verify.networking.FailureResponse
import com.twilio.verify.networking.HttpMethod.Delete
import com.twilio.verify.networking.HttpMethod.Post
import com.twilio.verify.networking.HttpMethod.Put
import com.twilio.verify.networking.MediaTypeHeader
import com.twilio.verify.networking.NetworkException
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.networking.Request
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

class OkHttpProvider(private val okHttpClient: OkHttpClient = okHttpClient()) : NetworkProvider {
  override fun execute(
    request: Request,
    success: (response: com.twilio.verify.networking.Response) -> Unit,
    error: (NetworkException) -> Unit
  ) {
    val okHttpRequest = toOkHttpRequest(request)
    okHttpClient.newCall(okHttpRequest)
      .enqueue(object : Callback {
        override fun onFailure(
          call: Call,
          e: IOException
        ) {
          error(NetworkException(e))
        }

        override fun onResponse(
          call: Call,
          response: Response
        ) {
          response.takeIf { it.isSuccessful }?.body?.run {
            success(
              com.twilio.verify.networking.Response(
                this.string(), response.headers.toMultimap()
              )
            )
          } ?: run {
            error(
              NetworkException(
                FailureResponse(
                  response.code, response.body?.string(), response.headers.toMultimap()
                )
              )
            )
          }
        }
      })
  }

  private fun toOkHttpRequest(request: Request): okhttp3.Request {
    val headersBuilder = Headers.Builder()
      .apply {
        request.headers.forEach { add(it.key, it.value) }
      }
    val requestBuilder = okhttp3.Request.Builder()
      .url(request.url)
      .headers(headersBuilder.build())
      .tag(request.tag)
    when (request.httpMethod) {
      Post, Put -> {
        val body = request.getParams()
        val contentType = request.headers[MediaTypeHeader.ContentType.type]
        if (body != null && contentType != null) {
          requestBuilder.post(
            body.toRequestBody(contentType.toMediaType())
          )
        }
      }
      Delete -> {
        val body = request.getParams()
        val contentType = request.headers[MediaTypeHeader.ContentType.type]
        if (body != null && contentType != null) {
          requestBuilder.delete(
            body.toRequestBody(contentType.toMediaType())
          )
        }
      }
    }
    return requestBuilder.build()
  }
}

fun okHttpClient(): OkHttpClient =
  OkHttpClient.Builder()
    .addInterceptor(
      HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
      }
    )
    .build()
