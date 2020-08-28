package plugins

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin

apply<SpotlessPlugin>()

configure<SpotlessExtension> {
  format("misc") {
    target(
      fileTree(
        mapOf(
          "dir" to ".",
          "include" to listOf("**/*.md", "**/.gitignore", "**/*.yaml", "**/*.yml"),
          "exclude" to listOf(
            ".gradle/**",
            ".gradle-cache/**",
            "**/tools/**",
            "**/build/**",
            "vendor/**"
          )
        )
      )
    )
    trimTrailingWhitespace()
    indentWithSpaces()
    endWithNewline()
  }
  format("xml") {
    target(
      fileTree(
        mapOf(
          "dir" to ".",
          "include" to listOf("**/res/**/*.xml"),
          "exclude" to listOf("**/build/**")
        )
      )
    )
    indentWithSpaces(2)
    trimTrailingWhitespace()
    endWithNewline()
  }
  kotlin {
    target(
      fileTree(
        mapOf(
          "dir" to ".",
          "include" to listOf("**/*.kt"),
          "exclude" to listOf("**/build/**")
        )
      )
    )
    ktlint().userData(mapOf("indent_size" to "2", "continuation_indent_size" to "2"))
    trimTrailingWhitespace()
    indentWithSpaces()
    endWithNewline()
  }
  kotlinGradle {
    target(
      fileTree(
        mapOf(
          "dir" to ".",
          "include" to listOf("**/*.gradle.kts", "*.gradle.kts"),
          "exclude" to listOf("**/build/**")
        )
      )
    )
    ktlint().userData(mapOf("indent_size" to "2", "continuation_indent_size" to "2"))
    trimTrailingWhitespace()
    indentWithSpaces()
    endWithNewline()
  }
}
