package plugins

import Config

tasks {
  register<Copy>("copyGitHooks") {
    description = "Copies the git hooks from scripts/git-hooks to the .git folder."
    group = Config.Groups.git_hooks
    from("$rootDir/scripts/git-hooks/") {
      include("**/*.sh")
      rename("(.*).sh", "$1")
    }
    into("$rootDir/.git/hooks")
  }

  register<Exec>("installGitHooks") {
    description = "Installs the git hooks from scripts/git-hooks."
    group = Config.Groups.git_hooks
    workingDir(rootDir)
    commandLine("chmod")
    args("-R", "+x", ".git/hooks/")
    dependsOn(named("copyGitHooks"))
    doLast {
      logger.info("Git hooks installed successfully.")
    }
  }

  afterEvaluate {
    tasks["clean"].dependsOn(tasks.named("installGitHooks"))
  }
}
