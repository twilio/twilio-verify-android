package com.twilio.verify.sample

import android.app.Application
import com.google.firebase.FirebaseApp
import com.twilio.verify.sample.kotlin.TwilioVerifyKotlinProvider
import com.twilio.verify.sample.viewmodel.ChallengeViewModel
import com.twilio.verify.sample.viewmodel.ChallengesViewModel
import com.twilio.verify.sample.viewmodel.FactorViewModel
import com.twilio.verify.sample.viewmodel.FactorsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class AppApplication : Application() {

  private val appModule = module {
    single<TwilioVerifyAdapter> {
      TwilioVerifyKotlinProvider.getInstance(
        androidContext()
      )
    }
  }

  private val viewModelModule = module {
    viewModel { FactorsViewModel(get()) }
    viewModel { FactorViewModel(get()) }
    viewModel { ChallengesViewModel(get()) }
    viewModel { ChallengeViewModel(get()) }
  }

  override fun onCreate() {
    super.onCreate()
    FirebaseApp.initializeApp(this)
    startKoin {
      androidLogger()
      androidContext(this@AppApplication)
      modules(appModule, viewModelModule)
    }
  }
}
