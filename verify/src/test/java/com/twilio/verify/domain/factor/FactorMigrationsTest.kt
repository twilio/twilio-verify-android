/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.data.Entry
import com.twilio.verify.data.toRFC3339Date
import com.twilio.verify.models.FactorStatus.Unverified
import com.twilio.verify.models.FactorType.PUSH
import java.util.Date
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val preferencesName = "testPreferences"

@RunWith(RobolectricTestRunner::class)
class FactorMigrationsTest {

  private val context: Context = ApplicationProvider.getApplicationContext()
  private val sharedPreferences =
    context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
  private val factorMapper: FactorMapper = mock()
  private val factorMigrations = FactorMigrations(sharedPreferences, factorMapper)

  @Test
  fun `Migrate from v1 to v2`() {
    val factorSids = listOf("sid123", "sid345", "sid678")
    val factors = factorSids.map { it to createFactorData(it) }
      .toMap()
    factors.forEach {
      sharedPreferences.edit()
        .putString(it.key, it.value)
        .apply()
    }
    argumentCaptor<JSONObject>().apply {
      whenever(factorMapper.getSid(capture())).then {
        factors.filter { it.value == lastValue.toString() }.keys.elementAt(0)
      }
    }
    whenever(factorMapper.isFactor(any())).thenReturn(true)
    val migrationList = factorMigrations.migrations()
    val migration = migrationList[0]
    val result = migration.migrate(factors.values.toList())
    assertEquals(
      factors.map { Entry(it.key, it.value) }
        .sortedBy { it.key },
      result.sortedBy { it.key }
    )
    for (factorSid in factorSids) {
      assertFalse(sharedPreferences.contains(factorSid))
    }
  }

  private fun createFactorData(sid: String): String {
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
