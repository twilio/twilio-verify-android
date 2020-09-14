task("bintrayLibraryReleaseCandidateUpload", GradleBuild::class) {
  dependsOn(
    ":security:bintrayLibraryReleaseCandidateUpload"
  ).finalizedBy(":verify:bintrayLibraryReleaseCandidateUpload")
}

task("bintrayLibraryReleaseUpload", GradleBuild::class) {
  dependsOn(
    ":security:bintrayLibraryReleaseCandidateUpload"
  ).finalizedBy(":verify:bintrayLibraryReleaseCandidateUpload")
}
