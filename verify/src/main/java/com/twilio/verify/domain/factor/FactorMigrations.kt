/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import android.content.SharedPreferences
import com.twilio.verify.data.Entry
import com.twilio.verify.data.Migration
import org.json.JSONObject

internal class FactorMigrations(
  private val sharedPreferences: SharedPreferences,
  private val factorMapper: FactorMapper = FactorMapper()
) {
  fun migrations(): List<Migration> {

    val migrationV1ToV2 = object : Migration {
      override val startVersion: Int = 1
      override val endVersion: Int = 2

      override fun migrate(data: List<String>): List<Entry> {
        val factors = sharedPreferences.all.values.filterIsInstance<String>()
        return factors.filter { factorMapper.isFactor(it) }
            .map { JSONObject(it) }
            .map {
              Entry(
                  factorMapper.getSid(it), it.toString()
              )
            }
            .apply {
              forEach {
                sharedPreferences.edit()
                    .remove(it.key)
                    .apply()
              }
            }
      }
    }

    return listOf(migrationV1ToV2)
  }
}