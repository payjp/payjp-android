fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android check

```sh
[bundle exec] fastlane android check
```

Runs all the tests

### android apidocs

```sh
[bundle exec] fastlane android apidocs
```

Generate API docs

### android create_pr_to_update_docs

```sh
[bundle exec] fastlane android create_pr_to_update_docs
```

Create PR to update docs

### android create_pr_to_public

```sh
[bundle exec] fastlane android create_pr_to_public
```

Create GitHub PR to `payjp/payjp-android` from internal repo. (internal only)

### android distribute_sample_app

```sh
[bundle exec] fastlane android distribute_sample_app
```

Distribute Sample App with Firebase App Distribution

### android test_robo_sample

```sh
[bundle exec] fastlane android test_robo_sample
```

Run robo test on Firebase Test Lab

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
