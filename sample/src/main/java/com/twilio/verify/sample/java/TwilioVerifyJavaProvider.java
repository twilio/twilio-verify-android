package com.twilio.verify.sample.java;

import android.content.Context;
import com.twilio.verify.TwilioVerify;
import com.twilio.verify.TwilioVerifyException;
import com.twilio.verify.sample.BuildConfig;
import com.twilio.verify.sample.networking.OkHttpProvider;
import com.twilio.verify.sample.networking.SampleBackendAPIClient;
import okhttp3.OkHttpClient;

import static com.twilio.verify.sample.networking.OkHttpProviderKt.okHttpClient;

/*
 * Copyright (c) 2020, Twilio Inc.
 */
public class TwilioVerifyJavaProvider {

  static TwilioVerifyJavaAdapter twilioVerifyJavaAdapter;

  public static TwilioVerifyJavaAdapter getInstance(Context applicationContext, String url)
      throws TwilioVerifyException {
    if (twilioVerifyJavaAdapter == null) {
      OkHttpClient okHttpClient = okHttpClient();
      SampleBackendAPIClient sampleBackendAPIClient = new SampleBackendAPIClient(okHttpClient,
          BuildConfig.JWT_URL);
      TwilioVerify twilioVerify = new TwilioVerify.Builder(
          applicationContext).networkProvider(new OkHttpProvider(okHttpClient)).build();
      twilioVerifyJavaAdapter = new TwilioVerifyJavaAdapter(twilioVerify, sampleBackendAPIClient);
    }
    return twilioVerifyJavaAdapter;
  }
}
