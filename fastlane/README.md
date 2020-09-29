fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew cask install fastlane`

# Available Actions
## Android
### android check
```
fastlane android check
```
Runs all the tests
### android apidocs
```
fastlane android apidocs
```
Generate API docs
### android create_pr_to_public
```
fastlane android create_pr_to_public
```
Create GitHub PR to `payjp/payjp-android` from internal repo. (internal only)
### android distribute_sample_app
```
fastlane android distribute_sample_app
```
Distribute Sample App with Firebase App Distribution
### android test_robo_sample
```
fastlane android test_robo_sample
```
Run robo test on Firebase Test Lab

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
