source "https://rubygems.org"

gem "fastlane", ">= 2.207.0"
gem "danger", ">= 8.4.0"
gem "danger-checkstyle_formatter", ">= 0.0.3"
gem "danger-android_lint", ">= 0.0.10"

plugins_path = File.join(File.dirname(__FILE__), 'fastlane', 'Pluginfile')
eval_gemfile(plugins_path) if File.exist?(plugins_path)
