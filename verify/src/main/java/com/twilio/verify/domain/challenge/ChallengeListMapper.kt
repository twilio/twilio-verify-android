package com.twilio.verify.domain.challenge

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.ChallengeListMetadata
import com.twilio.verify.models.FactorChallengeList
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException

internal const val challengesKey = "challenges"
internal const val metaKey = "meta"
internal const val pageKey = "page"
internal const val pageSizeKey = "page_size"
internal const val nextPageKey = "next_page_url"
internal const val key = "key"

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
      val metadata = ChallengeListMetadata(
          page = meta.getInt(pageKey),
          pageSize = meta.getInt(pageSizeKey),
          nextPageURL = meta.getString(nextPageKey),
          key = meta.getString(key)
      )
      return FactorChallengeList(challenges, metadata)
    } catch (e: JSONException) {
      throw TwilioVerifyException(e, MapperError)
    } catch (e: ParseException) {
      throw TwilioVerifyException(e, MapperError)
    }
  }
}