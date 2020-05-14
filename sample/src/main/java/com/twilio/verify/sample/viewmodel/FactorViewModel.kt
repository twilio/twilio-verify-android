/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twilio.verify.models.Factor
import com.twilio.verify.sample.TwilioVerifyAdapter
import com.twilio.verify.sample.model.CreateFactorData

class FactorViewModel(private val twilioVerifyAdapter: TwilioVerifyAdapter) : ViewModel() {
  private val factor: MutableLiveData<FactorResult> = MutableLiveData()

  fun loadFactor(sid: String) {
    twilioVerifyAdapter.getFactors({ factors ->
      factor.value = factors.firstOrNull { it.sid == sid }
          ?.let { Factor(it) } ?: FactorError(IllegalArgumentException("Factor not found"))
    }, {
      factor.value = FactorError(it)
    })
  }

  fun getFactor(): LiveData<FactorResult> {
    return factor
  }

  fun createFactor(createFactorData: CreateFactorData) {
    twilioVerifyAdapter.createFactor(createFactorData, {
      factor.value = Factor(it)
    }, {
      factor.value = FactorError(it)
    })
  }
}

sealed class FactorResult
class Factor(val factor: Factor) : FactorResult()
class FactorError(val exception: Exception) : FactorResult()