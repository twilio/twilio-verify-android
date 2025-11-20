/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//region Plugins
apply(from = "../jacoco.gradle.kts")
apply(from = "version.gradle.kts")
plugins {
  id(Config.Plugins.androidLibrary)
  id(Config.Plugins.kotlinAndroid)
  id(Config.Plugins.dokka)
  id(Config.Plugins.maven_publish)
  id(Config.Plugins.signing)
  jacoco
  id(Config.Plugins.apkscale)
}
//endregion

val verifyVersionName: String by extra
val verifyVersionCode: String by extra
val baseURL: String by extra
//region Android
android {
  namespace = "com.twilio.verify"
  compileSdk = Config.Versions.compileSDKVersion

  buildFeatures {
    buildConfig = true
  }

  defaultConfig {
    minSdk = Config.Versions.minSDKVersion
    targetSdk = Config.Versions.targetSDKVersion

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
    buildConfigField("String", "BASE_URL", baseURL)
    buildConfigField("String", "VERSION_NAME", "\"$verifyVersionName\"")
    buildConfigField("String", "VERSION_CODE", "\"$verifyVersionCode\"")
    version = verifyVersionName
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }
  testOptions {
    unitTests.isIncludeAndroidResources = true
    unitTests.isReturnDefaultValues = true
  }
  lint {
    lintConfig = rootProject.file(".lint/config.xml")
    xmlReport = true
    checkAllWarnings = true
  }
}
//endregion

//region KDoc
tasks.dokkaHtml {
  dokkaSourceSets {
    configureEach {
      includeNonPublic = false
      reportUndocumented = true
      skipEmptyPackages = true
    }
  }

  doLast {
    ant.withGroovyBuilder {
      "copy"(
        "file" to "index.html",
        "todir" to "../docs/$verifyVersionName"
      )
    }
  }
}
//endregion

//region Publish
val pomGroup: String by project
val pomArtifactId: String by project

val dokkaHtmlJar by tasks.creating(Jar::class) {
  dependsOn(tasks.dokkaHtml)
  from(
    tasks.dokkaHtml.get()
  )
  archiveClassifier.set("html-doc")
}

val sourcesJar by tasks.creating(Jar::class) {
  archiveClassifier.set("sources")
  from(android.sourceSets.getByName("main").java.srcDirs)
}

publishing {
  publications {
    create<MavenPublication>("TwilioVerify") {
      groupId = pomGroup
      artifactId = pomArtifactId
      version = verifyVersionName
      afterEvaluate {
        from(components["release"])
      }
      artifact(dokkaHtmlJar)

      // Make sure generateMetadataFileForTwilioVerifyPublication depends on sourcesJar
      tasks.named("generateMetadataFileForTwilioVerifyPublication") {
        dependsOn(sourcesJar)
      }

      pom {
        name.set("twilio-verify-android")
        description.set(
          "Twilio Verify Push SDK helps you verify users by adding a low-friction, secure, " +
            "cost-effective, \"push verification\" factor into your own mobile application. This fully" +
            " managed API service allows you to seamlessly verify users in-app via a secure channel," +
            " without the risks, hassles or costs of One-Time Passcodes (OTPs)."
        )
        url.set("https://github.com/twilio/twilio-verify-android")
        licenses {
          license {
            name.set("Apache License, Version 2.0")
            url.set("https://github.com/twilio/twilio-verify-android/blob/main/LICENSE")
          }
        }
        developers {
          developer {
            id.set("Twilio")
            name.set("Twilio")
          }
        }
        scm {
          connection.set("scm:git:github.com/twilio/twilio-verify-android.git")
          developerConnection.set("scm:git:ssh://github.com/twilio/twilio-verify-android.git")
          url.set("https://github.com/twilio/twilio-verify-android/tree/main")
        }
      }
    }
  }
}

signing {
  sign(publishing.publications)
}
//endregion

apkscale {
  abis = setOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
}

@Suppress("UNCHECKED_CAST")
task("generateSizeReport") {
  dependsOn("assembleRelease", "measureSize")
  description = "Calculate Verify SDK Size Impact"
  group = "Reporting"

  doLast {
    var sizeReport =
      "### Size impact\n" +
        "\n" +
        "| ABI             | APK Size Impact |\n" +
        "| --------------- | --------------- |\n"
    val apkscaleOutputFile = file("${layout.buildDirectory.get().asFile}/apkscale/build/outputs/reports/apkscale.json")
    val jsonSlurper = groovy.json.JsonSlurper()
    val apkscaleOutput = jsonSlurper.parseText(apkscaleOutputFile.readText()) as List<*>
    val releaseOutput = apkscaleOutput[0] as Map<*, *>
    val sizes = releaseOutput["size"] as? Map<String, String>
    sizes?.forEach { (arch, sizeImpact) ->
      sizeReport += "| ${arch.padEnd(16)}| ${sizeImpact.padEnd(16)}|\n"
    }
    val sizeReportDir = "${layout.buildDirectory.get().asFile}/outputs/sizeReport"
    mkdir(sizeReportDir)
    val targetFile = file("$sizeReportDir/${rootProject.name.replaceFirstChar { it.titlecase() }}SizeImpactReport.txt")
    targetFile.createNewFile()
    targetFile.writeText(sizeReport)
  }
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  debugImplementation(project(":${Modules.security}"))
  releaseImplementation("com.twilio:twilio-security-android:0.2.0")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.1.10")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
  androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.3.1")
  androidTestImplementation("com.squareup.okhttp3:okhttp-tls:4.3.1")
  androidTestImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
  testImplementation("junit:junit:4.13.2")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
  testImplementation("org.robolectric:robolectric:4.14.1")
  testImplementation("androidx.test:core:1.6.1")
  testImplementation("org.hamcrest:hamcrest-library:2.2")
  testImplementation("org.mockito:mockito-inline:3.11.2")
  testImplementation("io.mockk:mockk:1.13.16")
}
