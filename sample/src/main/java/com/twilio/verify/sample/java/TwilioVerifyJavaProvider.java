package com.twilio.verify.sample.java;

import android.content.Context;
import com.twilio.verify.TwilioVerify;
import com.twilio.verify.TwilioVerifyException;

/*
 * Copyright (c) 2020, Twilio Inc.
 */
public class TwilioVerifyJavaProvider {

  static TwilioVerifyJavaAdapter twilioVerifyJavaAdapter;

  public static TwilioVerifyJavaAdapter getInstance(Context applicationContext)
      throws TwilioVerifyException {
    if (twilioVerifyJavaAdapter == null) {
      TwilioVerify twilioVerify = new TwilioVerify.Builder(
          applicationContext).build();
      twilioVerifyJavaAdapter = new TwilioVerifyJavaAdapter(twilioVerify);
    }
    return twilioVerifyJavaAdapter;
  }
}
