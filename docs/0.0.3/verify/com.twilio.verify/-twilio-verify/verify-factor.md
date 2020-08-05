---
title: verifyFactor -
---
//[verify](../../index.md)/[com.twilio.verify](../index.md)/[TwilioVerify](index.md)/[verifyFactor](verify-factor.md)



# verifyFactor  
[androidJvm]  
Brief description  
Verifies a **Factor** from a **VerifyFactorPayload**  
  


## Parameters  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| error| : Block to be called when the operation fails with the cause of failure
| factorPayload| : Describes the information needed to verify a factor
| success| : Block to be called when the operation succeeds, returns the verified Factor
  
  
Content  
abstract fun [verifyFactor](verify-factor.md)(verifyFactorPayload: [VerifyFactorPayload](../../com.twilio.verify.models/-verify-factor-payload/index.md), success: ([Factor](../../com.twilio.verify.models/-factor/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), error: ([TwilioVerifyException](../-twilio-verify-exception/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))  



