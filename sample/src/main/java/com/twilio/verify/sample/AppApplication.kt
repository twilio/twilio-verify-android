/*
 * Copyright (c) 2020 Twilio Inc.
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
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.module

class AppApplication : Application() {

  private val appModule = module {
    single<TwilioVerifyAdapter> {
      TwilioVerifyKotlinProvider.getInstance(
        androidContext()
      )
    }
  }

  private val viewModelModule = module {
    viewModelOf(::FactorsViewModel)
    viewModelOf(::FactorViewModel)
    viewModelOf(::ChallengesViewModel)
    viewModelOf(::ChallengeViewModel)
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
