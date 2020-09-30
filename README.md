# PAY.JP Android SDK
[![Build Status](https://travis-ci.org/payjp/payjp-android.svg?branch=master)](https://travis-ci.org/payjp/payjp-android)
[![Maven Central](https://img.shields.io/maven-central/v/jp.pay/payjp-android.svg)](https://oss.sonatype.org/content/groups/public/jp/pay/payjp-android/)

オンライン決済サービス「[PAY.JP](https://pay.jp/)」のAndroidアプリ組込み用のSDKです。

| `FACE_MULTI_LINE` | `FACE_CARD_DISPLAY` |
| - | - |
| <img alt="face_multi_line" width=300 src="https://user-images.githubusercontent.com/949882/80774525-3d6c0280-8b98-11ea-92b7-f71528c7fcde.png" /> | <img alt="face_card_display" width=300 src="https://user-images.githubusercontent.com/949882/80774521-3a711200-8b98-11ea-966a-b82bb10c7b54.png" /> |

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

https://payjp.github.io/payjp-android/payjp-android/

## License

PAY.JP Android SDK is available under the MIT license. See the LICENSE file for more info.