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
    isIgnoreExitValue = true
  }

  register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args("--android", "-F", "src/**/*.kt")
  }
}
