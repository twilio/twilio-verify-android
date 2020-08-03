/*
 * Copyright (c) 2020, Twilio Inc.
 */

apply(plugin = UploadConstants.Maven.plugin)
apply(from = "version.gradle.kts")

val mavenRepo =
  if (project.hasProperty(UploadConstants.Maven.repo)) project.property(UploadConstants.Maven.repo) else ""
val mavenUsername = if (project.hasProperty(UploadConstants.Maven.username))
  project.property(UploadConstants.Maven.username) else ""
val mavenPassword = if (project.hasProperty(UploadConstants.Maven.password))
  project.property(UploadConstants.Maven.password) else ""
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
            UploadConstants.Bintray.repository(UploadConstants.Bintray.url to uri(mavenRepo ?: "")) {
              UploadConstants.Bintray.authentication(
                  UploadConstants.Bintray.userName to mavenUsername,
                  UploadConstants.Bintray.password to mavenPassword
              )
            }
          }
          pom.project {
            withGroovyBuilder {
              UploadConstants.Bintray.version(securityVersion)
              UploadConstants.Bintray.groupId(pomGroup)
              UploadConstants.Bintray.artifactId(pomArtifactId)
              UploadConstants.Bintray.packaging(pomPackaging)
            }
          }
        }
      }
    }
  }
}

task("bintrayLibraryReleaseCandidateUpload", GradleBuild::class) {
  description = "Publish Security SDK release candidate to internal bintray"
  group = UploadConstants.Bintray.group
  buildFile = file("build.gradle.kts")
  tasks = listOf("assembleRelease", "uploadArchives")
  startParameter.projectProperties.plusAssign(
      gradle.startParameter.projectProperties +
          UploadConstants.Bintray.credentials(
              project,
              "https://api.bintray.com/maven/twilio/internal-releases/twilio-security-android/;publish=1",
              UploadConstants.Bintray.user, UploadConstants.Bintray.apiKey
          )
  )
}

task("bintrayLibraryReleaseUpload", GradleBuild::class) {
  description = "Publish Security SDK release to bintray"
  group = UploadConstants.Bintray.group
  buildFile = file("build.gradle.kts")
  tasks = listOf("assembleRelease", "uploadArchives")
  startParameter.projectProperties.plusAssign(
      gradle.startParameter.projectProperties + UploadConstants.Bintray.credentials(
          project,
          "https://api.bintray.com/maven/twilio/releases/twilio-security-android/;publish=1",
          UploadConstants.Bintray.user, UploadConstants.Bintray.apiKey
      )
  )
}