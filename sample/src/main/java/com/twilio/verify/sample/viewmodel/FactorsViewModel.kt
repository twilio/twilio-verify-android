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

class FactorsViewModel(private val twilioVerifyAdapter: TwilioVerifyAdapter) : ViewModel() {
  private val factors: MutableLiveData<Factors> = MutableLiveData()

  fun loadFactors() {
    twilioVerifyAdapter.getFactors(
      { factorList ->
        factors.value = FactorList(factorList.sortedByDescending { it.createdAt })
      },
      {
        factors.value = FactorsError(it)
      }
    )
  }

  fun deleteFactor(sid: String) {
    twilioVerifyAdapter.deleteFactor(
      sid,
      {
        loadFactors()
      },
      {
        factors.value = DeleteFactorError(it)
        loadFactors()
      }
    )
  }

  fun getFactors(): LiveData<Factors> {
    return factors
  }
}

sealed class Factors
class FactorList(val factors: List<Factor>) : Factors()
class FactorsError(val exception: Exception) : Factors()
class DeleteFactorError(val exception: Exception) : Factors()
