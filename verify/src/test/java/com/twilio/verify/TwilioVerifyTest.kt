/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.key.signer.Signer
import com.twilio.security.crypto.key.template.SignerTemplate
import com.twilio.verify.domain.factor.accountSidKey
import com.twilio.verify.domain.factor.entitySidKey
import com.twilio.verify.domain.factor.friendlyNameKey
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.domain.factor.serviceSidKey
import com.twilio.verify.domain.factor.sidKey
import com.twilio.verify.domain.factor.statusKey
import com.twilio.verify.domain.factor.waitForEmpty
import com.twilio.verify.models.FactorStatus
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.models.VerifyPushFactorInput
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.NetworkProvider
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.io.InputStream
import java.io.OutputStream
import java.security.Key
import java.security.KeyStore
import java.security.KeyStoreSpi
import java.security.Provider
import java.security.Security
import java.security.cert.Certificate
import java.util.Date
import java.util.Enumeration
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [TestKeystore::class])
class TwilioVerifyTest {

  private val networkProvider: NetworkProvider = mock()
  private val authorization = Authorization("accountSid", "authToken")
  private lateinit var twilioVerify: TwilioVerify
  private lateinit var provider: Provider
  private val providerName = "AndroidKeyStore"
  private val counter = AtomicInteger(0)

  @Before
  fun setup() {
    provider = object : Provider(
        providerName, 1.0, "Fake KeyStore which is used for Robolectric tests"
    ) {
      init {
        put(
            "KeyStore.$providerName",
            "com.twilio.verify.KeyStoreMock"
        )
      }
    }
    Security.insertProviderAt(provider, 0)
    twilioVerify =
      TwilioVerify.Builder(ApplicationProvider.getApplicationContext(), authorization)
          .networkProvider(networkProvider)
          .build()
  }

  @Test
  fun `Create a factor should call success`() {
    val jsonObject = JSONObject()
        .put(sidKey, "sid123")
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
        .put(statusKey, FactorStatus.Unverified.value)
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(jsonObject.toString())
      }
    }
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYiIsIm5iZiI6MTU3NT" +
        "YwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OTI2NmU3NzZhZjAwMDAwIn" +
        "0.QWrQhpdrJTtXXFwDX9LL4wCy43SWhjS-w5p9C6bcsTk"
    val factorInput = PushFactorInput("friendly name", "pushToken", jwt)
    counter.incrementAndGet()
    twilioVerify.createFactor(factorInput, { factor ->
      assertEquals(jsonObject.getString(sidKey), factor.sid)
      assertTrue(keys.containsKey((factor as? PushFactor)?.keyPairAlias))
      counter.decrementAndGet()
    }, { exception ->
      fail(exception.message)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Verify a factor should call success`() {
    val sid = "sid"
    val jsonObject = JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
        .put(serviceSidKey, "serviceSid")
        .put(statusKey, FactorStatus.Verified.value)
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYiIsIm5iZiI6MTU3NT" +
        "YwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OTI2NmU3NzZhZjAwMDAwIn" +
        "0.QWrQhpdrJTtXXFwDX9LL4wCy43SWhjS-w5p9C6bcsTk"
    val factorInput = PushFactorInput("friendly name", "pushToken", jwt)
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(jsonObject.toString())
      }
    }
    counter.incrementAndGet()
    twilioVerify.createFactor(factorInput, { createdFactor ->
      val verifyFactorInput = VerifyPushFactorInput(sid)
      argumentCaptor<(String) -> Unit>().apply {
        whenever(networkProvider.execute(any(), capture(), any())).then {
          firstValue.invoke(jsonObject.toString())
        }
      }
      counter.incrementAndGet()
      twilioVerify.verifyFactor(verifyFactorInput, { factor ->
        assertEquals(jsonObject.getString(sidKey), factor.sid)
        counter.decrementAndGet()
      }, { exception ->
        fail(exception.message)
        counter.decrementAndGet()
      })
      counter.decrementAndGet()
    }, { exception ->
      fail(exception.message)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }
}

private val keys = mutableMapOf<String, String>()

@Implements(com.twilio.security.crypto.AndroidKeyManager::class)
class TestKeystore {
  @Implementation
  fun signer(template: SignerTemplate): Signer {
    keys[template.alias] = template.alias.hashCode()
        .toString()
    val mock: Signer = mock()
    whenever(mock.getPublic()).thenReturn(keys[template.alias]?.toByteArray())
    whenever(mock.sign(any())).thenReturn(keys[template.alias]?.toByteArray())
    return mock
  }

  @Implementation
  fun delete(alias: String) {
    keys.remove(alias)
  }
}

class KeyStoreMock : KeyStoreSpi() {
  override fun engineIsKeyEntry(alias: String?): Boolean {
    throw NotImplementedError()
  }

  override fun engineIsCertificateEntry(alias: String?): Boolean {
    throw NotImplementedError()
  }

  override fun engineGetCertificate(alias: String?): Certificate {
    throw NotImplementedError()
  }

  override fun engineGetCreationDate(alias: String?): Date {
    throw NotImplementedError()
  }

  override fun engineDeleteEntry(alias: String?) {
    throw NotImplementedError()
  }

  override fun engineSetKeyEntry(
    alias: String?,
    key: Key?,
    password: CharArray?,
    chain: Array<out Certificate>?
  ) {
    throw NotImplementedError()
  }

  override fun engineSetKeyEntry(
    alias: String?,
    key: ByteArray?,
    chain: Array<out Certificate>?
  ) {
    throw NotImplementedError()
  }

  override fun engineStore(
    stream: OutputStream?,
    password: CharArray?
  ) {
    throw NotImplementedError()
  }

  override fun engineSize(): Int {
    throw NotImplementedError()
  }

  override fun engineAliases(): Enumeration<String> {
    throw NotImplementedError()
  }

  override fun engineContainsAlias(alias: String?): Boolean {
    throw NotImplementedError()
  }

  override fun engineLoad(
    stream: InputStream?,
    password: CharArray?
  ) {

  }

  override fun engineGetCertificateChain(alias: String?): Array<Certificate> {
    throw NotImplementedError()
  }

  override fun engineSetCertificateEntry(
    alias: String?,
    cert: Certificate?
  ) {
    throw NotImplementedError()
  }

  override fun engineGetCertificateAlias(cert: Certificate?): String {
    throw NotImplementedError()
  }

  override fun engineGetKey(
    alias: String?,
    password: CharArray?
  ): Key {
    throw NotImplementedError()
  }

  override fun engineGetEntry(
    alias: String?,
    protParam: KeyStore.ProtectionParameter?
  ): KeyStore.Entry? {
    throw NotImplementedError()
  }
}