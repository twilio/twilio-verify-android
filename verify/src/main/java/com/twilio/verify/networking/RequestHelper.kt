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

import android.content.Context
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.twilio.verify.BuildConfig
import com.twilio.verify.networking.HttpMethod.DELETE
import com.twilio.verify.networking.HttpMethod.GET
import com.twilio.verify.networking.HttpMethod.POST
import com.twilio.verify.networking.MediaTypeValue.JSON
import com.twilio.verify.networking.MediaTypeValue.URL_ENCODED

private const val platform = "Android"
private const val sdkName = "VerifySDK"
internal const val userAgent = "User-Agent"

class RequestHelper internal constructor(
  context: Context,
  authorization: BasicAuthorization
) {

  private val userAgentHeader = Pair(userAgent, generateUserAgent(context))
  private val authorizationHeader = authorization.header

  private fun generateUserAgent(context: Context): String {
    val userAgentBuilder = StringBuilder()
    val separator = "; "
    val appName = context.applicationInfo.loadLabel(context.packageManager)
    val appVersionName = context.packageManager.getPackageInfo(context.packageName, 0)
      .versionName
    val appVersionCode = if (VERSION.SDK_INT >= VERSION_CODES.P) {
      context.packageManager.getPackageInfo(context.packageName, 0)
        .longVersionCode.toInt()
    } else {
      context.packageManager.getPackageInfo(context.packageName, 0)
        .versionCode
    }
    val osVersion = "$platform ${VERSION.RELEASE} (${VERSION.SDK_INT})"
    val sdkVersionName = BuildConfig.VERSION_NAME
    val sdkVersionCode = BuildConfig.VERSION_CODE
    val device = Build.MODEL
    userAgentBuilder.append(appName)
      .append(separator)
      .append(platform)
      .append(separator)
      .append(appVersionName)
      .append(separator)
      .append(appVersionCode)
      .append(separator)
      .append(osVersion)
      .append(separator)
      .append(device)
      .append(separator)
      .append(sdkName)
      .append(separator)
      .append(sdkVersionName)
      .append(separator)
      .append(sdkVersionCode)
    return userAgentBuilder.toString()
  }

  fun commonHeaders(httpMethod: HttpMethod?): Map<String, String> {
    var commonHeaders = mapOf(userAgentHeader, authorizationHeader)
    commonHeaders = when (httpMethod) {
      POST, DELETE -> commonHeaders.plus(
        mediaTypeHeaders(acceptTypeValue = JSON, contentTypeValue = URL_ENCODED)
      )
      GET -> commonHeaders.plus(
        mediaTypeHeaders(acceptTypeValue = URL_ENCODED, contentTypeValue = URL_ENCODED)
      )
      else -> commonHeaders
    }
    return commonHeaders
  }

  private fun mediaTypeHeaders(
    acceptTypeValue: MediaTypeValue,
    contentTypeValue: MediaTypeValue
  ): Map<String, String> =
    mapOf(
      MediaTypeHeader.ACCEPT.type to acceptTypeValue.type,
      MediaTypeHeader.CONTENT_TYPE.type to contentTypeValue.type
    )
}
