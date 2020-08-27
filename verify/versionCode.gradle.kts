val verifyVersionName: String by extra
val verifyVersionCode: String by extra

task("incrementVersion") {

  doLast {
    val versionCode = verifyVersionCode.toInt().plus(1)
    var versionName = verifyVersionName
    if (project.hasProperty("version_number")) {
      versionName = project.property("version_number") as String
    }
    ant.withGroovyBuilder {
      "propertyfile"("file" to "gradle.properties") {
        "entry"("key" to "verifyVersionName", "value" to versionName)
        "entry"("key" to "verifyVersionCode", "value" to versionCode)
      }
    }
  }
}
