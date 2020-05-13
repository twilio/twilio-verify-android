/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample

import android.app.Application
import com.twilio.verify.sample.kotlin.TwilioVerifyKotlinProvider
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class UnitTestApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidLogger()
      androidContext(this@UnitTestApplication)
      modules(appModule)
    }
  }

  fun updateTwilioVerifyDependency(twilioVerifyAdapter: TwilioVerifyAdapter) {
    unloadKoinModules(appModule)
    appModule = module {
      single {
        twilioVerifyAdapter
      }
    }
    loadKoinModules(appModule)
  }
}

private var appModule = module {
  single<TwilioVerifyAdapter> {
    TwilioVerifyKotlinProvider.getInstance(
        androidContext(),
        BuildConfig.JWT_URL
    )
  }
}