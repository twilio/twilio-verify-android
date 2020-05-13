package com.twilio.verify.sample

import android.app.Application
import com.google.firebase.FirebaseApp
import com.twilio.verify.sample.kotlin.TwilioVerifyKotlinProvider
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class AppApplication : Application() {

  private val appModule = module {
    single<TwilioVerifyAdapter> {
      TwilioVerifyKotlinProvider.getInstance(
          androidContext(),
          BuildConfig.JWT_URL
      )
    }
  }

  override fun onCreate() {
    super.onCreate()
    FirebaseApp.initializeApp(this)
    startKoin {
      androidLogger()
      androidContext(this@AppApplication)
      modules(appModule)
    }
  }
}