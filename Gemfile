source "https://rubygems.org"

gem 'abbrev'
gem "fastlane", ">= 2.228.0"
gem "danger", ">= 9.4.3"
gem "danger-checkstyle_formatter", ">= 0.0.3"
gem "danger-android_lint", ">= 0.0.9"
gem "danger-shroud", ">= 0.0.7"

plugins_path = File.join(File.dirname(__FILE__), 'fastlane', 'Pluginfile')
eval_gemfile(plugins_path) if File.exist?(plugins_path)
