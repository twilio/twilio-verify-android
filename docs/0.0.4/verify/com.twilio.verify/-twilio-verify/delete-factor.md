//[verify](../../index.md)/[com.twilio.verify](../index.md)/[TwilioVerify](index.md)/[deleteFactor](delete-factor.md)



# deleteFactor  
[androidJvm]  
Brief description  
Deletes a **Factor** with the given **id**  
  


## Parameters  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| error| : Block to be called when the operation fails with the cause of failure
| factorSid| : Id of the **Factor** to be deleted
| success| : Block to be called when the operation succeeds
  
  
Content  
abstract fun [deleteFactor](delete-factor.md)(factorSid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), success: () -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), error: ([TwilioVerifyException](../-twilio-verify-exception/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))  



