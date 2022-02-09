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

package com.twilio.verify

import com.twilio.verify.models.FactorType

private const val NETWORK_CODE = 60401
private const val MAPPER_CODE = 60402
private const val STORAGE_CODE = 60403
private const val INPUT_CODE = 60404
private const val KEY_STORAGE_CODE = 60405
private const val INITIALIZATION_CODE = 60406
private const val AUTHENTICATION_TOKEN_CODE = 60407

/**
 * Exception types returned by the TwilioVerify SDK. It encompasses different types of errors that have their own associated reasons and codes.
 *
 * @param cause associated reason of the exception.
 * @param errorCode associated error code of the exception.
 */
class TwilioVerifyException(
  cause: Throwable,
  errorCode: ErrorCode
) : Exception(errorCode.message, cause) {

  /**
   * Error codes
   *
   * @property message error description.
   */
  enum class ErrorCode(val message: String) {
    /**
     * An error occurred while calling the API.
     */
    NetworkError("{$NETWORK_CODE} Exception while calling the API"),

    /**
     * An error occurred while mapping an entity.
     *
     */
    MapperError("{$MAPPER_CODE} Exception while mapping an entity"),

    /**
     * An error occurred while storing/loading an entity.
     *
     */
    StorageError("{$STORAGE_CODE} Exception while storing/loading an entity"),

    /**
     * An error occurred while loading input.
     *
     */
    InputError("{$INPUT_CODE} Exception while loading input"),

    /**
     * An error occurred while storing/loading keypairs.
     *
     */
    KeyStorageError("{$KEY_STORAGE_CODE} Exception while storing/loading key pairs"),

    /**
     * An error occurred while initializing a class.
     *
     */
    InitializationError("{$INITIALIZATION_CODE} Exception while initializing"),

    /**
     * An error occurred while generating a token.
     *
     */
    AuthenticationTokenError("{$AUTHENTICATION_TOKEN_CODE} Exception while generating token")
  }
}

/**
 * Exception types returned as cause for an TwilioVerifyException on validation errors.
 *
 * @param message associated message of the exception.
 */
sealed class InputException(override val message: String) : Exception(message)
object ExpiredChallengeException : InputException("Expired challenge can not be updated")
object AlreadyUpdatedChallengeException : InputException("Responded challenge can not be updated")
object NotUpdatedChallengeException : InputException("Challenge was not updated")
object InvalidChallengeException : InputException("Invalid challenge for received factor")
object EmptyChallengeSidException : InputException("Empty challenge sid")
data class InvalidUpdateChallengePayloadException(private val factorType: FactorType) :
  InputException("Invalid update challenge payload for factor $factorType")

object EmptyFactorSidException : InputException("Empty factor sid")
object WrongFactorException : InputException("Wrong factor for challenge")
object InvalidFactorException : InputException("Invalid factor")
object SignatureFieldsException : InputException("Signature fields or response not set")
