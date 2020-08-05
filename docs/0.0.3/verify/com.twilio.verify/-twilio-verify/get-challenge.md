---
title: getChallenge -
---
//[verify](../../index.md)/[com.twilio.verify](../index.md)/[TwilioVerify](index.md)/[getChallenge](get-challenge.md)



# getChallenge  
[androidJvm]  
Brief description  
Gets a **Challenge** with the given challenge id and factor id  
  


## Parameters  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| challengeSid| : Id of the Challenge requested
| error| : Block to be called when the operation fails with the cause of failure
| factorSid| : id of the Factor to which the Challenge corresponds
| success| : Block to be called when the operation succeeds, returns the requested Challenge
  
  
Content  
abstract fun [getChallenge](get-challenge.md)(challengeSid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), factorSid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), success: ([Challenge](../../com.twilio.verify.models/-challenge/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), error: ([TwilioVerifyException](../-twilio-verify-exception/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))  



