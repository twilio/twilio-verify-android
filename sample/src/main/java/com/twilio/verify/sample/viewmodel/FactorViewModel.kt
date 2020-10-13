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

package com.twilio.verify.sample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twilio.verify.models.Factor
import com.twilio.verify.sample.TwilioVerifyAdapter
import com.twilio.verify.sample.model.CreateFactorData
import com.twilio.verify.sample.networking.backendAPIClient

class FactorViewModel(private val twilioVerifyAdapter: TwilioVerifyAdapter) : ViewModel() {
  private val factor: MutableLiveData<FactorResult> = MutableLiveData()

  fun loadFactor(sid: String) {
    twilioVerifyAdapter.getFactors(
      { factors ->
        factor.value = factors.firstOrNull { it.sid == sid }
          ?.let { Factor(it) } ?: FactorError(IllegalArgumentException("Factor not found"))
      },
      {
        factor.value = FactorError(it)
      }
    )
  }

  fun getFactor(): LiveData<FactorResult> {
    return factor
  }

  fun createFactor(createFactorData: CreateFactorData) {
    twilioVerifyAdapter.createFactor(
      createFactorData, backendAPIClient(createFactorData.accessTokenUrl),
      {
        factor.value = Factor(it)
      },
      {
        factor.value = FactorError(it)
      }
    )
  }
}

sealed class FactorResult
class Factor(val factor: Factor) : FactorResult()
class FactorError(val exception: Throwable) : FactorResult()
