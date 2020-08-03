import org.gradle.api.Project

/*
 * Copyright (c) 2020, Twilio Inc.
 */

object UploadConstants {
    object Maven {
        const val plugin = "maven"
        const val repo = "maven.repo"
        const val username = "maven.username"
        const val password = "maven.password"
    }

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
            password: String
        ): Map<String, String> {
            return mapOf(
                Maven.repo to repositoryURL,
                Maven.username to projectProperty(project, user),
                Maven.password to projectProperty(project, password)
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
}