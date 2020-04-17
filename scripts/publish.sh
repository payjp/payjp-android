#!/bin/bash -eux
# for releng
./gradlew clean cleanBuildCache
./gradlew build
./gradlew payjp-android-core:uploadArchives
./gradlew payjp-android-verifier:uploadArchives
./gradlew payjp-android-cardform:uploadArchives
./gradlew payjp-android-main:uploadArchives
./gradlew payjp-android-cardio:uploadArchives
./gradlew payjp-android-coroutine:uploadArchives