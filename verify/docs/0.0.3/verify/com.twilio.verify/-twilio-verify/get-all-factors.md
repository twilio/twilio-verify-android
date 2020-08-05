---
title: getAllFactors -
---
//[verify](../../index.md)/[com.twilio.verify](../index.md)/[TwilioVerify](index.md)/[getAllFactors](get-all-factors.md)



# getAllFactors  
[androidJvm]  
Brief description  
Gets all **Factors** created by the app  
  


## Parameters  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| error| : Block to be called when the operation fails with the cause of failure
| success| : Block to be called when the operation succeeds, returns a List of Factor
  
  
Content  
abstract fun [getAllFactors](get-all-factors.md)(success: ([List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<[Factor](../../com.twilio.verify.models/-factor/index.md)>) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), error: ([TwilioVerifyException](../-twilio-verify-exception/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))  



