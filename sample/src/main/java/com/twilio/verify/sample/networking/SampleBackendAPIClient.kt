/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample.networking

import com.twilio.verify.sample.BuildConfig
import com.twilio.verify.sample.model.EnrollmentResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.io.IOException

interface SampleBackendAPIClient {
  @POST(BuildConfig.ENROLLMENT_URL) @FormUrlEncoded
  fun enrollment(@Field("identity") identity: String): Call<EnrollmentResponse>?
}

fun backendAPIClient(okHttpClient: OkHttpClient): SampleBackendAPIClient {
  val retrofit = Retrofit.Builder()
      .baseUrl(BuildConfig.ENROLLMENT_URL.substringBeforeLast('/'))
      .addConverterFactory(GsonConverterFactory.create())
      .client(okHttpClient)
      .build()
  return retrofit.create(SampleBackendAPIClient::class.java)
}

fun SampleBackendAPIClient.getEnrollmentResponse(
  identity: String,
  success: (EnrollmentResponse) -> Unit,
  error: (Throwable) -> Unit
) {
  val call = enrollment(identity)
  call?.enqueue(object : retrofit2.Callback<EnrollmentResponse> {
    override fun onFailure(
      call: Call<EnrollmentResponse>,
      t: Throwable
    ) {
      error(t)
    }

    override fun onResponse(
      call: Call<EnrollmentResponse>,
      response: Response<EnrollmentResponse>
    ) {
      try {
        val enrollmentResponse =
          response.body() ?: throw IOException("${response.errorBody()?.string()}")
        success(enrollmentResponse)
      } catch (e: Exception) {
        error(e)
      }
    }
  })
}