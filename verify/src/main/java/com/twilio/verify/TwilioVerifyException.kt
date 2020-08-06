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
 * Exception types returned by the TwilioVerify SDK. It encompasses different types of errors that have their own associated reasons and codes.
 *
 * @constructor
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
         * An error occurred while calling the API. 68001.
         */
        NetworkError("{$NETWORK_CODE} Exception while calling the API"),

        /**
         * An error occurred while mapping an entity. 68002
         *
         */
        MapperError("{$MAPPER_CODE} Exception while mapping an entity"),

        /**
         * An error occurred while storing/loading an entity. 68003
         *
         */
        StorageError("{$STORAGE_CODE} Exception while storing/loading an entity"),

        /**
         * An error occurred while loading input. 68004
         *
         */
        InputError("{$INPUT_CODE} Exception while loading input"),

        /**
         * An error occurred while storing/loading keypairs. 68005
         *
         */
        KeyStorageError("{$KEY_STORAGE_CODE} Exception while storing/loading key pairs"),

        /**
         * An error occurred while initializing a class. 68006
         *
         */
        InitializationError("{$INITIALIZATION_CODE} Exception while initializing"),

        /**
         * An error occurred while generating a token. 68007
         *
         */
        AuthenticationTokenError("{$AUTHENTICATION_TOKEN_CODE} Exception while generating token")
    }
}
