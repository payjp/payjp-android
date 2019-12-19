# PAY.JP Android SDK
[![Build Status](https://travis-ci.org/payjp/payjp-android.svg?branch=master)](https://travis-ci.org/payjp/payjp-android)
[![Maven Central](https://img.shields.io/maven-central/v/jp.pay/payjp-android.svg)](https://oss.sonatype.org/content/groups/public/jp/pay/payjp-android/)

オンライン決済サービス「[PAY.JP](https://pay.jp/)」のAndroidアプリ組込み用のSDKです。

<img src="https://user-images.githubusercontent.com/949882/71063858-0977de00-21b1-11ea-89b6-be4661ca41b6.png" width=300 />

## 使い方

利用については以下のガイドを参照してください。

Androidでの利用 | PAY.JP https://pay.jp/docs/mobileapp-android

## サンプルコード

`sample` モジュールを実行するとサンプルアプリを動かすことができます。

## 動作環境

- Android Studio 3.0以上
- Android OS 4.1 (API Level 16) 以上

## インストール

`build.gradle` の`dependencies`に下記を追加します。バージョンは最新のものにします。

```build.gradle
implementation 'jp.pay:payjp-android:$latest_version'
// Optional: Card.IO Extension
implementation 'jp.pay:payjp-android-cardio:$latest_version'
// Optional: Kotlin Coroutine Extension
implementation 'jp.pay:payjp-android-coroutine:$latest_version'
```

## リファレンス

https://payjp.github.io/payjp-android/

## License

PAY.JP Android SDK is available under the MIT license. See the LICENSE file for more info.