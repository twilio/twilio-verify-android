---
title: getAllChallenges -
---
//[verify](../../index.md)/[com.twilio.verify](../index.md)/[TwilioVerify](index.md)/[getAllChallenges](get-all-challenges.md)



# getAllChallenges  
[androidJvm]  
Brief description  
Gets all **Challenges** associated to a **Factor** with the given **ChallengeListPayload**  
  


## Parameters  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| challengeListPayload| : Describes the information needed to fetch all the **Challenges**
| error| : Block to be called when the operation fails with the cause of failure
| success| : Block to be called when the operation succeeds, returns a ChallengeList which contains the Challenges and the metadata associated to the request
  
  
Content  
abstract fun [getAllChallenges](get-all-challenges.md)(challengeListPayload: [ChallengeListPayload](../../com.twilio.verify.models/-challenge-list-payload/index.md), success: ([ChallengeList](../../com.twilio.verify.models/-challenge-list/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), error: ([TwilioVerifyException](../-twilio-verify-exception/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))  



