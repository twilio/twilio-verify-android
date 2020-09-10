/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample.networking

import com.twilio.verify.sample.model.AccessTokenResponse
import java.io.IOException
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

interface SampleBackendAPIClient {
  @POST @FormUrlEncoded
  fun accessTokens(
    @Field("identity") identity: String,
    @Url url: String
  ): Call<AccessTokenResponse>?
}

@JvmOverloads fun backendAPIClient(
  accessTokenUrl: String,
  okHttpClient: OkHttpClient = OkHttpClient()
): SampleBackendAPIClient {
  val url = accessTokenUrl.toHttpUrl()
  val retrofit = Retrofit.Builder()
    .baseUrl("${url.scheme}://${url.host}")
    .addConverterFactory(GsonConverterFactory.create())
    .client(okHttpClient)
    .build()
  return retrofit.create(SampleBackendAPIClient::class.java)
}

fun SampleBackendAPIClient.getAccessTokenResponse(
  identity: String,
  accessTokenUrl: String,
  success: (AccessTokenResponse) -> Unit,
  error: (Throwable) -> Unit
) {
  val call = accessTokens(identity, accessTokenUrl)
  call?.enqueue(object : retrofit2.Callback<AccessTokenResponse> {
    override fun onFailure(
      call: Call<AccessTokenResponse>,
      t: Throwable
    ) {
      error(t)
    }

    override fun onResponse(
      call: Call<AccessTokenResponse>,
      response: Response<AccessTokenResponse>
    ) {
      try {
        val accessTokenResponse =
          response.body()
            ?.takeIf {
              !it.token.isNullOrBlank() && !it.factorType.isNullOrBlank() &&
                !it.identity.isNullOrBlank() && !it.serviceSid.isNullOrBlank()
            } ?: throw IOException(
            response.errorBody()
              ?.string() ?: "Invalid response"
          )
        success(accessTokenResponse)
      } catch (e: Exception) {
        error(e)
      }
    }
  })
}
