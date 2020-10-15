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

package com.twilio.verify.domain.challenge

import android.net.Uri
import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.domain.challenge.models.ChallengeListMetadata
import com.twilio.verify.domain.challenge.models.FactorChallengeList
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeList
import java.text.ParseException
import org.json.JSONException
import org.json.JSONObject

internal const val challengesKey = "challenges"
internal const val metaKey = "meta"
internal const val pageKey = "page"
internal const val pageSizeKey = "page_size"
internal const val previousPageKey = "previous_page_url"
internal const val nextPageKey = "next_page_url"
internal const val pageTokenKey = "PageToken"

internal class ChallengeListMapper(
  private val challengeMapper: ChallengeMapper = ChallengeMapper()
) {

  @Throws(TwilioVerifyException::class)
  fun fromApi(jsonObject: JSONObject): ChallengeList {
    try {
      val jsonChallenges = jsonObject.getJSONArray(challengesKey)
      val challenges = ArrayList<Challenge>()
      for (i in 0 until jsonChallenges.length()) {
        challenges.add(challengeMapper.fromApi(jsonChallenges.getJSONObject(i)))
      }
      val meta = jsonObject.getJSONObject(metaKey)
      val metadata =
        ChallengeListMetadata(
          // page from API starts in zero
          page = meta.getInt(pageKey),
          pageSize = meta.getInt(pageSizeKey),
          previousPageToken = meta.optString(previousPageKey)
            ?.let {
              Uri.parse(it)
                .getQueryParameter(
                  pageTokenKey
                )
            },
          nextPageToken = meta.optString(nextPageKey)
            ?.let {
              Uri.parse(it)
                .getQueryParameter(
                  pageTokenKey
                )
            }
        )
      return FactorChallengeList(
        challenges, metadata
      )
    } catch (e: JSONException) {
      Logger.log(Level.Error, e.toString(), e)
      throw TwilioVerifyException(e, MapperError)
    } catch (e: ParseException) {
      Logger.log(Level.Error, e.toString(), e)
      throw TwilioVerifyException(e, MapperError)
    }
  }
}
