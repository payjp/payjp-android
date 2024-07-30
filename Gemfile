source "https://rubygems.org"

gem "fastlane"
plugins_path = File.join(File.dirname(__FILE__), 'fastlane', 'Pluginfile')
# workaround for https://github.com/dependabot/dependabot-core/issues/1720
eval_gemfile('fastlane/Pluginfile') if File.exist?(plugins_path)
