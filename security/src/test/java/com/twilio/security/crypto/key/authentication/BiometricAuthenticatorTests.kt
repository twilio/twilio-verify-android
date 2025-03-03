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
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.security.Signature
import javax.crypto.Cipher
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BiometricAuthenticatorTests {
  private val title = "title"
  private val subtitle = "subtitle"
  private val negativeOption = "negativeOption"
  private val fragmentActivity: FragmentActivity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
  private val biometricPromptHelper: BiometricPromptHelper = mockk()
  private val biometricManager: BiometricManager = mockk()
  private val authenticator =
    BiometricAuthenticatorContext(title, subtitle, fragmentActivity, negativeOption, biometricPromptHelper, biometricManager)

  @Test(expected = Test.None::class)
  fun `Availability success`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) }.returns(BiometricManager.BIOMETRIC_SUCCESS)
    authenticator.checkAvailability()
  }

  @Test
  fun `Availability no dependency error`() {
    try {
      every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) }.throws(NoSuchMethodError())
      authenticator.checkAvailability()
      fail()
    } catch (e: Exception) {
      assertEquals(e.message, BiometricException(BiometricError.DependencyVersionMissMatch).message)
    }
  }

  @Test
  fun `Availability unsupported error`() {
    try {
      every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) }
        .returns(BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED)
      authenticator.checkAvailability()
      fail()
    } catch (e: Exception) {
      assertEquals(e.message, BiometricException(BiometricError.Unsupported).message)
    }
  }

  @Test
  fun `Availability hardware unavailable error`() {
    try {
      every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) }
        .returns(BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE)
      authenticator.checkAvailability()
      fail()
    } catch (e: Exception) {
      assertEquals(e.message, BiometricException(BiometricError.HardwareUnavailable).message)
    }
  }

  @Test
  fun `Availability no biometric enrolled error`() {
    try {
      every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) }
        .returns(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
      authenticator.checkAvailability()
      fail()
    } catch (e: Exception) {
      assertEquals(e.message, BiometricException(BiometricError.NoBiometricEnrolled).message)
    }
  }

  @Test
  fun `Availability no hardware error`() {
    try {
      every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) }
        .returns(BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE)
      authenticator.checkAvailability()
      fail()
    } catch (e: Exception) {
      assertEquals(e.message, BiometricException(BiometricError.NoHardware).message)
    }
  }

  @Test
  fun `Availability security update required error`() {
    try {
      every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) }
        .returns(BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED)
      authenticator.checkAvailability()
      fail()
    } catch (e: Exception) {
      assertEquals(e.message, BiometricException(BiometricError.SecureUpdateRequired).message)
    }
  }

  @Test
  fun `Start signature authentication without availability should not call biometric prompt`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) }
      .returns(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
    val signature: Signature = mockk()
    authenticator.startAuthentication(
      signature, { fail() },
      {
        verify(exactly = 0) { biometricPromptHelper.createBiometricPrompt(any(), any()) }
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with succeeded authentication should call success`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) }
      .returns(BiometricManager.BIOMETRIC_SUCCESS)
    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk()

    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()
    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) }.returns(biometricPrompt)
    val authenticationResult: BiometricPrompt.AuthenticationResult = mockk {
      every { cryptoObject }.returns(BiometricPrompt.CryptoObject(signature))
    }
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationSucceeded(authenticationResult)
    }
    authenticator.startAuthentication(
      signature,
      {
        assertEquals(it, signature)
      },
      {
        fail()
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with failed authentication should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)

    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationFailed()
    }

    authenticator.startAuthentication(
      signature,
      { fail("Expected failure callback") },
      { exception ->
        assertEquals(exception.message, BiometricException(BiometricError.AuthenticationFailed).message)
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with hardware unavailable error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)

    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_HW_UNAVAILABLE, "error")
    }

    authenticator.startAuthentication(
      signature,
      { fail("Expected failure callback") },
      { exception ->
        assertEquals(exception.message, BiometricException(BiometricError.HardwareUnavailable).message)
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with hardware unable to process error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)

    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, "error")
    }

    authenticator.startAuthentication(
      signature,
      { fail("Expected failure callback") },
      { exception ->
        assertEquals(exception.message, BiometricException(BiometricError.UnableToProcess).message)
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with timeout error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)

    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_TIMEOUT, "error")
    }

    authenticator.startAuthentication(
      signature,
      { fail("Expected failure callback") },
      { exception ->
        assertEquals(exception.message, BiometricException(BiometricError.Timeout).message)
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with no space error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)

    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_NO_SPACE, "error")
    }

    authenticator.startAuthentication(
      signature,
      { fail("Expected failure callback") },
      { exception ->
        assertEquals(exception.message, BiometricException(BiometricError.DeviceStorage).message)
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with canceled error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_CANCELED, "error")
    }

    authenticator.startAuthentication(
      signature, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.OperationCanceled).message)
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with lockout error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_LOCKOUT, "error")
    }

    authenticator.startAuthentication(
      signature, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.Lockout).message)
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with vendor error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_VENDOR, "error")
    }

    authenticator.startAuthentication(
      signature, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.Vendor).message)
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with lockout permanent error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_LOCKOUT_PERMANENT, "error")
    }

    authenticator.startAuthentication(
      signature, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.LockoutPermanent).message)
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with user canceled error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_USER_CANCELED, "error")
    }

    authenticator.startAuthentication(
      signature, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.UserCanceled).message)
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with no biometrics error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_NO_BIOMETRICS, "error")
    }

    authenticator.startAuthentication(
      signature, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.NoBiometricEnrolled).message)
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with hardware not present error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_HW_NOT_PRESENT, "error")
    }

    authenticator.startAuthentication(
      signature, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.NoHardware).message)
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with negative button error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_NEGATIVE_BUTTON, "error")
    }

    authenticator.startAuthentication(
      signature, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.UserCanceled).message)
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with no device credential error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL, "error")
    }

    authenticator.startAuthentication(
      signature, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.NoDeviceCredential).message)
      }
    )
  }

  @Test
  fun `Start signature authentication with availability with security update required error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val signature: Signature = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED, "error")
    }

    authenticator.startAuthentication(
      signature, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.SecureUpdateRequired).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with succeeded authentication should call success`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk(relaxed = true)
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt

    val authenticationResult: BiometricPrompt.AuthenticationResult = mockk {
      every { cryptoObject } returns BiometricPrompt.CryptoObject(cipher)
    }

    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationSucceeded(authenticationResult)
    }

    authenticator.startAuthentication(
      cipher,
      {
        assertEquals(it, cipher)
      },
      {
        fail("Expected success callback to be invoked")
      }
    )
  }

  @Test
  fun `Start cipher authentication without availability should not call biometric prompt`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) }
      .returns(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)

    val cipher: Cipher = mockk()

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        verify(exactly = 0) { biometricPromptHelper.createBiometricPrompt(any(), any()) }
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with failed authentication should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationFailed()
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.AuthenticationFailed).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with hardware unavailable error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_HW_UNAVAILABLE, "error")
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.HardwareUnavailable).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with hardware unable to process error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, "error")
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.UnableToProcess).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with timeout error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_TIMEOUT, "error")
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.Timeout).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with no space error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_NO_SPACE, "error")
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.DeviceStorage).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with canceled error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_CANCELED, "error")
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.OperationCanceled).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with lockout error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_LOCKOUT, "error")
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.Lockout).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with vendor error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_VENDOR, "error")
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.Vendor).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with lockout permanent error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_LOCKOUT_PERMANENT, "error")
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.LockoutPermanent).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with user canceled error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_USER_CANCELED, "error")
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.UserCanceled).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with no biometrics error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_NO_BIOMETRICS, "error")
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.NoBiometricEnrolled).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with hardware not present error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_HW_NOT_PRESENT, "error")
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.NoHardware).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with negative button error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_NEGATIVE_BUTTON, "error")
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.UserCanceled).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with no device credential error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL, "error")
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.NoDeviceCredential).message)
      }
    )
  }

  @Test
  fun `Start cipher authentication with availability with security update required error should call error with expected exception`() {
    every { biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns BiometricManager.BIOMETRIC_SUCCESS

    val cipher: Cipher = mockk()
    val biometricPrompt: BiometricPrompt = mockk()
    val authenticationCallback = slot<BiometricPrompt.AuthenticationCallback>()

    every { biometricPromptHelper.createBiometricPrompt(eq(fragmentActivity), capture(authenticationCallback)) } returns biometricPrompt
    every { biometricPrompt.authenticate(any(), any()) } answers {
      authenticationCallback.captured.onAuthenticationError(BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED, "error")
    }

    authenticator.startAuthentication(
      cipher, { fail("Expected error callback to be invoked") },
      {
        assertEquals(it.message, BiometricException(BiometricError.SecureUpdateRequired).message)
      }
    )
  }
}
