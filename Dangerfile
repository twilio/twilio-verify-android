## METHODS DECLARATION ##
def checkForFileAndroid(file)
  ext = File.extname(file)
  case ext
  # Warn when a file .gradle is modified
  when ".gradle"
    message("`#{file}` was modified")
  end
  # Warn when a FileManifest.xml is modified
  message("`#{file}` was modified") if file =~ /AndroidManifest\.xml/
end

def exceptionMessages(file)
  if File.file?(file)
    message "Something went wrong checking `#{file}`. Check your Dangerfile"
  else
    message "One of modified files could not be read, does it really exist?"
  end
end

# Make it more obvious that a PR is a work in progress and shouldn't be merged yet
if github.pr_title.include? "[WIP]"
    warn("PR is classed as Work in Progress")
end

# Warn when there is a big PR
if git.lines_of_code > 500
    warn("Big PR")
end

# If these are all empty something has gone wrong, better to raise it in a comment
if git.modified_files.empty? && git.added_files.empty? && git.deleted_files.empty?
  warn "This PR has no changes at all, this is likely an issue during development."
end

# Mainly to encourage writing up some reasoning about the PR, rather than
# just leaving a title
if github.pr_body.length < 120
  warn "Please provide a summary in the Pull Request description"
end

# Ensure that the PR title follows the convention
if !(github.pr_title =~ /\[ACCSEC-([0-9])+\](.*)/)
  warn "The Pull Request title does not follow the convention [ACCSEC-0000] PR Title text"
end

#Check modified files, apply rules to them
git.modified_files.each do |file|
  begin
    checkForFileAndroid(file)
  rescue
    exceptionMessages(file)
  end
end

github.dismiss_out_of_range_messages

# ktlint
#checkstyle_format.base_path = Dir.pwd
#checkstyle_format.report "verify/build/reports/ktlint/ktlint.xml"
#checkstyle_format.report "security/build/reports/ktlint/ktlint.xml"
#checkstyle_format.report "sample/build/reports/ktlint/ktlint.xml"
#checkstyle_format.report "build/reports/ktlint/ktlint.xml"
ktlint.lint(inline_mode: true)

# AndroidLint
android_lint.report_file = "sample/build/reports/lint-results.xml"
android_lint.report_file = "verify/build/reports/lint-results.xml"
android_lint.report_file = "security/build/reports/lint-results.xml"
#android_lint.skip_gradle_task = true
android_lint.filtering = true
android_lint.severity = "Error"
android_lint.lint(inline_mode: true)