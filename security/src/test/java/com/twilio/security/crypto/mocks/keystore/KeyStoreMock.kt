package com.twilio.security.crypto.mocks.keystore

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.security.Key
import java.security.KeyPair
import java.security.KeyStore
import java.security.KeyStoreSpi
import java.security.Provider
import java.security.Security
import java.security.cert.Certificate
import java.util.Date
import java.util.Enumeration
import java.util.IdentityHashMap
import javax.crypto.SecretKey

lateinit var keyStoreMockInput: KeyStoreMockInput
lateinit var keyStoreMockOutput: KeyStoreMockOutput

internal const val keyStoreMockName = "com.twilio.security.crypto.mocks.keystore.KeyStoreMock"

class KeyStoreMock : KeyStoreSpi() {
  override fun engineIsKeyEntry(alias: String?): Boolean {
    throw NotImplementedError()
  }

  override fun engineIsCertificateEntry(alias: String?): Boolean {
    throw NotImplementedError()
  }

  override fun engineGetCertificate(alias: String?): Certificate? {
    if (keyStoreMockInput.error != null) {
      throw keyStoreMockInput.error!!
    }
    if (keyStoreMockInput.key is KeyPair) {
      val certificate: Certificate = mock()
      val publicKey = (keyStoreMockInput.key as? KeyPair)?.public
      whenever(certificate.publicKey).thenReturn(publicKey)
      return certificate
    }
    return null
  }

  override fun engineGetCreationDate(alias: String?): Date {
    throw NotImplementedError()
  }

  override fun engineDeleteEntry(alias: String?) {
    if (keyStoreMockInput.error != null) {
      throw keyStoreMockInput.error!!
    }
    keyStoreMockOutput.deletedAlias = alias
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
    return keyStoreMockInput.containsAlias
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
  ): Key? {
    if (keyStoreMockInput.error != null) {
      throw keyStoreMockInput.error!!
    }
    return when (keyStoreMockInput.key) {
      is SecretKey -> keyStoreMockInput.key as? SecretKey
      is KeyPair -> (keyStoreMockInput.key as? KeyPair)?.private
      else -> null
    }
  }

  override fun engineGetEntry(
    alias: String?,
    protParam: KeyStore.ProtectionParameter?
  ): KeyStore.Entry? {
    throw NotImplementedError()
  }
}

fun addProvider(provider: Provider) {
  Security.insertProviderAt(provider, 0)
}

fun setProviderAsVerified(provider: Provider) {
  val jceSecurityClass = Class.forName("javax.crypto.JceSecurity")
  val verifiedProviders = IdentityHashMap<Provider, Any>().apply {
    put(provider, java.lang.Boolean.TRUE as Any)
  }
  setFinalStatic(
      jceSecurityClass.getDeclaredField("verificationResults"), verifiedProviders
  )
}

@Throws(Exception::class) fun setFinalStatic(
  field: Field,
  newValue: Any?
) {
  field.isAccessible = true
  val modifiersField: Field = Field::class.java.getDeclaredField("modifiers")
  modifiersField.isAccessible = true
  modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
  field.set(null, newValue)
}