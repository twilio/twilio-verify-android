//[verify](../../index.md)/[com.twilio.verify.models](../index.md)/[Factor](index.md)



# Factor  
 [androidJvm] Describes the information of a **Factor**  
  
interface [Factor](index.md)   


## Functions  
  
|  Name|  Summary| 
|---|---|
| [equals](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/equals.html)| [androidJvm]  <br>Content  <br>open operator override fun [equals](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/equals.html)(other: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)  <br><br><br>
| [hashCode](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/hash-code.html)| [androidJvm]  <br>Content  <br>open override fun [hashCode](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/hash-code.html)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)  <br><br><br>
| [toString](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/to-string.html)| [androidJvm]  <br>Content  <br>open override fun [toString](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/to-string.html)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)  <br><br><br>


## Properties  
  
|  Name|  Summary| 
|---|---|
| [accountSid](index.md#com.twilio.verify.models/Factor/accountSid/#/PointingToDeclaration/)|  [androidJvm] Id of the account to which the Factor is relatedabstract val [accountSid](index.md#com.twilio.verify.models/Factor/accountSid/#/PointingToDeclaration/): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br>
| [createdAt](index.md#com.twilio.verify.models/Factor/createdAt/#/PointingToDeclaration/)|  [androidJvm] Indicates the creation date of the Factorabstract val [createdAt](index.md#com.twilio.verify.models/Factor/createdAt/#/PointingToDeclaration/): [Date](https://developer.android.com/reference/java/util/Date.html)   <br>
| [friendlyName](index.md#com.twilio.verify.models/Factor/friendlyName/#/PointingToDeclaration/)|  [androidJvm] Friendly name of the factor, can be used for display purposesabstract val [friendlyName](index.md#com.twilio.verify.models/Factor/friendlyName/#/PointingToDeclaration/): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br>
| [identity](index.md#com.twilio.verify.models/Factor/identity/#/PointingToDeclaration/)|  [androidJvm] Identifies the user, should be an UUID you should not use PII (Personal Identifiable Information) because the systems that will process this attribute assume it is not directly identifying information.abstract val [identity](index.md#com.twilio.verify.models/Factor/identity/#/PointingToDeclaration/): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br>
| [serviceSid](index.md#com.twilio.verify.models/Factor/serviceSid/#/PointingToDeclaration/)|  [androidJvm] Id of the service to which the Factor is relatedabstract val [serviceSid](index.md#com.twilio.verify.models/Factor/serviceSid/#/PointingToDeclaration/): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br>
| [sid](index.md#com.twilio.verify.models/Factor/sid/#/PointingToDeclaration/)|  [androidJvm] Id of the Factorabstract val [sid](index.md#com.twilio.verify.models/Factor/sid/#/PointingToDeclaration/): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br>
| [status](index.md#com.twilio.verify.models/Factor/status/#/PointingToDeclaration/)|  [androidJvm] Status of the Factorabstract var [status](index.md#com.twilio.verify.models/Factor/status/#/PointingToDeclaration/): [FactorStatus](../-factor-status/index.md)   <br>
| [type](index.md#com.twilio.verify.models/Factor/type/#/PointingToDeclaration/)|  [androidJvm] Type of the Factorabstract val [type](index.md#com.twilio.verify.models/Factor/type/#/PointingToDeclaration/): [FactorType](../-factor-type/index.md)   <br>

