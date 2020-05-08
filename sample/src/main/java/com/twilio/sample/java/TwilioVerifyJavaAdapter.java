package com.twilio.sample.java;

import com.twilio.sample.TwilioVerifyAdapter;
import com.twilio.sample.model.CreateFactorData;
import com.twilio.sample.model.EnrollmentResponse;
import com.twilio.sample.networking.SampleBackendAPIClient;
import com.twilio.sample.push.NewChallenge;
import com.twilio.sample.push.VerifyEventBus;
import com.twilio.verify.TwilioVerify;
import com.twilio.verify.TwilioVerifyException;
import com.twilio.verify.models.Challenge;
import com.twilio.verify.models.Factor;
import com.twilio.verify.models.FactorInput;
import com.twilio.verify.models.PushFactorInput;
import com.twilio.verify.models.UpdateChallengeInput;
import com.twilio.verify.models.VerifyFactorInput;
import com.twilio.verify.models.VerifyPushFactorInput;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

/*
 * Copyright (c) 2020, Twilio Inc.
 */
public class TwilioVerifyJavaAdapter implements TwilioVerifyAdapter {

  private final TwilioVerify twilioVerify;
  private final SampleBackendAPIClient sampleBackendAPIClient;

  public TwilioVerifyJavaAdapter(TwilioVerify twilioVerify,
      SampleBackendAPIClient sampleBackendAPIClient) {
    this.twilioVerify = twilioVerify;
    this.sampleBackendAPIClient = sampleBackendAPIClient;
  }

  @Override public void createFactor(@NotNull final CreateFactorData createFactorData,
      @NotNull final Function1<? super Factor, Unit> onSuccess,
      @NotNull final Function1<? super Exception, Unit> onError) {
    sampleBackendAPIClient.enrollment(createFactorData.getJwtUrl(), createFactorData.getIdentity(),
        new Function1<EnrollmentResponse, Unit>() {
          @Override public Unit invoke(EnrollmentResponse enrollmentResponse) {
            createFactor(createFactorData, enrollmentResponse, onSuccess, onError);
            return Unit.INSTANCE;
          }
        }, onError);
  }

  @Override public void verifyFactor(@NotNull VerifyFactorInput verifyFactorInput,
      @NotNull Function1<? super Factor, Unit> onSuccess,
      @NotNull Function1<? super TwilioVerifyException, Unit> onError) {
    twilioVerify.verifyFactor(verifyFactorInput, onSuccess, onError);
  }

  @Override public void updateChallenge(@NotNull UpdateChallengeInput updateChallengeInput,
      @NotNull Function0<Unit> onSuccess,
      @NotNull Function1<? super TwilioVerifyException, Unit> onError) {
    twilioVerify.updateChallenge(updateChallengeInput, onSuccess, onError);
  }

  @Override public void getChallenge(@NotNull String challengeSid, @NotNull String factorSid) {
    twilioVerify.getChallenge(challengeSid, factorSid, new Function1<Challenge, Unit>() {
      @Override public Unit invoke(Challenge challenge) {
        VerifyEventBus.INSTANCE.send(new NewChallenge(challenge));
        return Unit.INSTANCE;
      }
    }, handleError);
  }

  @Override public void getChallenge(@NotNull String challengeSid, @NotNull String factorSid,
      @NotNull Function1<? super Challenge, Unit> onSuccess,
      @NotNull Function1<? super TwilioVerifyException, Unit> onError) {
    twilioVerify.getChallenge(challengeSid, factorSid, onSuccess, onError);
  }

  private Function1<? super TwilioVerifyException, Unit> handleError =
      new Function1<TwilioVerifyException, Unit>() {
        @Override public Unit invoke(TwilioVerifyException e) {
          e.printStackTrace();
          return Unit.INSTANCE;
        }
      };

  private void createFactor(CreateFactorData createFactorData,
      EnrollmentResponse enrollmentResponse,
      final Function1<? super Factor, Unit> onSuccess,
      final Function1<? super Exception, Unit> onError) {
    FactorInput factorInput;
    switch (enrollmentResponse.getFactorType()) {
      case PUSH:
        factorInput = new PushFactorInput(createFactorData.getFactorName(),
            enrollmentResponse.getServiceSid(),
            enrollmentResponse.getIdentity(), createFactorData.getPushToken(),
            enrollmentResponse.getToken());
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + enrollmentResponse.getFactorType());
    }
    twilioVerify.createFactor(factorInput,
        new Function1<Factor, Unit>() {
          @Override public Unit invoke(Factor factor) {
            onFactorCreated(factor, onSuccess, onError);
            return Unit.INSTANCE;
          }
        }, onError);
  }

  private void onFactorCreated(Factor factor,
      final Function1<? super Factor, Unit> onSuccess,
      final Function1<? super Exception, Unit> onError) {
    switch (factor.getType()) {
      case PUSH:
        twilioVerify.verifyFactor(new VerifyPushFactorInput(factor.getSid()), onSuccess, onError);
        break;
      default:
        onSuccess.invoke(factor);
        break;
    }
  }
}
