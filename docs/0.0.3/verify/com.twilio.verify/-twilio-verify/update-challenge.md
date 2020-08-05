---
title: updateChallenge -
---
//[verify](../../index.md)/[com.twilio.verify](../index.md)/[TwilioVerify](index.md)/[updateChallenge](update-challenge.md)



# updateChallenge  
[androidJvm]  
Brief description  
Updates a **Challenge** from a **UpdateChallengePayload**  
  


## Parameters  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| error| : Block to be called when the operation fails with the cause of failure
| success| : Block to be called when the operation succeeds
| updateChallengePayload| : Describes the information needed to update a challenge
  
  
Content  
abstract fun [updateChallenge](update-challenge.md)(updateChallengePayload: [UpdateChallengePayload](../../com.twilio.verify.models/-update-challenge-payload/index.md), success: () -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), error: ([TwilioVerifyException](../-twilio-verify-exception/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))  



