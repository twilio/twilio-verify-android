/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.twilio.security.storage.encryptedPreferences
import com.twilio.verify.data.CURRENT_VERSION
import com.twilio.verify.data.Storage
import com.twilio.verify.data.provider
import com.twilio.verify.data.toRFC3339Date
import com.twilio.verify.domain.factor.FactorMigrations
import com.twilio.verify.domain.factor.accountSidKey
import com.twilio.verify.domain.factor.configKey
import com.twilio.verify.domain.factor.credentialSidKey
import com.twilio.verify.domain.factor.dateCreatedKey
import com.twilio.verify.domain.factor.friendlyNameKey
import com.twilio.verify.domain.factor.identityKey
import com.twilio.verify.domain.factor.keyPairAliasKey
import com.twilio.verify.domain.factor.serviceSidKey
import com.twilio.verify.domain.factor.sidKey
import com.twilio.verify.domain.factor.statusKey
import com.twilio.verify.domain.factor.typeKey
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
    sharedPreferences.edit()
        .clear()
        .apply()
    encryptedSharedPreferences.edit()
        .clear()
        .apply()
    val keyStore = KeyStore.getInstance(provider)
        .apply {
          load(null)
        }
    keyStore.deleteEntry(alias)
  }

  @Test
  fun testMigrateFromV1ToV2() {
    val factorSids = listOf("sid123", "sid345", "sid678")
    val factors = factorSids.map { it to createFactorDataForV1(it) }
        .toMap()
    factors.forEach {
      sharedPreferences.edit()
          .putString(it.key, it.value)
          .apply()
      assertTrue(sharedPreferences.contains(it.key))
    }
    sharedPreferences.edit()
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

  private fun createFactorDataForV1(sid: String): String {
    return JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(serviceSidKey, "serviceSid123")
        .put(identityKey, "identity123")
        .put(typeKey, PUSH.factorTypeName)
        .put(keyPairAliasKey, "keyPairAlias123")
        .put(statusKey, Unverified.value)
        .put(configKey, JSONObject().put(credentialSidKey, "credentialSid"))
        .put(dateCreatedKey, toRFC3339Date(Date()))
        .toString()
  }
}