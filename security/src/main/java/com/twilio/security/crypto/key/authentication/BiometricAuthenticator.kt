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
import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
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
  DependencyVersionMissMatch("Biometric version is not supported."),
  Unsupported("The specified options are incompatible with the current Android version."),
  HardwareUnavailable("The hardware is unavailable. Try again later."),
  NoBiometricEnrolled("No biometric or device credential is enrolled."),
  NoHardware("There is no suitable hardware (e.g. no biometric sensor or no keyguard)"),
  SecureUpdateRequired(
    "A security vulnerability has been discovered with one or more hardware sensors. " +
      "The affected sensor(s) are unavailable until a security update has addressed the issue."
  ),
  UnableToProcess("The sensor was unable to process the current image."),
  Timeout("The current operation has been running too long and has timed out."),
  DeviceStorage("The operation can't be completed because there is not enough device storage remaining."),
  OperationCanceled(
    "The operation was canceled because the biometric sensor is unavailable. " +
      "This may happen when the user is switched, the device is locked, or another pending operation prevents it."
  ),
  Lockout(
    "The operation was canceled because the API is locked out due to too many attempts. " +
      "This occurs after 5 failed attempts, and lasts for 30 seconds."
  ),
  Vendor("The operation failed due to a vendor-specific error."),
  LockoutPermanent(
    "The operation was canceled because {@link #ERROR_LOCKOUT} occurred too many times. " +
      "Biometric authentication is disabled until the user unlocks with their device credential " +
      "(i.e. PIN, pattern, or password)."
  ),
  NoDeviceCredential("The device does not have pin, pattern, or password set up."),
  UserCanceled("The user canceled the operation or pressed the negative button."),
  AuthenticationFailed("Authentication failed."),
  InvalidResult("Invalid result."),
  KeyInvalidated("Key permanently invalidated.")
}

