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
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

private const val PREFERENCES_NAME = "testPreferences"

@RunWith(RobolectricTestRunner::class)
class FactorMigrationsTest {
  private val context: Context = ApplicationProvider.getApplicationContext()
  private val sharedPreferences =
    context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
  private val factorMapper: FactorMapper = mock()
  private val factorMigrations = FactorMigrations(sharedPreferences, factorMapper)

  @Test
  fun `Migrate from v1 to v2`() {
    val factorSids = listOf("sid123", "sid345", "sid678")
    val factors =
      factorSids
        .map { it to createFactorData(it) }
        .toMap()
    factors.forEach {
      sharedPreferences
        .edit()
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
      factors
        .map { Entry(it.key, it.value) }
        .sortedBy { it.key },
      result.sortedBy { it.key },
    )
    for (factorSid in factorSids) {
      assertFalse(sharedPreferences.contains(factorSid))
    }
  }

  private fun createFactorData(sid: String): String =
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
