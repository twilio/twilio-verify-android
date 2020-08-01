val versionMajor: String by project
val versionMinor: String by project
val versionPatch: String by project

fun generateSecurityVersionName() = "${versionMajor}.${versionMinor}.${versionPatch}"
extra.set("securityVersion", generateSecurityVersionName())
