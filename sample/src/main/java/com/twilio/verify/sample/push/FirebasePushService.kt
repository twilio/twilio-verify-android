package com.twilio.verify.sample.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.twilio.verify.sample.R
import com.twilio.verify.sample.TwilioVerifyAdapter
import com.twilio.verify.sample.view.MainActivity
import com.twilio.verify.sample.view.challenges.update.ARG_CHALLENGE_SID
import com.twilio.verify.sample.view.challenges.update.ARG_FACTOR_SID
import org.koin.android.ext.android.inject

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal const val typeKey = "type"
internal const val factorSidKey = "factor_sid"
internal const val challengeType = "verify_push_challenge"
internal const val challengeSidKey = "challenge_sid"
internal const val messageKey = "message"
internal const val channelId = "challenges"

class FirebasePushService() : FirebaseMessagingService() {

  @VisibleForTesting
  val twilioVerifyAdapter: TwilioVerifyAdapter by inject()

  override fun onNewToken(token: String) {
    Log.d("newToken", token)
    twilioVerifyAdapter.updatePushToken(token)
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val bundle = getBundleFromMessage(remoteMessage)
    when (bundle.getString(typeKey)) {
      challengeType -> newChallenge(bundle)
    }
  }

  private fun newChallenge(bundle: Bundle) {
    val factorSid = bundle.getString(factorSidKey)
    val challengeSid = bundle.getString(challengeSidKey)
    val message = bundle.getString(messageKey)
    if (factorSid != null && challengeSid != null) {
      twilioVerifyAdapter.showChallenge(challengeSid, factorSid)
      message?.let {
        createNotificationChannel()
        val i = Intent(this, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        i.putExtras(bundleOf(ARG_FACTOR_SID to factorSid, ARG_CHALLENGE_SID to challengeSid))
        val pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT)
        val builder = NotificationCompat.Builder(
            this,
            channelId
        )
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_challenge)
            .setContentTitle(getString(R.string.new_challenge))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(this)) {
          notify(challengeSid.hashCode(), builder.build())
        }
      }
    }
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = getString(R.string.channel_name)
      val descriptionText = getString(R.string.channel_description)
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val channel = NotificationChannel(channelId, name, importance).apply {
        description = descriptionText
      }
      val notificationManager: NotificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  private fun getBundleFromMessage(remoteMessage: RemoteMessage?): Bundle {
    return Bundle().apply {
      remoteMessage?.data?.entries?.let { entries ->
        for (entry in entries) {
          putString(entry.key, entry.value)
        }
      }
    }
  }
}