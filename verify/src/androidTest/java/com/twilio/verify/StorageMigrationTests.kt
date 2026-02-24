/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.twilio.security.storage.encryptedPreferences
import com.twilio.verify.data.CURRENT_VERSION
import com.twilio.verify.data.PROVIDER
import com.twilio.verify.data.Storage
import com.twilio.verify.data.toRFC3339Date
import com.twilio.verify.domain.factor.ACCOUNT_SID_KEY
import com.twilio.verify.domain.factor.CONFIG_KEY
import com.twilio.verify.domain.factor.CREDENTIAL_SID_KEY
import com.twilio.verify.domain.factor.DATE_CREATED_KEY
import com.twilio.verify.domain.factor.FRIENDLY_NAME_KEY
import com.twilio.verify.domain.factor.FactorMigrations
import com.twilio.verify.domain.factor.IDENTITY_KEY
import com.twilio.verify.domain.factor.KEY_PAIR_ALIAS_KEY
import com.twilio.verify.domain.factor.SERVICE_SID_KEY
import com.twilio.verify.domain.factor.SID_KEY
import com.twilio.verify.domain.factor.STATUS_KEY
import com.twilio.verify.domain.factor.TYPE_KEY
import com.twilio.verify.models.FactorStatus.Unverified
import com.twilio.verify.models.FactorType.PUSH
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyStore
import java.util.Date

class StorageMigrationTests {
  private val alias = "test"
  private val storageName = "testStorage"
  private val context: Context = ApplicationProvider.getApplicationContext()
  private val sharedPreferences = context.getSharedPreferences(storageName, Context.MODE_PRIVATE)
  private val encryptedSharedPreferences =
    context.getSharedPreferences("$storageName.enc", Context.MODE_PRIVATE)
  private val encryptedStorage = encryptedPreferences(alias, encryptedSharedPreferences)

  @After
  fun tearDown() {
    sharedPreferences
      .edit()
      .clear()
      .apply()
    encryptedSharedPreferences
      .edit()
      .clear()
      .apply()
    val keyStore =
      KeyStore
        .getInstance(PROVIDER)
        .apply {
          load(null)
        }
    keyStore.deleteEntry(alias)
  }

  @Test
  fun testMigrateFromV1ToV2() {
    val factorSids = listOf("sid123", "sid345", "sid678")
    val factors =
      factorSids
        .map { it to createFactorDataForV1(it) }
        .toMap()
    factors.forEach {
      sharedPreferences
        .edit()
        .putString(it.key, it.value)
        .apply()
      assertTrue(sharedPreferences.contains(it.key))
    }
    sharedPreferences
      .edit()
      .remove(CURRENT_VERSION)
      .apply()
    val factorMigrations = FactorMigrations(sharedPreferences)
    val storage = Storage(sharedPreferences, encryptedStorage, factorMigrations.migrations())
    factors.forEach {
      assertTrue(encryptedStorage.contains(it.key))
      assertFalse(sharedPreferences.contains(it.key))
      assertEquals(it.value, storage.get(it.key))
    }
  }

  private fun createFactorDataForV1(sid: String): String =
    JSONObject()
      .put(SID_KEY, sid)
      .put(FRIENDLY_NAME_KEY, "factor name")
      .put(ACCOUNT_SID_KEY, "accountSid123")
      .put(SERVICE_SID_KEY, "serviceSid123")
      .put(IDENTITY_KEY, "identity123")
      .put(TYPE_KEY, PUSH.factorTypeName)
      .put(KEY_PAIR_ALIAS_KEY, "keyPairAlias123")
      .put(STATUS_KEY, Unverified.value)
      .put(CONFIG_KEY, JSONObject().put(CREDENTIAL_SID_KEY, "credentialSid"))
      .put(DATE_CREATED_KEY, toRFC3339Date(Date()))
      .toString()
}
