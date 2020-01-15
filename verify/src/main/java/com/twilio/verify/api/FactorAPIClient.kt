/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.api

import android.content.Context
import com.twilio.verify.domain.factor.accountSidKey
import com.twilio.verify.domain.factor.entitySidKey
import com.twilio.verify.domain.factor.friendlyNameKey
import com.twilio.verify.domain.factor.models.FactorBuilder
import com.twilio.verify.domain.factor.sidKey
import com.twilio.verify.networking.Authorization
import org.json.JSONObject

internal class FactorAPIClient(
  context: Context,
  authorization: Authorization
) {
  fun create(
    factorBuilder: FactorBuilder,
    success: (JSONObject) -> Unit,
    error: () -> Unit
  ) {
    success(
        JSONObject()
            .put(sidKey, "sid123")
            .put(friendlyNameKey, factorBuilder.friendlyName)
            .put(accountSidKey, "accountSid123")
            .put(entitySidKey, "entitySid123")
    )
  }
}