/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twilio.verify.models.Service
import com.twilio.verify.sample.TwilioVerifyAdapter

class ServiceViewModel(private val twilioVerifyAdapter: TwilioVerifyAdapter) : ViewModel() {
  private val service: MutableLiveData<ServiceResult> = MutableLiveData()

  fun loadService(
    sid: String
  ) {
    twilioVerifyAdapter.getService(sid, {
      service.value = com.twilio.verify.sample.viewmodel.Service(it)
    }, {
      service.value = ServiceError(it)
    })
  }

  fun getService(): LiveData<ServiceResult> {
    return service
  }
}

sealed class ServiceResult
class Service(val service: Service) : ServiceResult()
class ServiceError(val exception: Exception) : ServiceResult()