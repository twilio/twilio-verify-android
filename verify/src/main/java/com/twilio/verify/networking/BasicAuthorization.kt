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

import android.util.Base64
import com.twilio.verify.data.encodeToBase64UTF8String

internal const val AuthorizationHeader = "Authorization"
internal const val BasicAuth = "Basic"

internal data class BasicAuthorization constructor(
  private val username: String,
  private val password: String
) {
  val header: Pair<String, String>
    get() {
      val encodedAuthorization = encodeToBase64UTF8String(
        "${this.username}:${this.password}".toByteArray(Charsets.UTF_8),
        Base64.NO_WRAP
      )
      return AuthorizationHeader to "$BasicAuth $encodedAuthorization"
    }
}
