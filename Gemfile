source "https://rubygems.org"

gem "fastlane"
gem "danger"
gem “danger-checkstyle_format”
gem ‘danger-android_lint’

plugins_path = File.join(File.dirname(__FILE__), 'fastlane', 'Pluginfile')
eval_gemfile(plugins_path) if File.exist?(plugins_path)
