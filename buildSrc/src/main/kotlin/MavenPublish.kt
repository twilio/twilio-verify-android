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

  const val signingKeyIdKey = "signing.keyId"
  const val signingPasswordKey = "signing.password"
  const val signingSecretKeyRingFileKey = "signing.secretKeyRingFile"
  const val ossrhUsernameKey = "ossrhUsername"
  const val ossrhPasswordKey = "ossrhPassword"
  const val sonatypeStagingProfileIdKey = "sonatypeStagingProfileId"

  const val signingKeyIdEnv = "SIGNING_KEY_ID"
  const val signingPasswordEnv = "SIGNING_PASSWORD"
  const val signingSecretKeyRingFileEnv = "SIGNING_SECRET_KEY_RING_FILE"
  const val ossrhUsernameEnv = "OSSRH_USERNAME"
  const val ossrhPasswordEnv = "OSSRH_PASSWORD"
  const val sonatypeStagingProfileIdEnv = "SONATYPE_STAGING_PROFILE_ID"

  fun credentials(
    project: Project,
    signingKeyId: String,
    signingPassword: String,
    signingKeyRingFile: String,
    ossrhUsername: String,
    ossrhPassword: String,
    sonatypeStagingProfileId: String
  ): Map<String, String> {
    return mapOf(
      signingKeyIdKey to Config.projectProperty(project, signingKeyId),
      signingPasswordKey to Config.projectProperty(project, signingPassword),
      signingSecretKeyRingFileKey to Config.projectProperty(project, signingKeyRingFile),
      ossrhUsernameKey to Config.projectProperty(project, ossrhUsername),
      ossrhPasswordKey to Config.projectProperty(project, ossrhPassword),
      sonatypeStagingProfileIdKey to Config.projectProperty(project, sonatypeStagingProfileId)
    )
  }
}
