//pluginManagement {
//    repositories {
//        jcenter()
//        mavenCentral()
//        maven("https://dl.bintray.com/kotlin/kotlin-eap")
//        maven("https://dl.bintray.com/kotlin/kotlin-dev")
//    }
//}
pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}
rootProject.name = "TwilioVerify"
include(Modules.sample, Modules.verify, Modules.security)
