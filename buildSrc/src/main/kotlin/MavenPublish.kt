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

import org.gradle.api.Project

object MavenPublish {
  const val sonatypeUsernameKey = "sonatypeUsername"
  const val sonatypePasswordKey = "sonatypePassword"
  const val sonatypeStagingProfileIdKey = "sonatypeStagingProfileId"
  const val sonatypeUsernameEnv = "SONATYPE_USERNAME"
  const val sonatypePasswordEnv = "SONATYPE_PASSWORD"
  const val sonatypeStagingProfileIdEnv = "SONATYPE_STAGING_PROFILE_ID"

  fun credentials(
    project: Project,
    sonatypeUsername: String,
    sonatypePassword: String,
    sonatypeStagingProfileId: String
  ): Map<String, String> {
    return mapOf(
      sonatypeUsernameKey to Config.projectProperty(project, sonatypeUsername),
      sonatypePasswordKey to Config.projectProperty(project, sonatypePassword),
      sonatypeStagingProfileIdKey to Config.projectProperty(project, sonatypeStagingProfileId)
    )
  }
}
