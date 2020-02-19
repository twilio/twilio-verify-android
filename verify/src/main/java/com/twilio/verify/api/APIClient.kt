package com.twilio.verify.api

import com.twilio.verify.networking.MediaTypeHeader
import com.twilio.verify.networking.MediaTypeValue

/*
 * Copyright (c) 2020, Twilio Inc.
 */
internal const val serviceSidPath = "{ServiceSid}"
internal const val entitySidPath = "{EntitySid}"
internal const val factorSidPath = "{FactorSid}"

internal const val authPayloadParam = "AuthPayload"

open class APIClient {

  protected fun postMediaTypeHeaders(): Map<String, String> =
    mapOf(
        MediaTypeHeader.Accept.type to MediaTypeValue.Json.type,
        MediaTypeHeader.ContentType.type to MediaTypeValue.UrlEncoded.type
    )
}