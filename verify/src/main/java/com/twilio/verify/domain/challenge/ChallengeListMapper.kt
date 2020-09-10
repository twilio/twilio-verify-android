package com.twilio.verify.domain.challenge

import android.net.Uri
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
      throw TwilioVerifyException(e, MapperError)
    } catch (e: ParseException) {
      throw TwilioVerifyException(e, MapperError)
    }
  }
}
