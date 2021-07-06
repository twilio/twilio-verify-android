/*
 * Copyright (c) 2021 Twilio Inc.
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

package com.twilio.security.crypto.key.authentication

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.Signature
import javax.crypto.Cipher

interface BiometricAuthenticator : Authenticator {

  @Throws(BiometricException::class)
  fun checkAvailability()
}

class BiometricException(message: String) : Exception(message) {
  constructor(biometricError: BiometricError) : this(biometricError.message)
}

enum class BiometricError(val message: String) {
  AuthenticationFailed("Authentication failed"),
  InvalidResult("Invalid result"),
  HardwareUnavailable("The hardware is unavailable. Try again later.")
}

class BiometricAuthenticatorContext(
  private val title: String,
  private val subtitle: String,
  private val fragmentActivity: FragmentActivity,
  private val negativeOption: String
) : BiometricAuthenticator {

  @Throws(BiometricException::class)
  override fun checkAvailability() {
    when (BiometricManager.from(fragmentActivity.applicationContext).canAuthenticate(Authenticators.BIOMETRIC_STRONG)) {
      BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> throw BiometricException(BiometricError.HardwareUnavailable)
      else -> return
    }
  }

  override fun startAuthentication(signatureObject: Signature, success: (Signature) -> Unit, error: (Exception) -> Unit) {
    try {
      checkAvailability()
      val biometricPrompt = createBiometricPrompt(callBackForSignature(success, error))
      biometricPrompt.authenticate(createPromptInfo(), BiometricPrompt.CryptoObject(signatureObject))
    } catch (e: Exception) {
      error(e)
    }
  }

  override fun startAuthentication(cipherObject: Cipher, success: (Cipher) -> Unit, error: (Exception) -> Unit) {
    try {
      checkAvailability()
      val biometricPrompt = createBiometricPrompt(callBackForCipher(success, error))
      biometricPrompt.authenticate(createPromptInfo(), BiometricPrompt.CryptoObject(cipherObject))
    } catch (e: Exception) {
      error(e)
    }
  }

  private fun createPromptInfo(): BiometricPrompt.PromptInfo {
    return BiometricPrompt.PromptInfo.Builder()
      .setTitle(title)
      .setSubtitle(subtitle)
      .setConfirmationRequired(true)
      .setNegativeButtonText(negativeOption)
      .build()
  }

  private fun createBiometricPrompt(callback: BiometricPrompt.AuthenticationCallback): BiometricPrompt {
    val executor = ContextCompat.getMainExecutor(fragmentActivity)
    return BiometricPrompt(fragmentActivity, executor, callback)
  }

  private fun callBackForSignature(success: (Signature) -> Unit, error: (Exception) -> Unit): BiometricPrompt.AuthenticationCallback {
    return object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        error(BiometricException(errString.toString()))
      }

      override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        error(BiometricException(BiometricError.AuthenticationFailed))
      }

      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        result.cryptoObject?.signature?.let {
          success(it)
        } ?: error(BiometricException(BiometricError.InvalidResult))
      }
    }
  }

  private fun callBackForCipher(success: (Cipher) -> Unit, error: (Exception) -> Unit): BiometricPrompt.AuthenticationCallback {
    return object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        error(BiometricException(errString.toString()))
      }

      override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        error(BiometricException(BiometricError.AuthenticationFailed))
      }

      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        result.cryptoObject?.cipher?.let {
          success(it)
        } ?: error(BiometricException(BiometricError.InvalidResult))
      }
    }
  }
}
