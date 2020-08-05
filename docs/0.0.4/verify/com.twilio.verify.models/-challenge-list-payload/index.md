//[verify](../../index.md)/[com.twilio.verify.models](../index.md)/[ChallengeListPayload](index.md)



# ChallengeListPayload  
 [androidJvm] escribes the information required to fetch a **ChallengeList**  
  
class [ChallengeListPayload](index.md)(**factorSid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html),**pageSize**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html),**status**: [ChallengeStatus](../-challenge-status/index.md)?,**pageToken**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?)   


## Constructors  
  
|  Name|  Summary| 
|---|---|
| [<init>](-init-.md)|  [androidJvm] fun [<init>](-init-.md)(factorSid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), pageSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), status: [ChallengeStatus](../-challenge-status/index.md)?, pageToken: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?)   <br>


## Functions  
  
|  Name|  Summary| 
|---|---|
| [equals](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/equals.html)| [androidJvm]  <br>Content  <br>open operator override fun [equals](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/equals.html)(other: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)  <br><br><br>
| [hashCode](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/hash-code.html)| [androidJvm]  <br>Content  <br>open override fun [hashCode](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/hash-code.html)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)  <br><br><br>
| [toString](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/to-string.html)| [androidJvm]  <br>Content  <br>open override fun [toString](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/to-string.html)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)  <br><br><br>


## Properties  
  
|  Name|  Summary| 
|---|---|
| [factorSid](index.md#com.twilio.verify.models/ChallengeListPayload/factorSid/#/PointingToDeclaration/)|  [androidJvm] Id of the factor to which the Challenge is relatedval [factorSid](index.md#com.twilio.verify.models/ChallengeListPayload/factorSid/#/PointingToDeclaration/): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br>
| [pageSize](index.md#com.twilio.verify.models/ChallengeListPayload/pageSize/#/PointingToDeclaration/)|  [androidJvm] Number of Challenges to be returned by the serviceval [pageSize](index.md#com.twilio.verify.models/ChallengeListPayload/pageSize/#/PointingToDeclaration/): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)   <br>
| [pageToken](index.md#com.twilio.verify.models/ChallengeListPayload/pageToken/#/PointingToDeclaration/)|  [androidJvm] val [pageToken](index.md#com.twilio.verify.models/ChallengeListPayload/pageToken/#/PointingToDeclaration/): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?   <br>
| [status](index.md#com.twilio.verify.models/ChallengeListPayload/status/#/PointingToDeclaration/)|  [androidJvm] Status to filter the Challenges, if nothing is sent, Challenges of all status will be returnedval [status](index.md#com.twilio.verify.models/ChallengeListPayload/status/#/PointingToDeclaration/): [ChallengeStatus](../-challenge-status/index.md)?   <br>

