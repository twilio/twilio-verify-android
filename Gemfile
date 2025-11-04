source "https://rubygems.org"

gem "fastlane"
gem "danger", ">= 8.3.1"
gem "danger-checkstyle_formatter", ">= 0.0.3"
gem "danger-android_lint", ">= 0.0.9"

plugins_path = File.join(File.dirname(__FILE__), 'fastlane', 'Pluginfile')
eval_gemfile(plugins_path) if File.exist?(plugins_path)
