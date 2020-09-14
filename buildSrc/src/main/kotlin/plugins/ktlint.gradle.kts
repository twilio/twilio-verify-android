package plugins

val ktlint: Configuration by configurations.creating

dependencies {
  ktlint("com.pinterest:ktlint:0.37.2")
}

tasks {
  register<JavaExec>("ktlint") {
    group = "verification"
    description = "Check Kotlin code style."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args = listOf(
      "--reporter=plain", "--reporter=checkstyle,output=$buildDir/reports/ktlint/ktlint.xml",
      "--android", "src/**/*.kt", "**/*.gradle.kts"
    )
  }

  register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args("--android", "-F", "src/**/*.kt")
  }
}
