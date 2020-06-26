/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample.networking

import com.twilio.verify.sample.model.EnrollmentResponse
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url
import java.io.IOException

interface SampleBackendAPIClient {
  @POST @FormUrlEncoded
  fun enrollment(
    @Field("identity") identity: String,
    @Url url: String
  ): Call<EnrollmentResponse>?
}

@JvmOverloads fun backendAPIClient(
  enrollmentUrl: String,
  okHttpClient: OkHttpClient = OkHttpClient()
): SampleBackendAPIClient {
  val url = enrollmentUrl.toHttpUrl()
  val retrofit = Retrofit.Builder()
      .baseUrl("${url.scheme}://${url.host}")
      .addConverterFactory(GsonConverterFactory.create())
      .client(okHttpClient)
      .build()
  return retrofit.create(SampleBackendAPIClient::class.java)
}

fun SampleBackendAPIClient.getEnrollmentResponse(
  identity: String,
  enrollmentUrl: String,
  success: (EnrollmentResponse) -> Unit,
  error: (Throwable) -> Unit
) {
  val call = enrollment(identity, enrollmentUrl)
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
          response.body()
              ?.takeIf {
                !it.token.isNullOrBlank() && !it.factorType.isNullOrBlank()
                    && !it.identity.isNullOrBlank() && !it.serviceSid.isNullOrBlank()
              } ?: throw IOException(
              response.errorBody()
                  ?.string() ?: "Invalid response"
          )
        success(enrollmentResponse)
      } catch (e: Exception) {
        error(e)
      }
    }
  })
}