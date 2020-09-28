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

    const val plugin = "maven"
    const val repo = "maven.repo"
    const val mavenUsername = "maven.username"
    const val mavenPassword = "maven.password"

    object Bintray {
        const val repository = "repository"
        const val url = "url"
        const val authentication = "authentication"
        const val userName = "userName"
        const val password = "password"
        const val version = "version"
        const val groupId = "groupId"
        const val artifactId = "artifactId"
        const val packaging = "packaging"
        const val user = "BINTRAY_USER"
        const val apiKey = "BINTRAY_APIKEY"
        const val group = "Publishing"

        fun credentials(
          project: Project,
          repositoryURL: String,
          user: String,
          pass: String
        ): Map<String, String> {
            return mapOf(
                repo to repositoryURL,
                mavenUsername to projectProperty(project, user),
                mavenPassword to projectProperty(project, pass)
            )
        }

        private fun projectProperty(
          project: Project,
          property: String
        ): String {
            val value =
                if (project.hasProperty(property)) project.property(property) as? String else System.getenv(
                    property
                )
            return value ?: ""
        }
    }

    fun mavenRepo(project: Project) =
        if (project.hasProperty(repo)) project.property(repo) as String else ""

    fun mavenUsername(project: Project) =
        if (project.hasProperty(mavenUsername)) project.property(mavenUsername) as String else ""

    fun mavenPassword(project: Project) =
        if (project.hasProperty(mavenPassword)) project.property(mavenPassword) as String else ""
}
