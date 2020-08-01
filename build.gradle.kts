// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
  repositories {
    google()
    jcenter()
  }
  dependencies {
    classpath(Config.Dependencies.androidTools)
    classpath(kotlin(Config.Dependencies.kotlin, Versions.kotlin))
    classpath(Config.Dependencies.googleServices)
    classpath(Config.Dependencies.firebasePerformance)
    // NOTE: Do not place your application dependencies here; they belong
    // in the individual module build.gradle files
  }
}

allprojects {
  repositories {
    google()
    jcenter()
    maven {
      url = uri("https://twilio.bintray.com/internal-releases")
      credentials {
        username = projectProperty("BINTRAY_USER")
        password = projectProperty("BINTRAY_APIKEY")
      }
    }
  }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

fun projectProperty(property: String): String {
  val value =
    if (project.hasProperty(property)) project.property(property) as? String else System.getenv(
        property
    )
  return value ?: ""
}
