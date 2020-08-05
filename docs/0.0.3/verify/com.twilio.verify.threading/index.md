---
title: com.twilio.verify.threading -
---
//[verify](../index.md)/[com.twilio.verify.threading](index.md)



# Package com.twilio.verify.threading  


## Types  
  
|  Name|  Summary| 
|---|---|
| [SafeError]()| [androidJvm]  <br>Content  <br>typealias [SafeError]()<[E]()> = ([E]()) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)  <br><br><br>
| [SafeSuccess]()| [androidJvm]  <br>Content  <br>typealias [SafeSuccess]() = () -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)  <br><br><br>
| [SafeSuccessResult]()| [androidJvm]  <br>Content  <br>typealias [SafeSuccessResult]()<[T]()> = ([T]()) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)  <br><br><br>
| [Task](-task/index.md)| [androidJvm]  <br>Content  <br>class [Task](-task/index.md)<[T](-task/index.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?, [E](-task/index.md) : [Exception](https://developer.android.com/reference/java/lang/Exception.html)> (**block**: (([T](-task/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), ([E](-task/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html),**success**: ([T](-task/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html),**error**: ([E](-task/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html),**handler**: [Handler](https://developer.android.com/reference/android/os/Handler.html)?) : [Runnable](https://developer.android.com/reference/java/lang/Runnable.html)  <br><br><br>


## Functions  
  
|  Name|  Summary| 
|---|---|
| [execute](execute.md)| [androidJvm]  <br>Content  <br>fun <[T](execute.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?> [execute](execute.md)(success: ([T](execute.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), error: ([TwilioVerifyException](../com.twilio.verify/-twilio-verify-exception/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), block: (([T](execute.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), ([TwilioVerifyException](../com.twilio.verify/-twilio-verify-exception/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))  <br>fun [execute](execute.md)(success: () -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), error: ([TwilioVerifyException](../com.twilio.verify/-twilio-verify-exception/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), block: (() -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), ([TwilioVerifyException](../com.twilio.verify/-twilio-verify-exception/index.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))  <br><br><br>

