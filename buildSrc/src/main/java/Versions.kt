import org.gradle.api.Project

/*
 * Copyright (c) 2020, Twilio Inc.
 */

object Versions {
  const val kotlin = "1.3.72"
  const val gradle = "3.6.3"
  const val googleServices = "4.3.3"
  const val firebasePerformance = "1.3.1"


  fun projectProperty(project: Project, property: String): String {
    val value =
      if (project.hasProperty(property)) project.property(property) as? String else System.getenv(
        property
      )
    return value ?: ""
  }
}