/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

class TwilioVerifyException(
  cause: Throwable,
  errorCode: ErrorCode
) : Exception(errorCode.message, cause) {

  enum class ErrorCode(val message: String) {
    NetworkError("Exception while calling API"),
    MapperError("Exception while mapping an entity"),
    StorageError("Exception while storing/loading an entity"),
    InputError("Exception while loading input"),
    KeyStorageError("Exception while storing/loading key pairs")
  }
}
