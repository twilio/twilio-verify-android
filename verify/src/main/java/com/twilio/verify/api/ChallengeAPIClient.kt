package com.twilio.verify.api

import android.content.Context
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.NetworkAdapter
import com.twilio.verify.networking.NetworkProvider

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal class ChallengeAPIClient(
  private val networkProvider: NetworkProvider = NetworkAdapter(),
  private val context: Context,
  private val authorization: Authorization
) {

}