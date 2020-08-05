plugins {
  id(Config.Plugins.androidLibrary)
  id(Config.Plugins.kotlinAndroid)
  id(Config.Plugins.kotlinAndroidExtensions)
  id("org.jetbrains.dokka")
  id(MavenPublish.plugin)
}

val versionMajor: String by project
val versionMinor: String by project
val versionPatch: String by project

fun generateVersionName() = "${versionMajor}.${versionMinor}.${versionPatch}"

fun generateVersionCode(): Int {
  val version = generateVersionName()
  val build = version.replace(".", "")
    .toInt()
  return build * 1000
}

val verifyVersionName = generateVersionName()
val verifyVersionCode = generateVersionCode()

android {
  compileSdkVersion(Config.Versions.compileSDKVersion)

  defaultConfig {
    minSdkVersion(Config.Versions.minSDKVersion)
    targetSdkVersion(Config.Versions.targetSDKVersion)
    versionCode = verifyVersionCode
    versionName = verifyVersionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
    buildConfigField("String", "BASE_URL", "\"https://verify.twilio.com/v2/\"")
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          "proguard-rules.pro"
      )
    }
  }
  testOptions.unitTests.isIncludeAndroidResources = true
}

val dokkaHtmlJar by tasks.register<Jar>("dokkaHtmlJar") {
  dependsOn(tasks.dokkaHtml)
  from(tasks.dokkaHtml.get().getOutputDirectoryAsFile())
  archiveClassifier.set("html-doc")
}

tasks.dokkaHtml {
  outputDirectory = "../docs/${verifyVersionName}"
  disableAutoconfiguration = false
  dokkaSourceSets {
    configureEach{
      includeNonPublic = false
      reportUndocumented = true
      skipEmptyPackages = true
    }
  }
}

val mavenRepo =
  if (project.hasProperty(MavenPublish.repo)) project.property(MavenPublish.repo) else ""
val mavenUsername = if (project.hasProperty(MavenPublish.username))
  project.property(MavenPublish.username) else ""
val mavenPassword = if (project.hasProperty(MavenPublish.password))
  project.property(MavenPublish.password) else ""
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

dependencies {
  val securityVersion: String by rootProject
  implementation(fileTree(mapOf("dir" to "libs", "includes" to listOf("*.jar"))))
  debugImplementation(project(Modules.security))
  releaseImplementation("com.twilio:twilio-security-android:$securityVersion")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
  androidTestImplementation("androidx.test.ext:junit:1.1.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
  androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.3.1")
  androidTestImplementation("com.squareup.okhttp3:okhttp-tls:4.3.1")
  androidTestImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
  testImplementation("junit:junit:4.12")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
  testImplementation("org.robolectric:robolectric:4.3.1")
  testImplementation("androidx.test:core:1.2.0")
  testImplementation("org.mockito:mockito-inline:2.28.2")
}
