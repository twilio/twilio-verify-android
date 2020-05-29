/*
 * Copyright (c) 2020, Twilio Inc.
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
    twilioVerifyAdapter.getFactors({ result ->
      factors.value = FactorList(result.sortedByDescending { it.createdAt })
    }, {
      factors.value = FactorsError(it)
    })
  }

  fun deleteFactor(sid: String) {
    twilioVerifyAdapter.deleteFactor(sid, {
      loadFactors()
    }, {
      factors.value = DeleteFactorError(it)
      loadFactors()
    })
  }

  fun getFactors(): LiveData<Factors> {
    return factors
  }
}

sealed class Factors
class FactorList(val factors: List<Factor>) : Factors()
class FactorsError(val exception: Exception) : Factors()
class DeleteFactorError(val exception: Exception) : Factors()
