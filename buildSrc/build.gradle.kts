import org.gradle.kotlin.dsl.`kotlin-dsl`
plugins {
    `kotlin-dsl`
}
repositories {
    jcenter()
}

dependencies {
  implementation("com.diffplug.spotless:spotless-plugin-gradle:5.1.1")
}
