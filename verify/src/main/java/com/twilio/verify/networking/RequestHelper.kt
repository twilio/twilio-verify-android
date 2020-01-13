package com.twilio.verify.networking

import android.content.Context
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.twilio.verify.BuildConfig

private const val platform = "Android"
private const val sdkName = "VerifySDK"

class RequestHelper internal constructor(
  context: Context,
  authorization: Authorization
) {

  private val userAgentHeader = Pair("User-Agent", generateUserAgent(context))
  private val authorizationHeader = authorization.header
  var commonHeaders = mapOf(userAgentHeader, authorizationHeader)

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
}