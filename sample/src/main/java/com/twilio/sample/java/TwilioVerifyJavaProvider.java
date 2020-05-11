package com.twilio.sample.java;

import android.content.Context;
import com.twilio.sample.networking.AuthenticationProvider;
import com.twilio.sample.networking.OkHttpProvider;
import com.twilio.sample.networking.SampleBackendAPIClient;
import com.twilio.verify.TwilioVerify;
import com.twilio.verify.TwilioVerifyException;
import okhttp3.OkHttpClient;

import static com.twilio.sample.networking.OkHttpProviderKt.okHttpClient;

/*
 * Copyright (c) 2020, Twilio Inc.
 */
public class TwilioVerifyJavaProvider {

  static TwilioVerifyJavaAdapter twilioVerifyJavaAdapter;

  public static TwilioVerifyJavaAdapter getInstance(Context applicationContext, String url)
      throws TwilioVerifyException {
    if (twilioVerifyJavaAdapter == null) {
      OkHttpClient okHttpClient = okHttpClient();
      SampleBackendAPIClient sampleBackendAPIClient = new SampleBackendAPIClient(okHttpClient);
      TwilioVerify twilioVerify = new TwilioVerify.Builder(
          applicationContext,
          new AuthenticationProvider(url, okHttpClient())
      ).networkProvider(new OkHttpProvider(okHttpClient)).build();
      twilioVerifyJavaAdapter = new TwilioVerifyJavaAdapter(twilioVerify, sampleBackendAPIClient);
    }
    return twilioVerifyJavaAdapter;
  }
}
