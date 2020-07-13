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

class TwilioVerifyException(
  cause: Throwable,
  errorCode: ErrorCode
) : Exception(errorCode.message, cause) {
  enum class ErrorCode(val message: String) {
    NetworkError("{$NETWORK_CODE} Exception while calling API"),
    MapperError("{$MAPPER_CODE} Exception while mapping an entity"),
    StorageError("{$STORAGE_CODE} Exception while storing/loading an entity"),
    InputError("{$INPUT_CODE} Exception while loading input"),
    KeyStorageError("{$KEY_STORAGE_CODE} Exception while storing/loading key pairs"),
    InitializationError("{$INITIALIZATION_CODE} Exception while initializing"),
    AuthenticationTokenError("{$AUTHENTICATION_TOKEN_CODE} Exception while generating token")
  }
}
