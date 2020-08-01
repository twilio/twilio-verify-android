/*
 * Copyright (c) 2020, Twilio Inc.
 */

object Versioning {

  val versionName = generateVersionName()
  val versionCode = generateVersionCode()
  private const val versionMajor = 0
  private const val versionMinor = 0
  private const val versionPatch = 2

  private fun generateVersionName(): String {
    return "${versionMajor}.${versionMinor}.${versionPatch}"
  }

  private fun generateVersionCode(): Int {
    val version = generateVersionName()
    val build = version.replace(".", "")
        .toInt()
    return build * 1000
  }
}