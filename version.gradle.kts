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
extra.set("verifyVersionName", generateVersionName())
extra.set("verifyVersionCode", generateVersionCode())