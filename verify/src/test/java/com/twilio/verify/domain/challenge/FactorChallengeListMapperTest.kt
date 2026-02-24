package com.twilio.verify.domain.challenge

import com.twilio.verify.ErrorCodeMatcher
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.data.fromRFC3339Date
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.models.ChallengeStatus
import org.hamcrest.Matchers
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val PREVIOUS_PAGE_TOKEN = "previousPageToken"
private const val NEXT_PAGE_TOKEN = "nextPageToken"

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FactorChallengeListMapperTest {
  private val challengeListMapper = ChallengeListMapper()

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  @Test
  fun `Map a valid response from API should return a list of challenges`() {
    val expectedChallenges =
      JSONArray(
        listOf(
          challengeJSONObject("sid123"),
          challengeJSONObject("sid456"),
        ),
      )
    val expectedMetadata = metaJSONObject()
    val jsonObject =
      JSONObject().apply {
        put(CHALLENGES_KEY, expectedChallenges)
        put(META_KEY, expectedMetadata)
      }

    val challengeList = challengeListMapper.fromApi(jsonObject)
    assertEquals(challengeList.challenges.size, expectedChallenges.length())
    assertEquals(expectedMetadata.getInt(PAGE_KEY), challengeList.metadata.page)
    assertEquals(expectedMetadata.getInt(PAGE_SIZE_KEY), challengeList.metadata.pageSize)
    assertEquals(PREVIOUS_PAGE_TOKEN, challengeList.metadata.previousPageToken)
    assertEquals(NEXT_PAGE_TOKEN, challengeList.metadata.nextPageToken)

    val firstJSONChallenge = expectedChallenges.getJSONObject(0)
    val firstChallenge = challengeList.challenges[0] as FactorChallenge
    assertEquals(firstJSONChallenge.getString(SID_KEY), firstChallenge.sid)
    assertEquals(firstJSONChallenge.getString(FACTOR_SID_KEY), firstChallenge.factorSid)
    assertEquals(
      fromRFC3339Date(firstJSONChallenge.getString(CREATED_DATE_KEY)),
      firstChallenge.createdAt,
    )
    assertEquals(
      fromRFC3339Date(firstJSONChallenge.getString(UPDATED_DATE_KEY)),
      firstChallenge.updatedAt,
    )
    assertEquals(firstJSONChallenge.getString(STATUS_KEY), firstChallenge.status.value)

    val secondJSONChallenge = expectedChallenges.getJSONObject(1)
    val secondChallenge = challengeList.challenges[1] as FactorChallenge
    assertEquals(secondJSONChallenge.getString(SID_KEY), secondChallenge.sid)
    assertEquals(secondJSONChallenge.getString(FACTOR_SID_KEY), secondChallenge.factorSid)
    assertEquals(
      fromRFC3339Date(secondJSONChallenge.getString(CREATED_DATE_KEY)),
      secondChallenge.createdAt,
    )
    assertEquals(
      fromRFC3339Date(secondJSONChallenge.getString(UPDATED_DATE_KEY)),
      secondChallenge.updatedAt,
    )
    assertEquals(secondJSONChallenge.getString(STATUS_KEY), secondChallenge.status.value)
  }

  @Test
  fun `Map a valid response and no next page url from API should return a list of challenges`() {
    val expectedChallenges =
      JSONArray(
        listOf(
          challengeJSONObject("sid123"),
          challengeJSONObject("sid456"),
        ),
      )
    val expectedMetadata = metaJSONObject(nextPageUrl = null)
    val jsonObject =
      JSONObject().apply {
        put(CHALLENGES_KEY, expectedChallenges)
        put(META_KEY, expectedMetadata)
      }

    val challengeList = challengeListMapper.fromApi(jsonObject)
    assertEquals(challengeList.challenges.size, expectedChallenges.length())
    assertEquals(expectedMetadata.getInt(PAGE_KEY), challengeList.metadata.page)
    assertEquals(expectedMetadata.getInt(PAGE_SIZE_KEY), challengeList.metadata.pageSize)
    assertEquals(PREVIOUS_PAGE_TOKEN, challengeList.metadata.previousPageToken)
    assertNull(challengeList.metadata.nextPageToken)

    val firstJSONChallenge = expectedChallenges.getJSONObject(0)
    val firstChallenge = challengeList.challenges[0] as FactorChallenge
    assertEquals(firstJSONChallenge.getString(SID_KEY), firstChallenge.sid)
    assertEquals(firstJSONChallenge.getString(FACTOR_SID_KEY), firstChallenge.factorSid)
    assertEquals(
      fromRFC3339Date(firstJSONChallenge.getString(CREATED_DATE_KEY)),
      firstChallenge.createdAt,
    )
    assertEquals(
      fromRFC3339Date(firstJSONChallenge.getString(UPDATED_DATE_KEY)),
      firstChallenge.updatedAt,
    )
    assertEquals(firstJSONChallenge.getString(STATUS_KEY), firstChallenge.status.value)

    val secondJSONChallenge = expectedChallenges.getJSONObject(1)
    val secondChallenge = challengeList.challenges[1] as FactorChallenge
    assertEquals(secondJSONChallenge.getString(SID_KEY), secondChallenge.sid)
    assertEquals(secondJSONChallenge.getString(FACTOR_SID_KEY), secondChallenge.factorSid)
    assertEquals(
      fromRFC3339Date(secondJSONChallenge.getString(CREATED_DATE_KEY)),
      secondChallenge.createdAt,
    )
    assertEquals(
      fromRFC3339Date(secondJSONChallenge.getString(UPDATED_DATE_KEY)),
      secondChallenge.updatedAt,
    )
    assertEquals(secondJSONChallenge.getString(STATUS_KEY), secondChallenge.status.value)
  }

  @Test
  fun `Map a valid response and no next page token from API should return a list of challenges`() {
    val expectedChallenges =
      JSONArray(
        listOf(
          challengeJSONObject("sid123"),
          challengeJSONObject("sid456"),
        ),
      )
    val expectedMetadata = metaJSONObject(nextPageUrl = "http://www.twilio.com")
    val jsonObject =
      JSONObject().apply {
        put(CHALLENGES_KEY, expectedChallenges)
        put(META_KEY, expectedMetadata)
      }

    val challengeList = challengeListMapper.fromApi(jsonObject)
    assertEquals(challengeList.challenges.size, expectedChallenges.length())
    assertEquals(expectedMetadata.getInt(PAGE_KEY), challengeList.metadata.page)
    assertEquals(expectedMetadata.getInt(PAGE_SIZE_KEY), challengeList.metadata.pageSize)
    assertEquals(PREVIOUS_PAGE_TOKEN, challengeList.metadata.previousPageToken)
    assertNull(challengeList.metadata.nextPageToken)

    val firstJSONChallenge = expectedChallenges.getJSONObject(0)
    val firstChallenge = challengeList.challenges[0] as FactorChallenge
    assertEquals(firstJSONChallenge.getString(SID_KEY), firstChallenge.sid)
    assertEquals(firstJSONChallenge.getString(FACTOR_SID_KEY), firstChallenge.factorSid)
    assertEquals(
      fromRFC3339Date(firstJSONChallenge.getString(CREATED_DATE_KEY)),
      firstChallenge.createdAt,
    )
    assertEquals(
      fromRFC3339Date(firstJSONChallenge.getString(UPDATED_DATE_KEY)),
      firstChallenge.updatedAt,
    )
    assertEquals(firstJSONChallenge.getString(STATUS_KEY), firstChallenge.status.value)

    val secondJSONChallenge = expectedChallenges.getJSONObject(1)
    val secondChallenge = challengeList.challenges[1] as FactorChallenge
    assertEquals(secondJSONChallenge.getString(SID_KEY), secondChallenge.sid)
    assertEquals(secondJSONChallenge.getString(FACTOR_SID_KEY), secondChallenge.factorSid)
    assertEquals(
      fromRFC3339Date(secondJSONChallenge.getString(CREATED_DATE_KEY)),
      secondChallenge.createdAt,
    )
    assertEquals(
      fromRFC3339Date(secondJSONChallenge.getString(UPDATED_DATE_KEY)),
      secondChallenge.updatedAt,
    )
    assertEquals(secondJSONChallenge.getString(STATUS_KEY), secondChallenge.status.value)
  }

  @Test
  fun `Map a valid response and invalid next page url from API should return a list of challenges`() {
    val expectedChallenges =
      JSONArray(
        listOf(
          challengeJSONObject("sid123"),
          challengeJSONObject("sid456"),
        ),
      )
    val expectedMetadata = metaJSONObject(nextPageUrl = "twilio")
    val jsonObject =
      JSONObject().apply {
        put(CHALLENGES_KEY, expectedChallenges)
        put(META_KEY, expectedMetadata)
      }

    val challengeList = challengeListMapper.fromApi(jsonObject)
    assertEquals(challengeList.challenges.size, expectedChallenges.length())
    assertEquals(expectedMetadata.getInt(PAGE_KEY), challengeList.metadata.page)
    assertEquals(expectedMetadata.getInt(PAGE_SIZE_KEY), challengeList.metadata.pageSize)
    assertEquals(PREVIOUS_PAGE_TOKEN, challengeList.metadata.previousPageToken)
    assertNull(challengeList.metadata.nextPageToken)

    val firstJSONChallenge = expectedChallenges.getJSONObject(0)
    val firstChallenge = challengeList.challenges[0] as FactorChallenge
    assertEquals(firstJSONChallenge.getString(SID_KEY), firstChallenge.sid)
    assertEquals(firstJSONChallenge.getString(FACTOR_SID_KEY), firstChallenge.factorSid)
    assertEquals(
      fromRFC3339Date(firstJSONChallenge.getString(CREATED_DATE_KEY)),
      firstChallenge.createdAt,
    )
    assertEquals(
      fromRFC3339Date(firstJSONChallenge.getString(UPDATED_DATE_KEY)),
      firstChallenge.updatedAt,
    )
    assertEquals(firstJSONChallenge.getString(STATUS_KEY), firstChallenge.status.value)

    val secondJSONChallenge = expectedChallenges.getJSONObject(1)
    val secondChallenge = challengeList.challenges[1] as FactorChallenge
    assertEquals(secondJSONChallenge.getString(SID_KEY), secondChallenge.sid)
    assertEquals(secondJSONChallenge.getString(FACTOR_SID_KEY), secondChallenge.factorSid)
    assertEquals(
      fromRFC3339Date(secondJSONChallenge.getString(CREATED_DATE_KEY)),
      secondChallenge.createdAt,
    )
    assertEquals(
      fromRFC3339Date(secondJSONChallenge.getString(UPDATED_DATE_KEY)),
      secondChallenge.updatedAt,
    )
    assertEquals(secondJSONChallenge.getString(STATUS_KEY), secondChallenge.status.value)
  }

  @Test
  fun `Map a valid response and no previous page url from API should return a list of challenges`() {
    val expectedChallenges =
      JSONArray(
        listOf(
          challengeJSONObject("sid123"),
          challengeJSONObject("sid456"),
        ),
      )
    val expectedMetadata = metaJSONObject(previousPageUrl = null)
    val jsonObject =
      JSONObject().apply {
        put(CHALLENGES_KEY, expectedChallenges)
        put(META_KEY, expectedMetadata)
      }

    val challengeList = challengeListMapper.fromApi(jsonObject)
    assertEquals(challengeList.challenges.size, expectedChallenges.length())
    assertEquals(expectedMetadata.getInt(PAGE_KEY), challengeList.metadata.page)
    assertEquals(expectedMetadata.getInt(PAGE_SIZE_KEY), challengeList.metadata.pageSize)
    assertNull(challengeList.metadata.previousPageToken)
    assertEquals(NEXT_PAGE_TOKEN, challengeList.metadata.nextPageToken)

    val firstJSONChallenge = expectedChallenges.getJSONObject(0)
    val firstChallenge = challengeList.challenges[0] as FactorChallenge
    assertEquals(firstJSONChallenge.getString(SID_KEY), firstChallenge.sid)
    assertEquals(firstJSONChallenge.getString(FACTOR_SID_KEY), firstChallenge.factorSid)
    assertEquals(
      fromRFC3339Date(firstJSONChallenge.getString(CREATED_DATE_KEY)),
      firstChallenge.createdAt,
    )
    assertEquals(
      fromRFC3339Date(firstJSONChallenge.getString(UPDATED_DATE_KEY)),
      firstChallenge.updatedAt,
    )
    assertEquals(firstJSONChallenge.getString(STATUS_KEY), firstChallenge.status.value)

    val secondJSONChallenge = expectedChallenges.getJSONObject(1)
    val secondChallenge = challengeList.challenges[1] as FactorChallenge
    assertEquals(secondJSONChallenge.getString(SID_KEY), secondChallenge.sid)
    assertEquals(secondJSONChallenge.getString(FACTOR_SID_KEY), secondChallenge.factorSid)
    assertEquals(
      fromRFC3339Date(secondJSONChallenge.getString(CREATED_DATE_KEY)),
      secondChallenge.createdAt,
    )
    assertEquals(
      fromRFC3339Date(secondJSONChallenge.getString(UPDATED_DATE_KEY)),
      secondChallenge.updatedAt,
    )
    assertEquals(secondJSONChallenge.getString(STATUS_KEY), secondChallenge.status.value)
  }

  @Test
  fun `Map a valid response and no previous page token from API should return a list of challenges`() {
    val expectedChallenges =
      JSONArray(
        listOf(
          challengeJSONObject("sid123"),
          challengeJSONObject("sid456"),
        ),
      )
    val expectedMetadata = metaJSONObject(previousPageUrl = "http://www.twilio.com")
    val jsonObject =
      JSONObject().apply {
        put(CHALLENGES_KEY, expectedChallenges)
        put(META_KEY, expectedMetadata)
      }

    val challengeList = challengeListMapper.fromApi(jsonObject)
    assertEquals(challengeList.challenges.size, expectedChallenges.length())
    assertEquals(expectedMetadata.getInt(PAGE_KEY), challengeList.metadata.page)
    assertEquals(expectedMetadata.getInt(PAGE_SIZE_KEY), challengeList.metadata.pageSize)
    assertNull(challengeList.metadata.previousPageToken)
    assertEquals(NEXT_PAGE_TOKEN, challengeList.metadata.nextPageToken)

    val firstJSONChallenge = expectedChallenges.getJSONObject(0)
    val firstChallenge = challengeList.challenges[0] as FactorChallenge
    assertEquals(firstJSONChallenge.getString(SID_KEY), firstChallenge.sid)
    assertEquals(firstJSONChallenge.getString(FACTOR_SID_KEY), firstChallenge.factorSid)
    assertEquals(
      fromRFC3339Date(firstJSONChallenge.getString(CREATED_DATE_KEY)),
      firstChallenge.createdAt,
    )
    assertEquals(
      fromRFC3339Date(firstJSONChallenge.getString(UPDATED_DATE_KEY)),
      firstChallenge.updatedAt,
    )
    assertEquals(firstJSONChallenge.getString(STATUS_KEY), firstChallenge.status.value)

    val secondJSONChallenge = expectedChallenges.getJSONObject(1)
    val secondChallenge = challengeList.challenges[1] as FactorChallenge
    assertEquals(secondJSONChallenge.getString(SID_KEY), secondChallenge.sid)
    assertEquals(secondJSONChallenge.getString(FACTOR_SID_KEY), secondChallenge.factorSid)
    assertEquals(
      fromRFC3339Date(secondJSONChallenge.getString(CREATED_DATE_KEY)),
      secondChallenge.createdAt,
    )
    assertEquals(
      fromRFC3339Date(secondJSONChallenge.getString(UPDATED_DATE_KEY)),
      secondChallenge.updatedAt,
    )
    assertEquals(secondJSONChallenge.getString(STATUS_KEY), secondChallenge.status.value)
  }

  @Test
  fun `Map response without challenges key should throw an error`() {
    val expectedMetaData = metaJSONObject()
    val jsonObject =
      JSONObject().apply {
        put(META_KEY, expectedMetaData)
      }

    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf<Throwable>(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeListMapper.fromApi(jsonObject)
  }

  fun `Map response without metadata key should throw an error`() {
    val expectedChallenges =
      JSONArray(
        listOf(
          challengeJSONObject("sid123"),
          (challengeJSONObject("sid456")),
        ),
      )
    val jsonObject =
      JSONObject().apply {
        put(META_KEY, expectedChallenges)
      }

    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf<Throwable>(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeListMapper.fromApi(jsonObject)
  }

  @Test
  fun `Map response with invalid metadata should throw an error`() {
    val metadata =
      JSONObject().apply {
        put(PAGE_KEY, 1)
        put(PAGE_SIZE_KEY, 10)
        put(NEXT_PAGE_KEY, 14)
      }
    val jsonObject =
      JSONObject().apply {
        put(META_KEY, metadata)
      }

    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf<Throwable>(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeListMapper.fromApi(jsonObject)
  }

  private fun challengeJSONObject(sid: String): JSONObject =
    JSONObject().apply {
      put(SID_KEY, sid)
      put(FACTOR_SID_KEY, "factorSid123")
      put(CREATED_DATE_KEY, "2020-02-19T16:39:57-08:00")
      put(UPDATED_DATE_KEY, "2020-02-21T18:39:57-08:00")
      put(STATUS_KEY, ChallengeStatus.Pending.value)
      put(
        DETAILS_KEY,
        JSONObject().apply {
          put(MESSAGE_KEY, "message123")
          put(
            FIELDS_KEY,
            JSONArray().apply {
              put(
                0,
                JSONObject().apply {
                  put(LABEL_KEY, "label123")
                  put(VALUE_KEY, "value123")
                },
              )
            },
          )
          put(DATE_KEY, "2020-02-19T16:39:57-08:00")
        },
      )
      put(
        HIDDEN_DETAILS_KEY,
        JSONObject().apply {
          put("key1", "value1")
        },
      )
      put(EXPIRATION_DATE_KEY, "2020-02-27T08:50:57-08:00")
    }

  private fun metaJSONObject(
    previousPageUrl: String? = "https://www.twilio.com?$PAGE_TOKEN_KEY=$PREVIOUS_PAGE_TOKEN",
    nextPageUrl: String? = "https://www.twilio.com?$PAGE_TOKEN_KEY=$NEXT_PAGE_TOKEN",
  ): JSONObject =
    JSONObject().apply {
      put(PAGE_KEY, 0)
      put(PAGE_SIZE_KEY, 10)
      put(PREVIOUS_PAGE_KEY, previousPageUrl)
      put(NEXT_PAGE_KEY, nextPageUrl)
    }
}
