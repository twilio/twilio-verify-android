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
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import com.twilio.verify.models.Factor
import com.twilio.verify.sample.R
import com.twilio.verify.sample.model.CreateFactorData
import com.twilio.verify.sample.view.showError
import com.twilio.verify.sample.viewmodel.FactorError
import com.twilio.verify.sample.viewmodel.FactorViewModel
import kotlinx.android.synthetic.main.fragment_create_factor.accessTokenUrlInput
import kotlinx.android.synthetic.main.fragment_create_factor.content
import kotlinx.android.synthetic.main.fragment_create_factor.createFactor
import kotlinx.android.synthetic.main.fragment_create_factor.identityInput
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateFactorFragment : Fragment() {

  private lateinit var token: String
  private val factorViewModel: FactorViewModel by viewModel()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_create_factor, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    createFactor.setOnClickListener {
      startCreateFactor()
    }
    getPushToken()
    factorViewModel.getFactor()
      .observe(
        viewLifecycleOwner,
        Observer {
          createFactor.isEnabled = true
          when (it) {
            is com.twilio.verify.sample.viewmodel.Factor -> onSuccess(it.factor)
            is FactorError -> it.exception.showError(content)
          }
        }
      )
  }

  private fun getPushToken() {
    FirebaseInstanceId.getInstance()
      .instanceId.addOnCompleteListener(
        OnCompleteListener { task ->
          if (!task.isSuccessful) {
            task.exception?.let { it.showError(content) }
            return@OnCompleteListener
          }
          task.result?.token?.let {
            token = it
          }
        }
      )
  }

  private fun startCreateFactor() {
    hideKeyboardFrom()
    when {
      !this::token.isInitialized ->
        IllegalArgumentException("Invalid push token").showError(content)
      identityInput.text.toString()
        .isEmpty() -> IllegalArgumentException("Invalid identity").showError(
        content
      )
      accessTokenUrlInput.text.toString()
        .isEmpty() || accessTokenUrlInput.text.toString()
        .toHttpUrlOrNull() == null ->
        IllegalArgumentException(
          "Invalid access token url"
        ).showError(
          content
        )
      else -> {
        createFactor(identityInput.text.toString(), accessTokenUrlInput.text.toString())
      }
    }
  }

  private fun createFactor(
    identity: String,
    accessTokenUrl: String
  ) {
    createFactor.isEnabled = false
    val createFactorData = CreateFactorData(identity, "$identity's factor", token, accessTokenUrl)
    factorViewModel.createFactor(createFactorData)
  }

  private fun onSuccess(factor: Factor) {
    Snackbar.make(content, "Factor ${factor.sid} created", LENGTH_SHORT)
    findNavController().navigate(R.id.action_show_new_factor)
  }

  private fun hideKeyboardFrom() {
    val imm = activity?.getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(content.windowToken, 0)
  }
}
