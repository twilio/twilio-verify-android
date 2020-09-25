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

val verifyVersionName: String by extra
val verifyVersionCode: String by extra

task("incrementVersion") {

  doLast {
    val versionCode = verifyVersionCode.toInt().plus(1)
    var versionName = verifyVersionName
    if (project.hasProperty("version_number")) {
      versionName = project.property("version_number") as String
    }
    ant.withGroovyBuilder {
      "propertyfile"("file" to "gradle.properties") {
        "entry"("key" to "verifyVersionName", "value" to versionName)
        "entry"("key" to "verifyVersionCode", "value" to versionCode)
      }
    }
  }
}
