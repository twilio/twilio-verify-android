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

interface NetworkProvider {
  fun execute(
    request: Request,
    success: (response: Response) -> Unit,
    error: (NetworkException) -> Unit
  )
}

class NetworkException constructor(
  message: String?,
  cause: Throwable?,
  val failureResponse: FailureResponse?
) : Exception(message, cause) {
  constructor(
    failureResponse: FailureResponse?
  ) : this(
    "Network exception with status code ${failureResponse?.responseCode} and body: ${failureResponse?.errorBody}",
    null,
    failureResponse
  )

  constructor(cause: Throwable) : this(
    cause.message, cause, null
  )
}

class FailureResponse(
  val responseCode: Int,
  val errorBody: String?,
  val headers: Map<String, List<String>>?
)
