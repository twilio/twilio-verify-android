/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
