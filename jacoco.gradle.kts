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

tasks.withType<Test> {
  configure<JacocoTaskExtension> {
    isIncludeNoLocationClasses = true
    excludes = listOf("jdk.internal.*")
  }
}

private val classDirectoriesTree = fileTree(project.layout.buildDirectory.asFile) {
  include(
    "**/classes/**/main/**",
    "**/intermediates/classes/debug/**",
    "**/intermediates/javac/debug/*/classes/**", // Android Gradle Plugin 3.2.x support.
    "**/tmp/kotlin-classes/debug/**"
  )
  exclude(
    "**/R.class",
    "**/R\$*.class",
    "**/*\$1*",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "android/**/*.*",
    "**/models/**",
    "**/*\$Lambda$*.*",
    "**/*\$inlined$*.*"
  )
}

private val sourceDirectoriesTree = project.layout.projectDirectory.dir("src/main/java").asFile

private val executionDataTree = fileTree(project.layout.buildDirectory.asFile) {
  include(
    "outputs/code_coverage/**/*.ec",
    "jacoco/jacocoTestReportDebug.exec",
    "jacoco/testDebugUnitTest.exec",
    "jacoco/test.exec"
  )
}

fun JacocoReportsContainer.reports() {
  csv.required.set(false)
  xml.required.set(true)
  html.required.set(true)
  html.outputLocation.set(layout.buildDirectory.dir("reports/code-coverage/html"))
  xml.outputLocation.set(layout.buildDirectory.file("reports/code-coverage/xml"))
}

fun JacocoReport.setDirectories() {
  sourceDirectories.setFrom(sourceDirectoriesTree)
  classDirectories.setFrom(classDirectoriesTree)
  executionData.setFrom(executionDataTree)
}

fun JacocoCoverageVerification.setDirectories() {
  sourceDirectories.setFrom(sourceDirectoriesTree)
  classDirectories.setFrom(classDirectoriesTree)
  executionData.setFrom(executionDataTree)
}

val jacocoGroup = "verification"
tasks.register<JacocoReport>("jacocoTestReport") {
  group = jacocoGroup
  description = "Code coverage report for both Android and Unit tests."
  dependsOn("testDebugUnitTest")
  reports {
    reports()
  }
  setDirectories()
}

val minimumCoverage = "0.8".toBigDecimal()
tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
  group = jacocoGroup
  description = "Code coverage verification for Android both Android and Unit tests."
  dependsOn("testDebugUnitTest")
  violationRules {
    rule {
      limit {
        minimum = minimumCoverage
      }
    }
    rule {
      element = "CLASS"
      excludes = listOf(
        "**.FactorFacade.Builder",
        "**.ServiceFacade.Builder",
        "**.ChallengeFacade.Builder",
        "**.Task"
      )
      limit {
        minimum = minimumCoverage
      }
    }
  }
  setDirectories()
}
