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
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException

internal const val CHALLENGES_KEY = "challenges"
internal const val META_KEY = "meta"
internal const val PAGE_KEY = "page"
internal const val PAGE_SIZE_KEY = "page_size"
internal const val PREVIOUS_PAGE_KEY = "previous_page_url"
internal const val NEXT_PAGE_KEY = "next_page_url"
internal const val PAGE_TOKEN_KEY = "PageToken"

internal class ChallengeListMapper(
  private val challengeMapper: ChallengeMapper = ChallengeMapper(),
) {
  @Throws(TwilioVerifyException::class)
  fun fromApi(jsonObject: JSONObject): ChallengeList {
    try {
      val jsonChallenges = jsonObject.getJSONArray(CHALLENGES_KEY)
      val challenges = ArrayList<Challenge>()
      for (i in 0 until jsonChallenges.length()) {
        challenges.add(challengeMapper.fromApi(jsonChallenges.getJSONObject(i)))
      }
      val meta = jsonObject.getJSONObject(META_KEY)
      val metadata =
        ChallengeListMetadata(
          // page from API starts in zero
          page = meta.getInt(PAGE_KEY),
          pageSize = meta.getInt(PAGE_SIZE_KEY),
          previousPageToken =
            meta
              .optString(PREVIOUS_PAGE_KEY)
              ?.let {
                Uri
                  .parse(it)
                  .getQueryParameter(
                    PAGE_TOKEN_KEY,
                  )
              },
          nextPageToken =
            meta
              .optString(NEXT_PAGE_KEY)
              ?.let {
                Uri
                  .parse(it)
                  .getQueryParameter(
                    PAGE_TOKEN_KEY,
                  )
              },
        )
      return FactorChallengeList(
        challenges,
        metadata,
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
