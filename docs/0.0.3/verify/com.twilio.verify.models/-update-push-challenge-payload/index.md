---
title: UpdatePushChallengePayload -
---
//[verify](../../index.md)/[com.twilio.verify.models](../index.md)/[UpdatePushChallengePayload](index.md)



# UpdatePushChallengePayload  
 [androidJvm] Describes the information required to update a **Push Challenge**  
  
class [UpdatePushChallengePayload](index.md)(**factorSid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html),**challengeSid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html),**status**: [ChallengeStatus](../-challenge-status/index.md)) : [UpdateChallengePayload](../-update-challenge-payload/index.md)   


## Constructors  
  
|  Name|  Summary| 
|---|---|
| [<init>](-init-.md)|  [androidJvm] fun [<init>](-init-.md)(factorSid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), challengeSid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), status: [ChallengeStatus](../-challenge-status/index.md))   <br>


## Functions  
  
|  Name|  Summary| 
|---|---|
| [equals](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/equals.html)| [androidJvm]  <br>Content  <br>open operator override fun [equals](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/equals.html)(other: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)  <br><br><br>
| [hashCode](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/hash-code.html)| [androidJvm]  <br>Content  <br>open override fun [hashCode](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/hash-code.html)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)  <br><br><br>
| [toString](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/to-string.html)| [androidJvm]  <br>Content  <br>open override fun [toString](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/to-string.html)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)  <br><br><br>


## Properties  
  
|  Name|  Summary| 
|---|---|
| [challengeSid](index.md#com.twilio.verify.models/UpdatePushChallengePayload/challengeSid/#/PointingToDeclaration/)|  [androidJvm] Id of the Challenge to be updatedopen override val [challengeSid](index.md#com.twilio.verify.models/UpdatePushChallengePayload/challengeSid/#/PointingToDeclaration/): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br>
| [factorSid](index.md#com.twilio.verify.models/UpdatePushChallengePayload/factorSid/#/PointingToDeclaration/)|  [androidJvm] Id of the Factor to which the Challenge is relatedopen override val [factorSid](index.md#com.twilio.verify.models/UpdatePushChallengePayload/factorSid/#/PointingToDeclaration/): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br>
| [status](index.md#com.twilio.verify.models/UpdatePushChallengePayload/status/#/PointingToDeclaration/)|  [androidJvm] Id of the Challenge to be updatedval [status](index.md#com.twilio.verify.models/UpdatePushChallengePayload/status/#/PointingToDeclaration/): [ChallengeStatus](../-challenge-status/index.md)   <br>

