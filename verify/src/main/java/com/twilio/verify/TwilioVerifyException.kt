/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

private const val NETWORK_CODE = 68001
private const val MAPPER_CODE = 68002
private const val STORAGE_CODE = 68003
private const val INPUT_CODE = 68004
private const val KEY_STORAGE_CODE = 68005
private const val INITIALIZATION_CODE = 68006
private const val AUTHENTICATION_TOKEN_CODE = 68007

/**
 * Exception types returned by the TwilioVerify SDK. It encompasses different types of errors that
 * have their own associated reasons and codes.
 * @property cause: Associated reason of the exception.
 * @property errorCode: Associated error code of the exception.
 *
 * - **NetworkError:** An error occurred while calling the API.
 * - **MapperError:** An error occurred while mapping an entity.
 * - **StorageError:** An error occurred while storing/loading an entity.
 * - **InputError:** An error occurred while loading input.
 * - **KeyStorageError:** An error occurred while storing/loading keypairs.
 * - **InitializationError:** An error occurred while initializing a class.
 * - **AuthenticationTokenError:** An error occurred while generating a token.
 *
 * **Error Codes**
 * - **NetworkError:** 68001
 * - **MapperError:** 68002
 * - **StorageError:** 68003
 * - **InputError:** 68004
 * - **KeyStorageError:** 68005
 * - **InitializationError:** 68006
 * - **AuthenticationTokenError:** 68007
 */
class TwilioVerifyException(
  cause: Throwable,
  errorCode: ErrorCode
) : Exception(errorCode.message, cause) {
  enum class ErrorCode(val message: String) {
    NetworkError("{$NETWORK_CODE} Exception while calling the API"),
    MapperError("{$MAPPER_CODE} Exception while mapping an entity"),
    StorageError("{$STORAGE_CODE} Exception while storing/loading an entity"),
    InputError("{$INPUT_CODE} Exception while loading input"),
    KeyStorageError("{$KEY_STORAGE_CODE} Exception while storing/loading key pairs"),
    InitializationError("{$INITIALIZATION_CODE} Exception while initializing"),
    AuthenticationTokenError("{$AUTHENTICATION_TOKEN_CODE} Exception while generating token")
  }
}
