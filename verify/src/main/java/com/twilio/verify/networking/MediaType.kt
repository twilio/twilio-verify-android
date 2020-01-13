package com.twilio.verify.networking

enum class MediaType(type: String) {
    UrlEncoded("x-www-form-urlencoded"),
    FormData("form-data")
}