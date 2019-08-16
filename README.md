# PAY.JP Android SDK
[![Build Status](https://travis-ci.org/payjp/payjp-android.svg?branch=master)](https://travis-ci.org/payjp/payjp-android)
[![Maven Central](https://img.shields.io/maven-central/v/jp.pay/payjp-android.svg)](https://oss.sonatype.org/content/groups/public/jp/pay/payjp-android/)

オンライン決済サービス「[PAY.JP](https://pay.jp/)」のAndroidアプリ組込み用のSDKです。

## 使い方

利用については以下のガイドを参照してください。

Androidでの利用 | PAY.JP https://pay.jp/docs/mobileapp-android

## サンプルコード

`sample` モジュールを実行するとサンプルアプリを動かすことができます。

## 動作環境

- Android Studio 3.0以上
- Android OS 4.0 (API Level 14) 以上

## インストール

`build.gradle` の`dependencies`に下記を追加します。バージョンは最新のものにします。

```build.gradle
implementation 'jp.pay:payjp-android:$latest_version'
```

## License

PAY.JP Android SDK is available under the MIT license. See the LICENSE file for more info.