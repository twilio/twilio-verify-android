import org.gradle.kotlin.dsl.`kotlin-dsl`
plugins {
    `kotlin-dsl`
}
repositories {
    jcenter()
}

val JACOCO = "0.8.5"
dependencies {
    implementation("org.jacoco:org.jacoco.core:${JACOCO}")
}