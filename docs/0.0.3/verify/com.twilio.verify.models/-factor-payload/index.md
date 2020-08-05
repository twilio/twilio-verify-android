---
title: FactorPayload -
---
//[verify](../../index.md)/[com.twilio.verify.models](../index.md)/[FactorPayload](index.md)



# FactorPayload  
 [androidJvm] Describes the information required to create a Factor  
  
interface [FactorPayload](index.md)   


## Functions  
  
|  Name|  Summary| 
|---|---|
| [equals](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/equals.html)| [androidJvm]  <br>Content  <br>open operator override fun [equals](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/equals.html)(other: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)  <br><br><br>
| [hashCode](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/hash-code.html)| [androidJvm]  <br>Content  <br>open override fun [hashCode](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/hash-code.html)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)  <br><br><br>
| [toString](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/to-string.html)| [androidJvm]  <br>Content  <br>open override fun [toString](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/to-string.html)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)  <br><br><br>


## Properties  
  
|  Name|  Summary| 
|---|---|
| [factorType](index.md#com.twilio.verify.models/FactorPayload/factorType/#/PointingToDeclaration/)|  [androidJvm] : Type of the factorabstract val [factorType](index.md#com.twilio.verify.models/FactorPayload/factorType/#/PointingToDeclaration/): [FactorType](../-factor-type/index.md)   <br>
| [friendlyName](index.md#com.twilio.verify.models/FactorPayload/friendlyName/#/PointingToDeclaration/)|  [androidJvm] : Friendl name of the factor, you can use this for display purposesabstract val [friendlyName](index.md#com.twilio.verify.models/FactorPayload/friendlyName/#/PointingToDeclaration/): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br>
| [identity](index.md#com.twilio.verify.models/FactorPayload/identity/#/PointingToDeclaration/)|  [androidJvm] : Identifies the user, should be an UUID you should not use PII (Personal Identifiable Information) because the systems that will process this attribute assume it is not directly identifying information.abstract val [identity](index.md#com.twilio.verify.models/FactorPayload/identity/#/PointingToDeclaration/): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br>
| [serviceSid](index.md#com.twilio.verify.models/FactorPayload/serviceSid/#/PointingToDeclaration/)|  [androidJvm] : Service idabstract val [serviceSid](index.md#com.twilio.verify.models/FactorPayload/serviceSid/#/PointingToDeclaration/): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br>


## Inheritors  
  
|  Name| 
|---|
| [PushFactorPayload](../-push-factor-payload/index.md)

