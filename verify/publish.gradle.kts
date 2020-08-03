/*
 * Copyright (c) 2020, Twilio Inc.
 */

apply(plugin = "maven")
apply(from = "../version.gradle.kts")

val mavenRepo =
  if (project.hasProperty(Constants.Maven.repo)) project.property(Constants.Maven.repo) else ""
val mavenUsername = if (project.hasProperty(Constants.Maven.username))
  project.property(Constants.Maven.username) else ""
val mavenPassword = if (project.hasProperty(Constants.Maven.password))
  project.property(Constants.Maven.password) else ""
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
            Constants.Bintray.repository(
                Constants.Bintray.url to uri(
                    mavenRepo ?: ""
                )
            ) {
              Constants.Bintray.authentication(
                  Constants.Bintray.userName to mavenUsername,
                  Constants.Bintray.password to mavenPassword
              )
            }
          }
          pom.project {
            withGroovyBuilder {
              Constants.Bintray.version(verifyVersionName)
              Constants.Bintray.groupId(pomGroup)
              Constants.Bintray.artifactId(pomArtifactId)
              Constants.Bintray.packaging(pomPackaging)
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
      gradle.startParameter.projectProperties + Constants.Bintray.credentials(
          project,
          "https://api.bintray.com/maven/twilio/internal-releases/twilio-verify-android/;publish=1",
          Constants.Bintray.user, Constants.Bintray.apiKey
      )
  )
}

task("bintrayLibraryReleaseUpload", GradleBuild::class) {
  description = "Publish Verify SDK release to bintray"
  group = "Publishing"
  buildFile = file("build.gradle.kts")
  tasks = listOf("assembleRelease", "uploadArchives")

  startParameter.projectProperties.plusAssign(
      gradle.startParameter.projectProperties + Constants.Bintray.credentials(
          project,
          "https://api.bintray.com/maven/twilio/releases/twilio-verify-android/;publish=1",
          Constants.Bintray.user, Constants.Bintray.apiKey
      )
  )
}