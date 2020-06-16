/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

class TwilioVerifyException(
  cause: Throwable,
  errorCode: ErrorCode
) : Exception(errorCode.message, cause) {

  enum class ErrorCode(val message: String, val code: Int) {
    NetworkError("(${NetworkError.code}) Exception while calling API", 68001),
    MapperError("(${MapperError.code}) Exception while mapping an entity", 68002),
    StorageError("(${StorageError.code}) Exception while storing/loading an entity", 68003),
    InputError("(${InputError.code}) Exception while loading input", 68004),
    KeyStorageError("(${KeyStorageError.code}) Exception while storing/loading key pairs", 68005),
    InitializationError("(${InitializationError.code}) Exception while initializing", 68006),
    AuthenticationTokenError("(${AuthenticationTokenError.code}) Exception while generating token", 68007)
  }
}
