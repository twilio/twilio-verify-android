package com.twilio.verify.data

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.text.ParseException
import java.util.concurrent.TimeUnit.MILLISECONDS

/*
 * Copyright (c) 2020, Twilio Inc.
 */

@RunWith(RobolectricTestRunner::class)
class DateProviderTest {

  private val preferences: SharedPreferences = mock()
  private val dateProvider: DateProvider = DateAdapter(preferences)

  @Test
  fun testGetCurrentTime_withTimeCorrectionNotStored_shouldReturnLocalTime() {
    assertEquals(MILLISECONDS.toSeconds(System.currentTimeMillis()), dateProvider.getCurrentTime())
  }

  @Test
  fun testGetCurrentTime_withTimeCorrectionStored_shouldReturnLocalTimePlusTimeCorrection() {
    val timeCorrection = 1000L
    whenever(preferences.getLong(timeCorrectionKey, 0)).thenReturn(timeCorrection)
    assertEquals(
        MILLISECONDS.toSeconds(System.currentTimeMillis()) + timeCorrection,
        dateProvider.getCurrentTime()
    )
  }

  @Test
  fun testSyncTime_withValidDate_shouldStoreTimeCorrection() {
    val editor: Editor = mock()
    val date = "Tue, 21 Jul 2020 17:07:32 GMT"
    whenever(preferences.edit()).thenReturn(editor)
    whenever(
        preferences.edit()
            .putLong(any(), any())
    ).thenReturn(editor)
    dateProvider.syncTime(date)
    verify(
        preferences.edit())
            .putLong(
                timeCorrectionKey,
                MILLISECONDS.toSeconds(fromRFC1123Date(date).time) - MILLISECONDS.toSeconds(
                    System.currentTimeMillis()
                )
            )

  }

  @Test(expected = ParseException::class)
  fun testSyncTime_withInvalidDate_shouldThrow() {
    val date = "invalidDate"
    dateProvider.syncTime(date)
  }
}