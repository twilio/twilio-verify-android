/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import com.twilio.verify.ErrorCodeMatcher
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.data.fromRFC3339Date
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.models.ChallengeStatus
import java.text.ParseException
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

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ChallengeMapperTest {

  private val challengeMapper = ChallengeMapper()

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  @Test
  fun `Map a valid response from API should return a challenge`() {
    val jsonObject = JSONObject().apply {
      put(sidKey, "sid123")
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.value)
      put(
        detailsKey,
        JSONObject().apply {
          put(messageKey, "message123")
          put(
            fieldsKey,
            JSONArray().apply {
              put(
                0,
                JSONObject().apply {
                  put(labelKey, "label123")
                  put(valueKey, "value123")
                }
              )
            }
          )
          put(dateKey, "2020-02-19T16:39:57-08:00")
        }
          .toString()
      )
      put(
        hiddenDetailsKey,
        JSONObject().apply {
          put("key1", "value1")
        }
          .toString()
      )
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
    val expectedSignatureFieldsHeader = jsonObject.keys()
      .asSequence()
      .toList()
      .joinToString(
        signatureFieldsHeaderSeparator
      )
    val challenge =
      challengeMapper.fromApi(jsonObject, expectedSignatureFieldsHeader) as FactorChallenge
    assertEquals(jsonObject.getString(sidKey), challenge.sid)
    assertEquals(jsonObject.getString(factorSidKey), challenge.factorSid)
    assertEquals(
      expectedSignatureFieldsHeader,
      challenge.signatureFields?.joinToString(signatureFieldsHeaderSeparator)
    )
    assertEquals(fromRFC3339Date(jsonObject.getString(createdDateKey)), challenge.createdAt)
    assertEquals(fromRFC3339Date(jsonObject.getString(updatedDateKey)), challenge.updatedAt)
    assertEquals(jsonObject.getString(statusKey), challenge.status.value)
    assertEquals(jsonObject, challenge.response)
    val details = JSONObject(jsonObject.getString(detailsKey))
    assertEquals(details.getString(messageKey), challenge.challengeDetails.message)
    assertEquals(
      details.getJSONArray(fieldsKey)
        .length(),
      challenge.challengeDetails.fields.size
    )
    assertEquals(
      details.getJSONArray(fieldsKey)
        .getJSONObject(0)
        .getString(labelKey),
      challenge.challengeDetails.fields[0].label
    )
    assertEquals(
      details.getJSONArray(fieldsKey)
        .getJSONObject(0)
        .getString(valueKey),
      challenge.challengeDetails.fields[0].value
    )
    assertEquals(fromRFC3339Date(details.getString(dateKey)), challenge.challengeDetails.date)
    assertEquals(jsonObject.getString(hiddenDetailsKey), challenge.hiddenDetails)
    assertEquals(fromRFC3339Date(jsonObject.getString(expirationDateKey)), challenge.expirationDate)
  }

  @Test
  fun `Map a valid response from API and no fields should return a challenge`() {
    val jsonObject = JSONObject().apply {
      put(sidKey, "sid123")
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.value)
      put(
        detailsKey,
        JSONObject().apply {
          put(messageKey, "message123")
          put(dateKey, "2020-02-19T16:39:57-08:00")
        }
          .toString()
      )
      put(
        hiddenDetailsKey,
        JSONObject().apply {
          put("key1", "value1")
        }
          .toString()
      )
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
    val expectedSignatureFieldsHeader = jsonObject.keys()
      .asSequence()
      .toList()
      .joinToString(
        signatureFieldsHeaderSeparator
      )
    val challenge =
      challengeMapper.fromApi(jsonObject, expectedSignatureFieldsHeader) as FactorChallenge
    assertEquals(jsonObject.getString(sidKey), challenge.sid)
    assertEquals(jsonObject.getString(factorSidKey), challenge.factorSid)
    assertEquals(
      expectedSignatureFieldsHeader,
      challenge.signatureFields?.joinToString(signatureFieldsHeaderSeparator)
    )
    assertEquals(fromRFC3339Date(jsonObject.getString(createdDateKey)), challenge.createdAt)
    assertEquals(fromRFC3339Date(jsonObject.getString(updatedDateKey)), challenge.updatedAt)
    assertEquals(jsonObject.getString(statusKey), challenge.status.value)
    assertEquals(jsonObject, challenge.response)
    val details = JSONObject(jsonObject.getString(detailsKey))
    assertEquals(details.getString(messageKey), challenge.challengeDetails.message)
    assertTrue(challenge.challengeDetails.fields.isEmpty())
    assertEquals(fromRFC3339Date(details.getString(dateKey)), challenge.challengeDetails.date)
    assertEquals(jsonObject.getString(hiddenDetailsKey), challenge.hiddenDetails)
    assertEquals(fromRFC3339Date(jsonObject.getString(expirationDateKey)), challenge.expirationDate)
  }

  @Test
  fun `Map a valid response from API and no details date should return a challenge`() {
    val jsonObject = JSONObject().apply {
      put(sidKey, "sid123")
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.value)
      put(
        detailsKey,
        JSONObject().apply {
          put(messageKey, "message123")
          put(
            fieldsKey,
            JSONArray().apply {
              put(
                0,
                JSONObject().apply {
                  put(labelKey, "label123")
                  put(valueKey, "value123")
                }
              )
            }
          )
        }
          .toString()
      )
      put(
        hiddenDetailsKey,
        JSONObject().apply {
          put("key1", "value1")
        }
          .toString()
      )
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
    val expectedSignatureFieldsHeader = jsonObject.keys()
      .asSequence()
      .toList()
      .joinToString(
        signatureFieldsHeaderSeparator
      )
    val challenge =
      challengeMapper.fromApi(jsonObject, expectedSignatureFieldsHeader) as FactorChallenge
    assertEquals(jsonObject.getString(sidKey), challenge.sid)
    assertEquals(jsonObject.getString(factorSidKey), challenge.factorSid)
    assertEquals(
      expectedSignatureFieldsHeader,
      challenge.signatureFields?.joinToString(signatureFieldsHeaderSeparator)
    )
    assertEquals(fromRFC3339Date(jsonObject.getString(createdDateKey)), challenge.createdAt)
    assertEquals(fromRFC3339Date(jsonObject.getString(updatedDateKey)), challenge.updatedAt)
    assertEquals(jsonObject.getString(statusKey), challenge.status.value)
    assertEquals(jsonObject, challenge.response)
    val details = JSONObject(jsonObject.getString(detailsKey))
    assertEquals(details.getString(messageKey), challenge.challengeDetails.message)
    assertEquals(
      details.getJSONArray(fieldsKey)
        .length(),
      challenge.challengeDetails.fields.size
    )
    assertEquals(
      details.getJSONArray(fieldsKey)
        .getJSONObject(0)
        .getString(labelKey),
      challenge.challengeDetails.fields[0].label
    )
    assertEquals(
      details.getJSONArray(fieldsKey)
        .getJSONObject(0)
        .getString(valueKey),
      challenge.challengeDetails.fields[0].value
    )
    assertNull(challenge.challengeDetails.date)
    assertEquals(jsonObject.getString(hiddenDetailsKey), challenge.hiddenDetails)
    assertEquals(fromRFC3339Date(jsonObject.getString(expirationDateKey)), challenge.expirationDate)
  }

  @Test
  fun `Map a valid response from API with approved status should return a challenge`() {
    val jsonObject = JSONObject().apply {
      put(sidKey, "sid123")
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Approved.value)
      put(
        detailsKey,
        JSONObject().apply {
          put(messageKey, "message123")
          put(
            fieldsKey,
            JSONArray().apply {
              put(
                0,
                JSONObject().apply {
                  put(labelKey, "label123")
                  put(valueKey, "value123")
                }
              )
            }
          )
          put(dateKey, "2020-02-19T16:39:57-08:00")
        }
          .toString()
      )
      put(
        hiddenDetailsKey,
        JSONObject().apply {
          put("key1", "value1")
        }
          .toString()
      )
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
    val expectedSignatureFieldsHeader = jsonObject.keys()
      .asSequence()
      .toList()
      .joinToString(
        signatureFieldsHeaderSeparator
      )
    val challenge =
      challengeMapper.fromApi(jsonObject, expectedSignatureFieldsHeader) as FactorChallenge
    assertEquals(jsonObject.getString(sidKey), challenge.sid)
    assertEquals(jsonObject.getString(factorSidKey), challenge.factorSid)
    assertNull(challenge.signatureFields)
    assertEquals(fromRFC3339Date(jsonObject.getString(createdDateKey)), challenge.createdAt)
    assertEquals(fromRFC3339Date(jsonObject.getString(updatedDateKey)), challenge.updatedAt)
    assertEquals(jsonObject.getString(statusKey), challenge.status.value)
    assertNull(challenge.response)
    val details = JSONObject(jsonObject.getString(detailsKey))
    assertEquals(details.getString(messageKey), challenge.challengeDetails.message)
    assertEquals(
      details.getJSONArray(fieldsKey)
        .length(),
      challenge.challengeDetails.fields.size
    )
    assertEquals(
      details.getJSONArray(fieldsKey)
        .getJSONObject(0)
        .getString(labelKey),
      challenge.challengeDetails.fields[0].label
    )
    assertEquals(
      details.getJSONArray(fieldsKey)
        .getJSONObject(0)
        .getString(valueKey),
      challenge.challengeDetails.fields[0].value
    )
    assertEquals(fromRFC3339Date(details.getString(dateKey)), challenge.challengeDetails.date)
    assertEquals(jsonObject.getString(hiddenDetailsKey), challenge.hiddenDetails)
    assertEquals(fromRFC3339Date(jsonObject.getString(expirationDateKey)), challenge.expirationDate)
  }

  @Test
  fun `Map a valid response from API with pending status and no signature fields should return a challenge`() {
    val jsonObject = JSONObject().apply {
      put(sidKey, "sid123")
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.value)
      put(
        detailsKey,
        JSONObject().apply {
          put(messageKey, "message123")
          put(
            fieldsKey,
            JSONArray().apply {
              put(
                0,
                JSONObject().apply {
                  put(labelKey, "label123")
                  put(valueKey, "value123")
                }
              )
            }
          )
          put(dateKey, "2020-02-19T16:39:57-08:00")
        }
          .toString()
      )
      put(
        hiddenDetailsKey,
        JSONObject().apply {
          put("key1", "value1")
        }
          .toString()
      )
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
    val challenge =
      challengeMapper.fromApi(jsonObject) as FactorChallenge
    assertEquals(jsonObject.getString(sidKey), challenge.sid)
    assertEquals(jsonObject.getString(factorSidKey), challenge.factorSid)
    assertNull(challenge.signatureFields?.joinToString(signatureFieldsHeaderSeparator))
    assertEquals(fromRFC3339Date(jsonObject.getString(createdDateKey)), challenge.createdAt)
    assertEquals(fromRFC3339Date(jsonObject.getString(updatedDateKey)), challenge.updatedAt)
    assertEquals(jsonObject.getString(statusKey), challenge.status.value)
    assertNull(challenge.response)
    val details = JSONObject(jsonObject.getString(detailsKey))
    assertEquals(details.getString(messageKey), challenge.challengeDetails.message)
    assertEquals(
      details.getJSONArray(fieldsKey)
        .length(),
      challenge.challengeDetails.fields.size
    )
    assertEquals(
      details.getJSONArray(fieldsKey)
        .getJSONObject(0)
        .getString(labelKey),
      challenge.challengeDetails.fields[0].label
    )
    assertEquals(
      details.getJSONArray(fieldsKey)
        .getJSONObject(0)
        .getString(valueKey),
      challenge.challengeDetails.fields[0].value
    )
    assertEquals(fromRFC3339Date(details.getString(dateKey)), challenge.challengeDetails.date)
    assertEquals(jsonObject.getString(hiddenDetailsKey), challenge.hiddenDetails)
    assertEquals(fromRFC3339Date(jsonObject.getString(expirationDateKey)), challenge.expirationDate)
  }

  @Test
  fun `Map a response from API without sid should throw an error`() {
    val jsonObject = JSONObject().apply {
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.value)
      put(
        detailsKey,
        JSONObject().apply {
          put(messageKey, "message123")
          put(
            fieldsKey,
            JSONArray().apply {
              put(
                0,
                JSONObject().apply {
                  put(labelKey, "label123")
                  put(valueKey, "value123")
                }
              )
            }
          )
          put(dateKey, "2020-02-19T16:39:57-08:00")
        }
          .toString()
      )
      put(
        hiddenDetailsKey,
        JSONObject().apply {
          put("key1", "value1")
        }
          .toString()
      )
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeMapper.fromApi(jsonObject)
  }

  @Test
  fun `Map a response from API without details should throw an error`() {
    val jsonObject = JSONObject().apply {
      put(sidKey, "sid123")
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.value)
      put(
        hiddenDetailsKey,
        JSONObject().apply {
          put("key1", "value1")
        }
          .toString()
      )
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeMapper.fromApi(jsonObject)
  }

  @Test
  fun `Map a response from API without message should throw an error`() {
    val jsonObject = JSONObject().apply {
      put(sidKey, "sid123")
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.value)
      put(
        detailsKey,
        JSONObject().apply {
          put(
            fieldsKey,
            JSONArray().apply {
              put(
                0,
                JSONObject().apply {
                  put(labelKey, "label123")
                  put(valueKey, "value123")
                }
              )
            }
          )
          put(dateKey, "2020-02-19T16:39:57-08:00")
        }
          .toString()
      )
      put(
        hiddenDetailsKey,
        JSONObject().apply {
          put("key1", "value1")
        }
          .toString()
      )
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeMapper.fromApi(jsonObject)
  }

  @Test
  fun `Map a response from API with invalid created date should throw an error`() {
    val jsonObject = JSONObject().apply {
      put(sidKey, "sid123")
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "19-02-2020")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.value)
      put(
        detailsKey,
        JSONObject().apply {
          put(messageKey, "message123")
          put(
            fieldsKey,
            JSONArray().apply {
              put(
                0,
                JSONObject().apply {
                  put(labelKey, "label123")
                  put(valueKey, "value123")
                }
              )
            }
          )
          put(dateKey, "2020-02-19T16:39:57-08:00")
        }
          .toString()
      )
      put(
        hiddenDetailsKey,
        JSONObject().apply {
          put("key1", "value1")
        }
          .toString()
      )
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf(ParseException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeMapper.fromApi(jsonObject)
  }

  @Test
  fun `Map a response from API with invalid details date should throw an error`() {
    val jsonObject = JSONObject().apply {
      put(sidKey, "sid123")
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.value)
      put(
        detailsKey,
        JSONObject().apply {
          put(messageKey, "message123")
          put(
            fieldsKey,
            JSONArray().apply {
              put(
                0,
                JSONObject().apply {
                  put(labelKey, "label123")
                  put(valueKey, "value123")
                }
              )
            }
          )
          put(dateKey, "2020-02-19")
        }
          .toString()
      )
      put(
        hiddenDetailsKey,
        JSONObject().apply {
          put("key1", "value1")
        }
          .toString()
      )
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf(ParseException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(TwilioVerifyException.ErrorCode.MapperError))
    challengeMapper.fromApi(jsonObject)
  }

  @Test
  fun `Map a valid response from API with details date should return a challenge`() {
    val jsonObject = JSONObject().apply {
      put(sidKey, "sid123")
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.value)
      put(
        detailsKey,
        JSONObject().apply {
          put(messageKey, "message123")
          put(
            fieldsKey,
            JSONArray().apply {
              put(
                0,
                JSONObject().apply {
                  put(labelKey, "label123")
                  put(valueKey, "value123")
                }
              )
            }
          )
          put(dateKey, "2020-03-24T20:37:26Z")
        }
          .toString()
      )
      put(
        hiddenDetailsKey,
        JSONObject().apply {
          put("key1", "value1")
        }
          .toString()
      )
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
    val expectedSignatureFieldsHeader = jsonObject.keys()
      .asSequence()
      .toList()
      .joinToString(
        signatureFieldsHeaderSeparator
      )
    val challenge =
      challengeMapper.fromApi(jsonObject, expectedSignatureFieldsHeader) as FactorChallenge
    assertEquals(1585082246000, challenge.challengeDetails.date?.time)
  }
}
