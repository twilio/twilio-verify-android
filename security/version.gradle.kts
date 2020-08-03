val versionMajor: String by project
val versionMinor: String by project
val versionPatch: String by project

fun generateSecurityVersionName() = "${versionMajor}.${versionMinor}.${versionPatch}"
fun generateSecurityVersionCode(): Int {
    val version = generateSecurityVersionName()
    val build = version.replace(".", "")
        .toInt()
    return build * 1000
}
extra.set("securityVersionCode", generateSecurityVersionCode())
rootProject.extra.set("securityVersion", generateSecurityVersionName())
