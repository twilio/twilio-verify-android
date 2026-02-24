/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import com.twilio.verify.ErrorCodeMatcher
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.data.fromRFC3339Date
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.models.ChallengeStatus
import org.hamcrest.Matchers.instanceOf
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.ParseException

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ChallengeMapperTest {
  private val challengeMapper = ChallengeMapper()

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  @Test
  fun `Map a valid response from API should return a challenge`() {
    val hiddenDetails = mapOf("key1" to "value1")
    val jsonObject =
      JSONObject().apply {
        put(SID_KEY, "sid123")
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
          JSONObject(hiddenDetails),
        )
        put(EXPIRATION_DATE_KEY, "2020-02-27T08:50:57-08:00")
      }
    val expectedSignatureFieldsHeader =
      jsonObject
        .keys()
        .asSequence()
        .toList()
        .joinToString(
          SIGNATURE_FIELDS_HEADER_SEPARATOR,
        )
    val challenge =
      challengeMapper.fromApi(jsonObject, expectedSignatureFieldsHeader) as FactorChallenge
    assertEquals(jsonObject.getString(SID_KEY), challenge.sid)
    assertEquals(jsonObject.getString(FACTOR_SID_KEY), challenge.factorSid)
    assertEquals(
      expectedSignatureFieldsHeader,
      challenge.signatureFields?.joinToString(SIGNATURE_FIELDS_HEADER_SEPARATOR),
    )
    assertEquals(fromRFC3339Date(jsonObject.getString(CREATED_DATE_KEY)), challenge.createdAt)
    assertEquals(fromRFC3339Date(jsonObject.getString(UPDATED_DATE_KEY)), challenge.updatedAt)
    assertEquals(jsonObject.getString(STATUS_KEY), challenge.status.value)
    assertEquals(jsonObject, challenge.response)
    val details = JSONObject(jsonObject.getString(DETAILS_KEY))
    assertEquals(details.getString(MESSAGE_KEY), challenge.challengeDetails.message)
    assertEquals(
      details
        .getJSONArray(FIELDS_KEY)
        .length(),
      challenge.challengeDetails.fields.size,
    )
    assertEquals(
      details
        .getJSONArray(FIELDS_KEY)
        .getJSONObject(0)
        .getString(LABEL_KEY),
      challenge.challengeDetails.fields[0].label,
    )
    assertEquals(
      details
        .getJSONArray(FIELDS_KEY)
        .getJSONObject(0)
        .getString(VALUE_KEY),
      challenge.challengeDetails.fields[0].value,
    )
    assertEquals(fromRFC3339Date(details.getString(DATE_KEY)), challenge.challengeDetails.date)
    assertEquals(hiddenDetails, challenge.hiddenDetails)
    assertEquals(fromRFC3339Date(jsonObject.getString(EXPIRATION_DATE_KEY)), challenge.expirationDate)
  }

  @Test
  fun `Map a valid response from API and no fields should return a challenge`() {
    val hiddenDetails = mapOf("key1" to "value1")
    val jsonObject =
      JSONObject().apply {
        put(SID_KEY, "sid123")
        put(FACTOR_SID_KEY, "factorSid123")
        put(CREATED_DATE_KEY, "2020-02-19T16:39:57-08:00")
        put(UPDATED_DATE_KEY, "2020-02-21T18:39:57-08:00")
        put(STATUS_KEY, ChallengeStatus.Pending.value)
        put(
          DETAILS_KEY,
          JSONObject().apply {
            put(MESSAGE_KEY, "message123")
            put(DATE_KEY, "2020-02-19T16:39:57-08:00")
          },
        )
        put(
          HIDDEN_DETAILS_KEY,
          JSONObject(hiddenDetails),
        )
        put(EXPIRATION_DATE_KEY, "2020-02-27T08:50:57-08:00")
      }
    val expectedSignatureFieldsHeader =
      jsonObject
        .keys()
        .asSequence()
        .toList()
        .joinToString(
          SIGNATURE_FIELDS_HEADER_SEPARATOR,
        )
    val challenge =
      challengeMapper.fromApi(jsonObject, expectedSignatureFieldsHeader) as FactorChallenge
    assertEquals(jsonObject.getString(SID_KEY), challenge.sid)
    assertEquals(jsonObject.getString(FACTOR_SID_KEY), challenge.factorSid)
    assertEquals(
      expectedSignatureFieldsHeader,
      challenge.signatureFields?.joinToString(SIGNATURE_FIELDS_HEADER_SEPARATOR),
    )
    assertEquals(fromRFC3339Date(jsonObject.getString(CREATED_DATE_KEY)), challenge.createdAt)
    assertEquals(fromRFC3339Date(jsonObject.getString(UPDATED_DATE_KEY)), challenge.updatedAt)
    assertEquals(jsonObject.getString(STATUS_KEY), challenge.status.value)
    assertEquals(jsonObject, challenge.response)
    val details = JSONObject(jsonObject.getString(DETAILS_KEY))
    assertEquals(details.getString(MESSAGE_KEY), challenge.challengeDetails.message)
    assertTrue(challenge.challengeDetails.fields.isEmpty())
    assertEquals(fromRFC3339Date(details.getString(DATE_KEY)), challenge.challengeDetails.date)
    assertEquals(hiddenDetails, challenge.hiddenDetails)
    assertEquals(fromRFC3339Date(jsonObject.getString(EXPIRATION_DATE_KEY)), challenge.expirationDate)
  }

  @Test
  fun `Map a valid response from API and no details date should return a challenge`() {
    val hiddenDetails = mapOf("key1" to "value1")
    val jsonObject =
      JSONObject().apply {
        put(SID_KEY, "sid123")
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
          },
        )
        put(
          HIDDEN_DETAILS_KEY,
          JSONObject(hiddenDetails),
        )
        put(EXPIRATION_DATE_KEY, "2020-02-27T08:50:57-08:00")
      }
    val expectedSignatureFieldsHeader =
      jsonObject
        .keys()
        .asSequence()
        .toList()
        .joinToString(
          SIGNATURE_FIELDS_HEADER_SEPARATOR,
        )
    val challenge =
      challengeMapper.fromApi(jsonObject, expectedSignatureFieldsHeader) as FactorChallenge
    assertEquals(jsonObject.getString(SID_KEY), challenge.sid)
    assertEquals(jsonObject.getString(FACTOR_SID_KEY), challenge.factorSid)
    assertEquals(
      expectedSignatureFieldsHeader,
      challenge.signatureFields?.joinToString(SIGNATURE_FIELDS_HEADER_SEPARATOR),
    )
    assertEquals(fromRFC3339Date(jsonObject.getString(CREATED_DATE_KEY)), challenge.createdAt)
    assertEquals(fromRFC3339Date(jsonObject.getString(UPDATED_DATE_KEY)), challenge.updatedAt)
    assertEquals(jsonObject.getString(STATUS_KEY), challenge.status.value)
    assertEquals(jsonObject, challenge.response)
    val details = JSONObject(jsonObject.getString(DETAILS_KEY))
    assertEquals(details.getString(MESSAGE_KEY), challenge.challengeDetails.message)
    assertEquals(
      details
        .getJSONArray(FIELDS_KEY)
        .length(),
      challenge.challengeDetails.fields.size,
    )
    assertEquals(
      details
        .getJSONArray(FIELDS_KEY)
        .getJSONObject(0)
        .getString(LABEL_KEY),
      challenge.challengeDetails.fields[0].label,
    )
    assertEquals(
      details
        .getJSONArray(FIELDS_KEY)
        .getJSONObject(0)
        .getString(VALUE_KEY),
      challenge.challengeDetails.fields[0].value,
    )
    assertNull(challenge.challengeDetails.date)
    assertEquals(hiddenDetails, challenge.hiddenDetails)
    assertEquals(fromRFC3339Date(jsonObject.getString(EXPIRATION_DATE_KEY)), challenge.expirationDate)
  }

  @Test
  fun `Map a valid response from API with approved status should return a challenge`() {
    val hiddenDetails = mapOf("key1" to "value1")
    val jsonObject =
      JSONObject().apply {
        put(SID_KEY, "sid123")
        put(FACTOR_SID_KEY, "factorSid123")
        put(CREATED_DATE_KEY, "2020-02-19T16:39:57-08:00")
        put(UPDATED_DATE_KEY, "2020-02-21T18:39:57-08:00")
        put(STATUS_KEY, ChallengeStatus.Approved.value)
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
          JSONObject(hiddenDetails),
        )
        put(EXPIRATION_DATE_KEY, "2020-02-27T08:50:57-08:00")
      }
    val expectedSignatureFieldsHeader =
      jsonObject
        .keys()
        .asSequence()
        .toList()
        .joinToString(
          SIGNATURE_FIELDS_HEADER_SEPARATOR,
        )
    val challenge =
      challengeMapper.fromApi(jsonObject, expectedSignatureFieldsHeader) as FactorChallenge
    assertEquals(jsonObject.getString(SID_KEY), challenge.sid)
    assertEquals(jsonObject.getString(FACTOR_SID_KEY), challenge.factorSid)
    assertNull(challenge.signatureFields)
    assertEquals(fromRFC3339Date(jsonObject.getString(CREATED_DATE_KEY)), challenge.createdAt)
    assertEquals(fromRFC3339Date(jsonObject.getString(UPDATED_DATE_KEY)), challenge.updatedAt)
    assertEquals(jsonObject.getString(STATUS_KEY), challenge.status.value)
    assertNull(challenge.response)
    val details = JSONObject(jsonObject.getString(DETAILS_KEY))
    assertEquals(details.getString(MESSAGE_KEY), challenge.challengeDetails.message)
    assertEquals(
      details
        .getJSONArray(FIELDS_KEY)
        .length(),
      challenge.challengeDetails.fields.size,
    )
    assertEquals(
      details
        .getJSONArray(FIELDS_KEY)
        .getJSONObject(0)
        .getString(LABEL_KEY),
      challenge.challengeDetails.fields[0].label,
    )
    assertEquals(
      details
        .getJSONArray(FIELDS_KEY)
        .getJSONObject(0)
        .getString(VALUE_KEY),
      challenge.challengeDetails.fields[0].value,
    )
    assertEquals(fromRFC3339Date(details.getString(DATE_KEY)), challenge.challengeDetails.date)
    assertEquals(hiddenDetails, challenge.hiddenDetails)
    assertEquals(fromRFC3339Date(jsonObject.getString(EXPIRATION_DATE_KEY)), challenge.expirationDate)
  }

  @Test
  fun `Map a valid response from API with pending status and no signature fields should return a challenge`() {
    val hiddenDetails = mapOf("key1" to "value1")
    val jsonObject =
      JSONObject().apply {
        put(SID_KEY, "sid123")
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
          JSONObject(hiddenDetails),
        )
        put(EXPIRATION_DATE_KEY, "2020-02-27T08:50:57-08:00")
      }
    val challenge =
      challengeMapper.fromApi(jsonObject) as FactorChallenge
    assertEquals(jsonObject.getString(SID_KEY), challenge.sid)
    assertEquals(jsonObject.getString(FACTOR_SID_KEY), challenge.factorSid)
    assertNull(challenge.signatureFields?.joinToString(SIGNATURE_FIELDS_HEADER_SEPARATOR))
    assertEquals(fromRFC3339Date(jsonObject.getString(CREATED_DATE_KEY)), challenge.createdAt)
    assertEquals(fromRFC3339Date(jsonObject.getString(UPDATED_DATE_KEY)), challenge.updatedAt)
    assertEquals(jsonObject.getString(STATUS_KEY), challenge.status.value)
    assertNull(challenge.response)
    val details = JSONObject(jsonObject.getString(DETAILS_KEY))
    assertEquals(details.getString(MESSAGE_KEY), challenge.challengeDetails.message)
    assertEquals(
      details
        .getJSONArray(FIELDS_KEY)
        .length(),
      challenge.challengeDetails.fields.size,
    )
    assertEquals(
      details
        .getJSONArray(FIELDS_KEY)
        .getJSONObject(0)
        .getString(LABEL_KEY),
      challenge.challengeDetails.fields[0].label,
    )
    assertEquals(
      details
        .getJSONArray(FIELDS_KEY)
        .getJSONObject(0)
        .getString(VALUE_KEY),
      challenge.challengeDetails.fields[0].value,
    )
    assertEquals(fromRFC3339Date(details.getString(DATE_KEY)), challenge.challengeDetails.date)
    assertEquals(hiddenDetails, challenge.hiddenDetails)
    assertEquals(fromRFC3339Date(jsonObject.getString(EXPIRATION_DATE_KEY)), challenge.expirationDate)
  }

  @Test
  fun `Map a response from API without sid should throw an error`() {
    val jsonObject =
      JSONObject().apply {
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
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf<Throwable>(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeMapper.fromApi(jsonObject)
  }

  @Test
  fun `Map a response from API without details should throw an error`() {
    val jsonObject =
      JSONObject().apply {
        put(SID_KEY, "sid123")
        put(FACTOR_SID_KEY, "factorSid123")
        put(CREATED_DATE_KEY, "2020-02-19T16:39:57-08:00")
        put(UPDATED_DATE_KEY, "2020-02-21T18:39:57-08:00")
        put(STATUS_KEY, ChallengeStatus.Pending.value)
        put(
          HIDDEN_DETAILS_KEY,
          JSONObject().apply {
            put("key1", "value1")
          },
        )
        put(EXPIRATION_DATE_KEY, "2020-02-27T08:50:57-08:00")
      }
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf<Throwable>(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeMapper.fromApi(jsonObject)
  }

  @Test
  fun `Map a response from API without message should throw an error`() {
    val jsonObject =
      JSONObject().apply {
        put(SID_KEY, "sid123")
        put(FACTOR_SID_KEY, "factorSid123")
        put(CREATED_DATE_KEY, "2020-02-19T16:39:57-08:00")
        put(UPDATED_DATE_KEY, "2020-02-21T18:39:57-08:00")
        put(STATUS_KEY, ChallengeStatus.Pending.value)
        put(
          DETAILS_KEY,
          JSONObject().apply {
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
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf<Throwable>(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeMapper.fromApi(jsonObject)
  }

  @Test
  fun `Map a response from API with invalid created date should throw an error`() {
    val jsonObject =
      JSONObject().apply {
        put(SID_KEY, "sid123")
        put(FACTOR_SID_KEY, "factorSid123")
        put(CREATED_DATE_KEY, "19-02-2020")
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
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf<Throwable>(ParseException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeMapper.fromApi(jsonObject)
  }

  @Test
  fun `Map a response from API with invalid details date should throw an error`() {
    val jsonObject =
      JSONObject().apply {
        put(SID_KEY, "sid123")
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
            put(DATE_KEY, "2020-02-19")
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
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf<Throwable>(ParseException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeMapper.fromApi(jsonObject)
  }

  @Test
  fun `Map a valid response from API with details date should return a challenge`() {
    val jsonObject =
      JSONObject().apply {
        put(SID_KEY, "sid123")
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
            put(DATE_KEY, "2020-03-24T20:37:26Z")
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
    val expectedSignatureFieldsHeader =
      jsonObject
        .keys()
        .asSequence()
        .toList()
        .joinToString(
          SIGNATURE_FIELDS_HEADER_SEPARATOR,
        )
    val challenge =
      challengeMapper.fromApi(jsonObject, expectedSignatureFieldsHeader) as FactorChallenge
    assertEquals(1585082246000, challenge.challengeDetails.date?.time)
  }
}
