package com.twilio.verify.domain.challenge

import com.twilio.verify.ErrorCodeMatcher
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.models.ChallengeStatus
import org.hamcrest.Matchers
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FactorChallengeListMapperTest {

  private val challengeListMapper = ChallengeListMapper()

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  @Test
  fun `Map a valid response from API should return a list of challenges`() {
    val expectedChallenges = JSONArray(
        listOf(
            challengeJSONObject("sid123"),
            challengeJSONObject("sid456")
        )
    )
    val expectedMetadata = metaJSONObject()
    val jsonObject = JSONObject().apply {
      put(challengesKey, expectedChallenges)
      put(metaKey, expectedMetadata)
    }

    val challengeList = challengeListMapper.fromApi(jsonObject)
    assertEquals(challengeList.challenges.size, expectedChallenges.length())
    assertEquals(expectedMetadata.getInt(pageKey), challengeList.metadata.page)
    assertEquals(expectedMetadata.getInt(pageSizeKey), challengeList.metadata.pageSize)
    assertEquals(expectedMetadata.getString(nextPageKey), challengeList.metadata.nextPageURL)
    assertEquals(expectedMetadata.getString(key), challengeList.metadata.key)

    val firstJSONChallenge = expectedChallenges.getJSONObject(0)
    val firstChallenge = challengeList.challenges[0] as FactorChallenge
    assertEquals(firstJSONChallenge.getString(sidKey), firstChallenge.sid)
    assertEquals(firstJSONChallenge.getString(factorSidKey), firstChallenge.factorSid)
    assertEquals(
        fromRFC3339Date(firstJSONChallenge.getString(createdDateKey)), firstChallenge.createdAt
    )
    assertEquals(
        fromRFC3339Date(firstJSONChallenge.getString(updatedDateKey)), firstChallenge.updatedAt
    )
    assertEquals(firstJSONChallenge.getString(statusKey), firstChallenge.status.value)

    val secondJSONChallenge = expectedChallenges.getJSONObject(1)
    val secondChallenge = challengeList.challenges[1] as FactorChallenge
    assertEquals(secondJSONChallenge.getString(sidKey), secondChallenge.sid)
    assertEquals(secondJSONChallenge.getString(factorSidKey), secondChallenge.factorSid)
    assertEquals(
        fromRFC3339Date(secondJSONChallenge.getString(createdDateKey)),
        secondChallenge.createdAt
    )
    assertEquals(
        fromRFC3339Date(secondJSONChallenge.getString(updatedDateKey)),
        secondChallenge.updatedAt
    )
    assertEquals(secondJSONChallenge.getString(statusKey), secondChallenge.status.value)
  }

  @Test
  fun `Map response without challenges should throw an error`() {
    val expectedMetaData = metaJSONObject()
    val jsonObject = JSONObject().apply {
      put(metaKey, expectedMetaData)
    }

    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeListMapper.fromApi(jsonObject)
  }

  fun `Map response without metadata should throw an error`() {
    val expectedChallenges = JSONArray(
        listOf(
            challengeJSONObject("sid123"),
            (challengeJSONObject("sid456"))
        )
    )
    val jsonObject = JSONObject().apply {
      put(metaKey, expectedChallenges)
    }

    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeListMapper.fromApi(jsonObject)
  }

  @Test
  fun `Map response with invalid metadata should throw an error`() {
    val metadata = JSONObject().apply {
      put(pageKey, 1)
      put(pageSizeKey, 10)
      put(nextPageKey, 14)
      put(key, "key")
    }
    val jsonObject = JSONObject().apply {
      put(metaKey, metadata)
    }

    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeListMapper.fromApi(jsonObject)
  }

  private fun challengeJSONObject(sid: String): JSONObject {
    return JSONObject().apply {
      put(sidKey, sid)
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.value)
      put(detailsKey, JSONObject().apply {
        put(messageKey, "message123")
        put(fieldsKey, JSONArray().apply {
          put(0, JSONObject().apply {
            put(labelKey, "label123")
            put(valueKey, "value123")
          })
        })
        put(dateKey, "2020-02-19T16:39:57-08:00")
      }
          .toString())
      put(hiddenDetailsKey, JSONObject().apply {
        put("key1", "value1")
      }
          .toString())
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
  }

  private fun metaJSONObject(): JSONObject {
    return JSONObject().apply {
      put(pageKey, 1)
      put(pageSizeKey, 10)
      put(nextPageKey, "next_page")
      put(key, "key")
    }
  }
}