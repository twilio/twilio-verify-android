# Releasing Twilio Verify Android

Releases are published to Maven Central by GitHub Actions. Changes reach `main` only through reviewed pull requests, so a release has two parts: a workflow opens a release PR with the version bump, and merging that PR publishes it.

## Steps

1. **Merge `dev` into `main`** with a pull request, as for any change. Nothing is published yet.
2. **Run Prepare release.** In the **Actions** tab, select **Prepare release**, then **Run workflow** on `main`. The job needs an approval for the `production` environment and starts after a 30-minute wait. It:
   - works out the next version from the commits since the last release (see [Versions](#versions));
   - bumps the version in `verify/gradle.properties`;
   - adds the release notes and the SDK size report to `CHANGELOG.md`;
   - generates the API docs in `docs/<version>` and points `docs/latest` at them;
   - opens a **Release &lt;version&gt;** pull request from a `release/<version>` branch into `main`.

   If no commits since the last release need one, it finishes without opening a PR.
3. **Review and merge the release PR.** Check the version, the changelog entry and the docs. While the PR is open, testers receive DEV, STAGE and PROD builds of the release candidate through Firebase App Distribution.
4. **Publish release runs on the merge.** It needs another `production` approval and 30-minute wait, then it:
   - tags the merge commit with the version;
   - publishes the SDK to Maven Central;
   - creates the GitHub release from the changelog entry;
   - sends the release build of the sample app to testers.
5. **Merge `main` back into `dev`** so `dev` has the version bump and the changelog entry.

## Versions

The next version comes from the [Conventional Commits](https://www.conventionalcommits.org) messages since the last release:

| Commits since the last release include | Next version |
|---|---|
| A `BREAKING CHANGE:` line in a commit body | Major |
| `feat:` | Minor |
| `fix:` | Patch |
| Only other types (`chore:`, `docs:`, `refactor:`, ...) | No release |

A `!` after the type (`feat!:`) doesn't count as a breaking change; add a `BREAKING CHANGE:` line to the commit body instead.

## Testing a release

- **Before it's published:** check out the `release/<version>` branch and run `./gradlew :sample:assembleRelease`. The sample builds against the SDK source, which is exactly what gets published.
- **After it's published:** `./gradlew :sample:assembleRelease -PsampleVerifyVersion=<version>` builds the sample against the version on Maven Central, to check the published artifact.
