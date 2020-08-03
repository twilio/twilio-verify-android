/*
 * Copyright (c) 2020, Twilio Inc.
 */

apply(plugin = "maven")
apply(from = "../version.gradle.kts")

val mavenRepo = if (project.hasProperty("maven.repo")) project.property("maven.repo") else ""
val mavenUsername = if (project.hasProperty("maven.username"))
  project.property("maven.username") else ""
val mavenPassword = if (project.hasProperty("maven.password"))
  project.property("maven.password") else ""
val verifyVersionName: String by project
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
              "authentication"(
                  "userName" to mavenUsername,
                  "password" to mavenPassword
              )
            }
          }
          pom.project {
            withGroovyBuilder {
              "version"(verifyVersionName)
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

task("bintrayLibraryReleaseCandidateUpload", GradleBuild::class) {
  description = "Publish Verify SDK release candidate to internal bintray"
  group = "Publishing"
  buildFile = file("build.gradle.kts")
  tasks = listOf("assembleRelease", "uploadArchives")
  startParameter.projectProperties.plusAssign(
      gradle.startParameter.projectProperties + mapOf(
          "maven.repo" to "https://api.bintray.com/maven/twilio/internal-releases/twilio-verify-android/;publish=1",
          "maven.username" to Versions.projectProperty(project, "BINTRAY_USER"),
          "maven.password" to Versions.projectProperty(project, "BINTRAY_APIKEY")
      )
  )
}

task("bintrayLibraryReleaseUpload", GradleBuild::class) {
  description = "Publish Verify SDK release to bintray"
  group = "Publishing"
  buildFile = file("build.gradle.kts")
  tasks = listOf("assembleRelease", "uploadArchives")
  startParameter.projectProperties.plusAssign(
      gradle.startParameter.projectProperties + mapOf(
          "maven.repo" to "https://api.bintray.com/maven/twilio/releases/twilio-verify-android/;publish=1",
          "maven.username" to Versions.projectProperty(project, "BINTRAY_USER"),
          "maven.password" to Versions.projectProperty(project, "BINTRAY_APIKEY")
      )
  )
}