package com.twilio.verify.sample.java;

import com.twilio.verify.TwilioVerify;
import com.twilio.verify.TwilioVerifyException;
import com.twilio.verify.models.Challenge;
import com.twilio.verify.models.ChallengeList;
import com.twilio.verify.models.ChallengeListInput;
import com.twilio.verify.models.Factor;
import com.twilio.verify.models.FactorInput;
import com.twilio.verify.models.PushFactorInput;
import com.twilio.verify.models.UpdateChallengeInput;
import com.twilio.verify.models.UpdatePushFactorInput;
import com.twilio.verify.models.VerifyFactorInput;
import com.twilio.verify.models.VerifyPushFactorInput;
import com.twilio.verify.sample.TwilioVerifyAdapter;
import com.twilio.verify.sample.model.CreateFactorData;
import com.twilio.verify.sample.model.EnrollmentResponse;
import com.twilio.verify.sample.model.EnrollmentResponseKt;
import com.twilio.verify.sample.networking.SampleBackendAPIClient;
import com.twilio.verify.sample.networking.SampleBackendAPIClientKt;
import com.twilio.verify.sample.push.NewChallenge;
import com.twilio.verify.sample.push.VerifyEventBus;
import java.util.List;
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
      @NotNull final Function1<? super Factor, Unit> success,
      @NotNull final Function1<? super Throwable, Unit> error) {
    SampleBackendAPIClientKt.getEnrollmentResponse(sampleBackendAPIClient,
        createFactorData.getIdentity(), enrollmentResponse -> {
          FactorInput factorInput = getFactorInput(createFactorData, enrollmentResponse);
          twilioVerify.createFactor(factorInput,
              factor -> {
                verifyFactor(factor, success, error);
                return Unit.INSTANCE;
              }, error);
          return Unit.INSTANCE;
        }, error);
  }

  @Override public void verifyFactor(@NotNull VerifyFactorInput verifyFactorInput,
      @NotNull Function1<? super Factor, Unit> success,
      @NotNull Function1<? super TwilioVerifyException, Unit> error) {
    twilioVerify.verifyFactor(verifyFactorInput, success, error);
  }

  @Override public void updateChallenge(@NotNull UpdateChallengeInput updateChallengeInput,
      @NotNull Function0<Unit> success,
      @NotNull Function1<? super TwilioVerifyException, Unit> error) {
    twilioVerify.updateChallenge(updateChallengeInput, success, error);
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

  @Override public void getAllChallenges(@NotNull ChallengeListInput challengeListInput,
      @NotNull Function1<? super ChallengeList, Unit> success,
      @NotNull Function1<? super TwilioVerifyException, Unit> error) {
    twilioVerify.getAllChallenges(challengeListInput, success, error);
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

  private FactorInput getFactorInput(CreateFactorData createFactorData,
      EnrollmentResponse enrollmentResponse) {
    switch (EnrollmentResponseKt.getFactorType(enrollmentResponse)) {
      case PUSH:
        return new PushFactorInput(createFactorData.getFactorName(),
            enrollmentResponse.getServiceSid(),
            enrollmentResponse.getIdentity(), createFactorData.getPushToken(),
            enrollmentResponse.getToken());
      default:
        throw new IllegalStateException("Unexpected value: " + enrollmentResponse.getFactorType());
    }
  }

  private void verifyFactor(Factor factor,
      final Function1<? super Factor, Unit> onSuccess,
      final Function1<? super Exception, Unit> onError) {
    switch (factor.getType()) {
      case PUSH:
        verifyFactor(new VerifyPushFactorInput(factor.getSid()), onSuccess, onError);
        break;
    }
  }

  private void updateFactor(Factor factor, String token) {
    twilioVerify.updateFactor(new UpdatePushFactorInput(factor.getSid(), token),
        factor1 -> Unit.INSTANCE, handleError);
  }
}
