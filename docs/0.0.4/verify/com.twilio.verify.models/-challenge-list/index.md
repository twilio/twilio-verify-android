//[verify](../../index.md)/[com.twilio.verify.models](../index.md)/[ChallengeList](index.md)



# ChallengeList  
 [androidJvm] Describes the information of a **ChallengeList**  
  
interface [ChallengeList](index.md)   


## Functions  
  
|  Name|  Summary| 
|---|---|
| [equals](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/equals.html)| [androidJvm]  <br>Content  <br>open operator override fun [equals](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/equals.html)(other: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)  <br><br><br>
| [hashCode](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/hash-code.html)| [androidJvm]  <br>Content  <br>open override fun [hashCode](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/hash-code.html)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)  <br><br><br>
| [toString](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/to-string.html)| [androidJvm]  <br>Content  <br>open override fun [toString](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/to-string.html)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)  <br><br><br>


## Properties  
  
|  Name|  Summary| 
|---|---|
| [challenges](index.md#com.twilio.verify.models/ChallengeList/challenges/#/PointingToDeclaration/)|  [androidJvm] List of Challenges that matches the parameters of the **ChallengeListPayload** usedabstract val [challenges](index.md#com.twilio.verify.models/ChallengeList/challenges/#/PointingToDeclaration/): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<[Challenge](../-challenge/index.md)>   <br>
| [metadata](index.md#com.twilio.verify.models/ChallengeList/metadata/#/PointingToDeclaration/)|  [androidJvm] Metadata returned by the /Challenges endpoint, used to fetch subsequent pages of Challengesabstract val [metadata](index.md#com.twilio.verify.models/ChallengeList/metadata/#/PointingToDeclaration/): [Metadata](../-metadata/index.md)   <br>


## Inheritors  
  
|  Name| 
|---|
| [FactorChallengeList](../-factor-challenge-list/index.md)