class BiometricAuthenticatorContext(
  private val title: String,
  private val subtitle: String,
  private val fragmentActivity: FragmentActivity,
  private val negativeOption: String,
  private val biometricPromptHelper: BiometricPromptHelper = BiometricPromptHelper(),
  private val biometricManager: BiometricManager = BiometricManager.from(fragmentActivity.applicationContext)
) : BiometricAuthenticator {

  @Throws(BiometricException::class)
  override fun checkAvailability() {
    try {
      val error = when (biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricException(BiometricError.Unsupported)
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricException(BiometricError.HardwareUnavailable)
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricException(BiometricError.NoBiometricEnrolled)
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricException(BiometricError.NoHardware)
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricException(BiometricError.SecureUpdateRequired)
        else -> null
      }
      error?.let {
        Logger.log(Level.Error, it.message.toString(), it)
        throw it
      }
    } catch (e: NoSuchMethodError) {
      val exception = BiometricException(BiometricError.DependencyVersionMissMatch)
      Logger.log(Level.Error, exception.toString(), exception)
      throw exception
    }
  }

  override fun startAuthentication(signatureObject: Signature, success: (Signature) -> Unit, error: (Exception) -> Unit) {
    Logger.log(Level.Debug, "Starting signature biometric authentication")
    val signatureCryptoObject = BiometricPrompt.CryptoObject(signatureObject)
    val authenticationCallback = callBackForSignature(success, error)
    startAuthentication(signatureCryptoObject, authenticationCallback, error)
  }

  override fun startAuthentication(cipherObject: Cipher, success: (Cipher) -> Unit, error: (Exception) -> Unit) {
    Logger.log(Level.Debug, "Starting cipher biometric authentication")
    val cipherCryptoObject = BiometricPrompt.CryptoObject(cipherObject)
    val authenticationCallback = callBackForCipher(success, error)
    startAuthentication(cipherCryptoObject, authenticationCallback, error)
  }

  private fun startAuthentication(
    cryptoObject: BiometricPrompt.CryptoObject,
    authenticationCallback: BiometricPrompt.AuthenticationCallback,
    error: (Exception) -> Unit
  ) {
    try {
      checkAvailability()
      Logger.log(Level.Info, "Starting biometric authentication")
      val biometricPrompt = biometricPromptHelper.createBiometricPrompt(fragmentActivity, authenticationCallback)
      biometricPrompt.authenticate(createPromptInfo(), cryptoObject)
    } catch (e: NoSuchMethodError) {
      val exception = BiometricException(BiometricError.DependencyVersionMissMatch)
      Logger.log(Level.Error, exception.toString(), exception)
      error(exception)
    } catch (e: Exception) {
      Logger.log(Level.Error, e.toString(), e)
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

  private fun callBackForSignature(success: (Signature) -> Unit, error: (Exception) -> Unit): BiometricPrompt.AuthenticationCallback {
    return object : AuthenticationCallback(AuthenticationType.Signature, error) {
      override fun onSignatureAuthenticationSucceeded(signature: Signature) {
        Logger.log(Level.Info, "Signature authentication succeeded")
        success(signature)
      }
    }
  }

  private fun callBackForCipher(success: (Cipher) -> Unit, error: (Exception) -> Unit): BiometricPrompt.AuthenticationCallback {
    return object : AuthenticationCallback(AuthenticationType.Cipher, error) {
      override fun onCipherAuthenticationSucceeded(cipher: Cipher) {
        Logger.log(Level.Info, "Cipher authentication succeeded")
        success(cipher)
      }
    }
  }
}

class BiometricPromptHelper {
  fun createBiometricPrompt(fragmentActivity: FragmentActivity, callback: BiometricPrompt.AuthenticationCallback): BiometricPrompt {
    val executor = ContextCompat.getMainExecutor(fragmentActivity)
    return BiometricPrompt(fragmentActivity, executor, callback)
  }
}

abstract class AuthenticationCallback(private val authenticationType: AuthenticationType, private val error: (Exception) -> Unit) :
  BiometricPrompt.AuthenticationCallback() {

  enum class AuthenticationType {
    Signature, Cipher
  }

  override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
    super.onAuthenticationError(errorCode, errString)
    sendError(errorMapper(errorCode, errString))
  }

  override fun onAuthenticationFailed() {
    super.onAuthenticationFailed()
    sendError(BiometricException(BiometricError.AuthenticationFailed))
  }

  override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
    super.onAuthenticationSucceeded(result)
    when (authenticationType) {
      AuthenticationType.Signature -> {
        result.cryptoObject?.signature?.let {
          onSignatureAuthenticationSucceeded(it)
        } ?: sendError(BiometricException(BiometricError.InvalidResult))
      }
      AuthenticationType.Cipher -> {
        result.cryptoObject?.cipher?.let {
          onCipherAuthenticationSucceeded(it)
        } ?: sendError(BiometricException(BiometricError.InvalidResult))
      }
    }
  }

  private fun sendError(exception: BiometricException) {
    Logger.log(Level.Error, exception.toString(), exception)
    error(exception)
  }

  open fun onSignatureAuthenticationSucceeded(signature: Signature) {}
  open fun onCipherAuthenticationSucceeded(cipher: Cipher) {}

  private fun errorMapper(errorCode: Int, errString: CharSequence): BiometricException {
    return when (errorCode) {
      BiometricPrompt.ERROR_HW_UNAVAILABLE -> BiometricException(BiometricError.HardwareUnavailable)
      BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> BiometricException(BiometricError.UnableToProcess)
      BiometricPrompt.ERROR_TIMEOUT -> BiometricException(BiometricError.Timeout)
      BiometricPrompt.ERROR_NO_SPACE -> BiometricException(BiometricError.DeviceStorage)
      BiometricPrompt.ERROR_CANCELED -> BiometricException(BiometricError.OperationCanceled)
      BiometricPrompt.ERROR_LOCKOUT -> BiometricException(BiometricError.Lockout)
      BiometricPrompt.ERROR_VENDOR -> BiometricException(BiometricError.Vendor)
      BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BiometricException(BiometricError.LockoutPermanent)
      BiometricPrompt.ERROR_USER_CANCELED -> BiometricException(BiometricError.UserCanceled)
      BiometricPrompt.ERROR_NO_BIOMETRICS -> BiometricException(BiometricError.NoBiometricEnrolled)
      BiometricPrompt.ERROR_HW_NOT_PRESENT -> BiometricException(BiometricError.NoHardware)
      BiometricPrompt.ERROR_NEGATIVE_BUTTON -> BiometricException(BiometricError.UserCanceled)
      BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> BiometricException(BiometricError.NoDeviceCredential)
      BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED -> BiometricException(BiometricError.SecureUpdateRequired)
      else -> BiometricException(errString.toString())
    }
  }
}
