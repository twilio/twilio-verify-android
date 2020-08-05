---
title: createFactor -
---
//[verify](../../index.md)/[com.twilio.verify](../index.md)/[TwilioVerify](index.md)/[createFactor](create-factor.md)



# createFactor  
[androidJvm]  
Brief description  
Creates a **Factor** from a **FactorPayload**  
  


## Parameters  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| error| : Block to be called when the operation fails with the cause of failure
| factorPayload| : Describes Information needed to create a Factor
| success| : Block to be called when the operation succeeds, returns the created Factor
  
  
Content  
abstract fun [createFactor](create-factor.md)(factorPayload: [FactorPayload](../../com.twilio.verify.models/-factor-payload/index.md), success: ([Factor](../../com.twilio.verify.models/-factor/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), error: ([TwilioVerifyException](../-twilio-verify-exception/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))  



