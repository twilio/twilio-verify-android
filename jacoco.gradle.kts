tasks.withType<Test> {
  configure<JacocoTaskExtension> {
    isIncludeNoLocationClasses = true
  }
}

private val classDirectoriesTree = fileTree("${project.buildDir}") {
  include(
      "**/classes/**/main/**",
      "**/intermediates/classes/debug/**",
      "**/intermediates/javac/debug/*/classes/**", // Android Gradle Plugin 3.2.x support.
      "**/tmp/kotlin-classes/debug/**"
  )

  exclude(
      "**/R.class",
      "**/R\$*.class",
      "**/BuildConfig.*",
      "**/Manifest*.*",
      "**/*Test*.*",
      "android/**/*.*",
      "**/models/**"
  )
}

private val sourceDirectoriesTree = files("$projectDir/src/main/java")

private val executionDataTree = fileTree("${project.buildDir}")
{
  include(
      "outputs/code_coverage/**/*.ec",
      "jacoco/jacocoTestReportDebug.exec",
      "jacoco/testDebugUnitTest.exec",
      "jacoco/test.exec"
  )
}

fun JacocoReportsContainer.reports() {
  xml.isEnabled = false
  html.isEnabled = true
  csv.isEnabled = false
  html.apply {
    isEnabled = true
    destination = file("${buildDir}/reports/jacoco/jacocoTestReport/html")
  }
}

fun JacocoCoverageVerification.setDirectories() {
  sourceDirectories.setFrom(sourceDirectoriesTree)
  classDirectories.setFrom(classDirectoriesTree)
  executionData.setFrom(executionDataTree)
}

fun JacocoReport.setDirectories() {
  sourceDirectories.setFrom(sourceDirectoriesTree)
  classDirectories.setFrom(classDirectoriesTree)
  executionData.setFrom(executionDataTree)
}

val jacocoGroup = "verification"
tasks.register<JacocoReport>("jacocoAndroidTestReport") {
  group = jacocoGroup
  description = "Code coverage report for both Android and Unit tests."
  dependsOn("testDebugUnitTest", "createDebugCoverageReport")
  reports {
    reports()
  }
  setDirectories()
}

tasks.register<JacocoCoverageVerification>("jacocoAndroidCoverageVerification") {
  group = jacocoGroup
  description = "Code coverage verification for Android both Android and Unit tests."
  dependsOn("testDebugUnitTest", "createDebugCoverageReport")
  violationRules {
    rule {
      limit {
        minimum = "0.8".toBigDecimal()
      }
    }
    rule {
      element = "CLASS"
      limit {
        minimum = "0.75".toBigDecimal()
      }
    }
  }
  setDirectories()
}