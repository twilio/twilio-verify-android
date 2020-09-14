// Top-level build file where you can add configuration options common to all sub-projects/modules.
apply(from = "publish.gradle.kts")
plugins {
  id(Config.Plugins.dokka) version Config.Versions.dokka
}

buildscript {
  repositories {
    jcenter()
    google()
    maven { url = uri("https://twilio.bintray.com/releases") }
    maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
  }
  dependencies {
    classpath(Config.Dependencies.androidTools)
    classpath(kotlin(Config.Dependencies.kotlin, Config.Versions.kotlin))
    classpath(Config.Dependencies.googleServices)
    classpath(Config.Dependencies.firebasePerformance)
    classpath(Config.Dependencies.versionBumper)
    classpath(Config.Dependencies.jacoco)
    classpath(Config.Dependencies.apkscale)
    // NOTE: Do not place your application dependencies here; they belong
    // in the individual module build.gradle files
  }
}

allprojects {
  repositories {
    google()
    jcenter()
    maven {
      url = uri("https://twilio.bintray.com/internal-releases")
      credentials {
        username = Config.projectProperty(project, "BINTRAY_USER")
        password = Config.projectProperty(project, "BINTRAY_APIKEY")
      }
    }
  }
  plugins.apply(Config.Plugins.spotless)
  plugins.apply(Config.Plugins.ktlint)
  plugins.apply(Config.Plugins.gitHooks)
}

task("linter") {
  group = "Reporting"
  dependsOn(
    ":verify:ktlint", ":security:ktlint", ":sample:ktlint",
    ":verify:lint", ":security:lint", ":sample:lint"
  )
}
