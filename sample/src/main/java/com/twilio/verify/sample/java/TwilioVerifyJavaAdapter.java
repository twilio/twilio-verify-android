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

package com.twilio.verify.sample.java;

import com.twilio.verify.TwilioVerify;
import com.twilio.verify.TwilioVerifyException;
import com.twilio.verify.models.Challenge;
import com.twilio.verify.models.ChallengeList;
import com.twilio.verify.models.ChallengeListPayload;
import com.twilio.verify.models.Factor;
import com.twilio.verify.models.FactorPayload;
import com.twilio.verify.models.PushFactorPayload;
import com.twilio.verify.models.UpdateChallengePayload;
import com.twilio.verify.models.UpdatePushFactorPayload;
import com.twilio.verify.models.VerifyFactorPayload;
import com.twilio.verify.models.VerifyPushFactorPayload;
import com.twilio.verify.sample.TwilioVerifyAdapter;
import com.twilio.verify.sample.model.AccessTokenResponse;
import com.twilio.verify.sample.model.AccessTokenResponseKt;
import com.twilio.verify.sample.model.CreateFactorData;
import com.twilio.verify.sample.networking.SampleBackendAPIClient;
import com.twilio.verify.sample.networking.SampleBackendAPIClientKt;
import com.twilio.verify.sample.push.NewChallenge;
import com.twilio.verify.sample.push.VerifyEventBus;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class TwilioVerifyJavaAdapter implements TwilioVerifyAdapter {

  private final TwilioVerify twilioVerify;

  TwilioVerifyJavaAdapter(TwilioVerify twilioVerify) {
    this.twilioVerify = twilioVerify;
  }

  @Override public void createFactor(@NotNull final CreateFactorData createFactorData,
      @NotNull SampleBackendAPIClient sampleBackendAPIClient,
      @NotNull final Function1<? super Factor, Unit> success,
      @NotNull final Function1<? super Throwable, Unit> error) {
    SampleBackendAPIClientKt.getAccessTokenResponse(sampleBackendAPIClient,
        createFactorData.getIdentity(), createFactorData.getAccessTokenUrl(), accessTokenResponse -> {
          FactorPayload factorPayload = getFactorPayload(createFactorData, accessTokenResponse);
          twilioVerify.createFactor(factorPayload,
              factor -> {
                verifyFactor(factor, success, error);
                return Unit.INSTANCE;
              }, error);
          return Unit.INSTANCE;
        }, error);
  }

  @Override public void verifyFactor(@NotNull VerifyFactorPayload verifyFactorPayload,
      @NotNull Function1<? super Factor, Unit> success,
      @NotNull Function1<? super TwilioVerifyException, Unit> error) {
    twilioVerify.verifyFactor(verifyFactorPayload, success, error);
  }

  @Override public void updateChallenge(@NotNull UpdateChallengePayload updateChallengePayload,
      @NotNull Function0<Unit> success,
      @NotNull Function1<? super TwilioVerifyException, Unit> error) {
    twilioVerify.updateChallenge(updateChallengePayload, success, error);
  }

  @Override
  public void showChallenge(@NotNull final String challengeSid, @NotNull final String factorSid) {
    VerifyEventBus.INSTANCE.send(new NewChallenge(challengeSid, factorSid));
  }

  @Override public void getChallenge(@NotNull String challengeSid, @NotNull String factorSid,
      @NotNull Function1<? super Challenge, Unit> success,
      @NotNull Function1<? super TwilioVerifyException, Unit> error) {
    twilioVerify.getChallenge(challengeSid, factorSid, success, error);
  }

  @Override public void getFactors(@NotNull Function1<? super List<? extends Factor>, Unit> success,
      @NotNull Function1<? super TwilioVerifyException, Unit> error) {
    twilioVerify.getAllFactors(success, error);
  }

  @Override public void deleteFactor(@NotNull String factorSid, @NotNull Function0<Unit> success,
      @NotNull Function1<? super TwilioVerifyException, Unit> error) {
    twilioVerify.deleteFactor(factorSid, success, error);
  }

  @Override public void getAllChallenges(@NotNull ChallengeListPayload challengeListPayload,
      @NotNull Function1<? super ChallengeList, Unit> success,
      @NotNull Function1<? super TwilioVerifyException, Unit> error) {
    twilioVerify.getAllChallenges(challengeListPayload, success, error);
  }

  @Override public void updatePushToken(@NotNull final String token) {
    twilioVerify.getAllFactors(factors -> {
      for (Factor factor : factors) {
        updateFactor(factor, token);
      }
      return Unit.INSTANCE;
    }, handleError);
  }

  private Function1<? super TwilioVerifyException, Unit> handleError =
      (Function1<TwilioVerifyException, Unit>) e -> {
        e.printStackTrace();
        return Unit.INSTANCE;
      };

  private FactorPayload getFactorPayload(CreateFactorData createFactorData,
                                         AccessTokenResponse accessTokenResponse) {
    switch (AccessTokenResponseKt.getFactorType(accessTokenResponse)) {
      case PUSH:
        return new PushFactorPayload(createFactorData.getFactorName(),
            accessTokenResponse.getServiceSid(),
            accessTokenResponse.getIdentity(), createFactorData.getPushToken(),
            accessTokenResponse.getToken());
      default:
        throw new IllegalStateException("Unexpected value: " + accessTokenResponse.getFactorType());
    }
  }

  private void verifyFactor(Factor factor,
      final Function1<? super Factor, Unit> onSuccess,
      final Function1<? super Exception, Unit> onError) {
    switch (factor.getType()) {
      case PUSH:
        verifyFactor(new VerifyPushFactorPayload(factor.getSid()), onSuccess, onError);
        break;
    }
  }

  private void updateFactor(Factor factor, String token) {
    twilioVerify.updateFactor(new UpdatePushFactorPayload(factor.getSid(), token),
        factor1 -> Unit.INSTANCE, handleError);
  }
}
