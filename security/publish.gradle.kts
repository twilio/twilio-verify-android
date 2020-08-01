/*
 * Copyright (c) 2020, Twilio Inc.
 */

apply(plugin = "maven")
apply(from = "version.gradle.kts")

val mavenRepo = if (project.hasProperty("maven.repo")) project.property("maven.repo") else ""
val mavenUsername = if (project.hasProperty("maven.username"))
  project.property("maven.username") else ""
val mavenPassword = if (project.hasProperty("maven.password"))
  project.property("maven.password") else ""
val securityVersion: String by project
val pomPackaging: String by project
val pomGroup: String by project
val pomArtifactId: String by project
/*
 * Maven upload configuration that can be used for any maven repo
 */
tasks {
  "uploadArchives"(Upload::class) {
    repositories {
      withConvention(MavenRepositoryHandlerConvention::class) {
        mavenDeployer {
          withGroovyBuilder {
            "repository"("url" to uri(mavenRepo ?: "")) {
              "authentication"("userName" to mavenUsername, "password" to mavenPassword)
            }
          }
          pom.project {
            withGroovyBuilder {
              "version"(securityVersion)
              "groupId"(pomGroup)
              "artifactId"(pomArtifactId)
              "packaging"(pomPackaging)
            }
          }
        }
      }
    }
  }
}

tasks.register("bintrayLibraryReleaseCandidateUpload", GradleBuild::class) {
  description = "Publish Security SDK release candidate to internal bintray"
  group = "Publishing"
  buildFile = file("build.gradle.kts")
  tasks = listOf("assembleRelease", "uploadArchives")
  startParameter.projectProperties.plusAssign(
      gradle.startParameter.projectProperties + mapOf(
          "releaseCandidate" to "true",
          "maven.repo" to "https://api.bintray.com/maven/twilio/internal-releases/twilio-security-android/;publish=1",
          "maven.username" to projectProperty("BINTRAY_USER"),
          "maven.password" to projectProperty("BINTRAY_APIKEY")
      )
  )
}

tasks.register("bintrayLibraryReleaseUpload", GradleBuild::class) {
  description = "Publish Security SDK release to bintray"
  group = "Publishing"
  buildFile = file("build.gradle.kts")
  tasks = listOf("assembleRelease", "uploadArchives")
  startParameter.projectProperties.plusAssign(
      gradle.startParameter.projectProperties + mapOf(
          "maven.repo" to "https://api.bintray.com/maven/twilio/releases/twilio-security-android/;publish=1",
          "maven.username" to projectProperty("BINTRAY_USER"),
          "maven.password" to projectProperty("BINTRAY_APIKEY")
      )
  )
}


fun projectProperty(property: String): String {
  val value =
    if (project.hasProperty(property)) project.property(property) as? String else System.getenv(
        property
    )
  return value ?: ""
}
