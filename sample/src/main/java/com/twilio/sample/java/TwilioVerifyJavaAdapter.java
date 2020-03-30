package com.twilio.sample.java;

import com.twilio.sample.TwilioVerifyAdapter;
import com.twilio.sample.model.CreateFactorData;
import com.twilio.sample.networking.SampleBackendAPIClient;
import com.twilio.sample.push.NewChallenge;
import com.twilio.sample.push.VerifiedFactor;
import com.twilio.sample.push.VerifyEvent;
import com.twilio.sample.push.VerifyEventBus;
import com.twilio.verify.TwilioVerify;
import com.twilio.verify.TwilioVerifyException;
import com.twilio.verify.models.Challenge;
import com.twilio.verify.models.Factor;
import com.twilio.verify.models.PushFactorInput;
import com.twilio.verify.models.UpdateChallengeInput;
import com.twilio.verify.models.VerifyFactorInput;
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
    sampleBackendAPIClient.getJwt(createFactorData.getJwtUrl(), createFactorData.getIdentity(),
        new Function1<String, Unit>() {
          @Override public Unit invoke(String jwt) {
            createFactor(createFactorData, jwt, onSuccess, onError);
            return Unit.INSTANCE;
          }
        }, onError);
  }

  @Override public void verifyFactor(@NotNull VerifyFactorInput verifyFactorInput) {
    twilioVerify.verifyFactor(verifyFactorInput, new Function1<Factor, Unit>() {
      @Override public Unit invoke(Factor factor) {
        VerifyEventBus.INSTANCE.send(new VerifiedFactor(factor));
        return Unit.INSTANCE;
      }
    }, handleError);
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

  private void createFactor(CreateFactorData createFactorData, String jwt,
      final Function1<? super Factor, Unit> onSuccess,
      final Function1<? super Exception, Unit> onError) {
    twilioVerify.createFactor(
        new PushFactorInput(createFactorData.getFactorName(),
            createFactorData.getPushToken(), jwt),
        new Function1<Factor, Unit>() {
          @Override public Unit invoke(Factor factor) {
            onSuccess.invoke(factor);
            waitForFactorVerified(factor, onSuccess);
            return Unit.INSTANCE;
          }
        }, onError);
  }

  private void waitForFactorVerified(final Factor factor,
      final Function1<? super Factor, Unit> onFactorVerified) {
    VerifyEventBus.INSTANCE.consumeEvent(new Function1<VerifyEvent, Unit>() {
      @Override public Unit invoke(VerifyEvent verifyEvent) {
        if (verifyEvent instanceof VerifiedFactor) {
          Factor updatedFactor = ((VerifiedFactor) verifyEvent).getFactor();
          if (updatedFactor.getSid().equals(factor.getSid())) {
            onFactorVerified.invoke(updatedFactor);
          }
        }
        return Unit.INSTANCE;
      }
    });
  }
}
