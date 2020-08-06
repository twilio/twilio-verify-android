// Top-level build file where you can add configuration options common to all sub-projects/modules.
apply(from = "publish.gradle.kts")
plugins {
    id(Config.Plugins.dokka) version Config.Versions.dokka
}

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath(Config.Dependencies.androidTools)
        classpath(kotlin(Config.Dependencies.kotlin, Config.Versions.kotlin))
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
                username = Config.projectProperty(project, "BINTRAY_USER")
                password = Config.projectProperty(project, "BINTRAY_APIKEY")
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}