---
title: updateFactor -
---
//[verify](../../index.md)/[com.twilio.verify](../index.md)/[TwilioVerify](index.md)/[updateFactor](update-factor.md)



# updateFactor  
[androidJvm]  
Brief description  
Updates a **Factor** from a **FactorPayload**  
  


## Parameters  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| error| : Block to be called when the operation fails with the cause of failure
| success| : Block to be called when the operation succeeds, returns the updated Factor
| updateFactorPayload| : Describes the information needed to update a factor
  
  
Content  
abstract fun [updateFactor](update-factor.md)(updateFactorPayload: [UpdateFactorPayload](../../com.twilio.verify.models/-update-factor-payload/index.md), success: ([Factor](../../com.twilio.verify.models/-factor/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), error: ([TwilioVerifyException](../-twilio-verify-exception/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))  



