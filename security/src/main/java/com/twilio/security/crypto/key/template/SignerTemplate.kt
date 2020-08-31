/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.template

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyGenParameterSpec.Builder
import android.security.keystore.KeyProperties
import java.security.spec.ECGenParameterSpec

sealed class SignerTemplate : Template {
  internal abstract val keyGenParameterSpec: KeyGenParameterSpec
  internal abstract val signatureAlgorithm: String
}

data class ECP256SignerTemplate(
  override val alias: String,
  override val shouldExist: Boolean = false
) : SignerTemplate() {
  override val algorithm = KeyProperties.KEY_ALGORITHM_EC
  override val keyGenParameterSpec: KeyGenParameterSpec =
    Builder(alias, KeyProperties.PURPOSE_SIGN).setAlgorithmParameterSpec(
      ECGenParameterSpec("secp256r1")
    )
      .setDigests(KeyProperties.DIGEST_SHA256)
      .build()
  override val signatureAlgorithm = "SHA256withECDSA"
}
