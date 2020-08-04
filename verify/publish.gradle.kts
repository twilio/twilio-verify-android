/*
 * Copyright (c) 2020, Twilio Inc.
 */

apply(plugin = MavenPublish.plugin)
apply(from = "../version.gradle.kts")

val mavenRepo =
  if (project.hasProperty(MavenPublish.repo)) project.property(MavenPublish.repo) else ""
val mavenUsername = if (project.hasProperty(MavenPublish.username))
  project.property(MavenPublish.username) else ""
val mavenPassword = if (project.hasProperty(MavenPublish.password))
  project.property(MavenPublish.password) else ""
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
            MavenPublish.Bintray.repository(
                MavenPublish.Bintray.url to uri(
                    mavenRepo ?: ""
                )
            ) {
              MavenPublish.Bintray.authentication(
                  MavenPublish.Bintray.userName to mavenUsername,
                  MavenPublish.Bintray.password to mavenPassword
              )
            }
          }
          pom.project {
            withGroovyBuilder {
              MavenPublish.Bintray.version(verifyVersionName)
              MavenPublish.Bintray.groupId(pomGroup)
              MavenPublish.Bintray.artifactId(pomArtifactId)
              MavenPublish.Bintray.packaging(pomPackaging)
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
      gradle.startParameter.projectProperties + MavenPublish.Bintray.credentials(
          project,
          "https://api.bintray.com/maven/twilio/internal-releases/twilio-verify-android/;publish=1",
          MavenPublish.Bintray.user, MavenPublish.Bintray.apiKey
      )
  )
}

task("bintrayLibraryReleaseUpload", GradleBuild::class) {
  description = "Publish Verify SDK release to bintray"
  group = "Publishing"
  buildFile = file("build.gradle.kts")
  tasks = listOf("assembleRelease", "uploadArchives")

  startParameter.projectProperties.plusAssign(
      gradle.startParameter.projectProperties + MavenPublish.Bintray.credentials(
          project,
          "https://api.bintray.com/maven/twilio/releases/twilio-verify-android/;publish=1",
          MavenPublish.Bintray.user, MavenPublish.Bintray.apiKey
      )
  )
}