/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
