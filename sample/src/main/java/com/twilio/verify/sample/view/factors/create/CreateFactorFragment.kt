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

package com.twilio.verify.sample.view.factors.create

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.models.Factor
import com.twilio.verify.networking.NetworkException
import com.twilio.verify.sample.R
import com.twilio.verify.sample.databinding.FragmentCreateFactorBinding
import com.twilio.verify.sample.model.CreateFactorData
import com.twilio.verify.sample.view.showError
import com.twilio.verify.sample.viewmodel.FactorError
import com.twilio.verify.sample.viewmodel.FactorViewModel
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class CreateFactorFragment : Fragment() {

  private lateinit var token: String
  private val factorViewModel: FactorViewModel by activityViewModel()
  private var _binding: FragmentCreateFactorBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentCreateFactorBinding.inflate(inflater, container, false)
    val view = binding.root
    return view
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    binding.createFactorButton.setOnClickListener {
      startCreateFactor()
    }
    getPushToken()
    factorViewModel.getFactor()
      .observe(
        viewLifecycleOwner,
        Observer {
          binding.createFactorButton.isEnabled = true
          when (it) {
            is com.twilio.verify.sample.viewmodel.Factor -> onSuccess(it.factor)
            is FactorError -> showError(it.exception)
          }
        }
      )
  }

  private fun getPushToken() {
    FirebaseMessaging.getInstance().token.addOnCompleteListener(
      OnCompleteListener { task ->
        if (!task.isSuccessful) {
          task.exception?.let { it.showError(binding.content) }
          return@OnCompleteListener
        }
        task.result?.let {
          token = it
        }
      }
    )
  }

  private fun startCreateFactor() {
    hideKeyboardFrom()
    when {
      binding.includePushTokenCheck.isChecked && !this::token.isInitialized ->
        IllegalArgumentException("Invalid push token").showError(binding.content)
      binding.identityInput.text.toString()
        .isEmpty() -> IllegalArgumentException("Invalid identity").showError(
        binding.content
      )
      binding.accessTokenUrlInput.text.toString()
        .isEmpty() || binding.accessTokenUrlInput.text.toString()
        .toHttpUrlOrNull() == null ->
        IllegalArgumentException(
          "Invalid access token url"
        ).showError(
          binding.content
        )
      else -> {
        createFactor(binding.identityInput.text.toString(), binding.accessTokenUrlInput.text.toString())
      }
    }
  }

  private fun createFactor(
    identity: String,
    accessTokenUrl: String
  ) {
    binding.createFactorButton.isEnabled = false
    val pushToken = if (binding.includePushTokenCheck.isChecked) token else null
    val metadata = if (!binding.includePushTokenCheck.isChecked) mapOf("os" to "Android") else null
    val createFactorData =
      CreateFactorData(identity, "$identity's factor", accessTokenUrl, pushToken, metadata)
    factorViewModel.createFactor(createFactorData)
  }

  private fun onSuccess(factor: Factor) {
    Snackbar.make(binding.content, "Factor ${factor.sid} created", Snackbar.LENGTH_LONG)
      .show()
    findNavController().navigate(R.id.action_show_new_factor)
  }

  private fun hideKeyboardFrom() {
    val imm = activity?.getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(binding.content.windowToken, 0)
  }

  private fun showError(exception: Throwable) {
    when (exception) {
      is TwilioVerifyException -> handleNetworkException(exception as TwilioVerifyException)
      else -> exception.showError(binding.content)
    }
  }

  private fun handleNetworkException(exception: TwilioVerifyException) {
    (exception.cause as? NetworkException)?.failureResponse?.apiError?.let {
      val snackbar = Snackbar.make(
        binding.content,
        "Code: ${it.code} - ${it.message}",
        Snackbar.LENGTH_INDEFINITE
      )
      snackbar.setAction(R.string.dismiss) {
        snackbar.dismiss()
      }
      snackbar.show()
    } ?: exception.showError(binding.content)
  }
}
