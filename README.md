# PAY.JP Android SDK

オンライン決済サービス「[PAY.JP](https://pay.jp/)」のAndroidアプリ組込み用のSDKです。

## 使い方

SDKを利用してクレジットカードの情報をトークン化します。

カード情報のトークン化については以下のドキュメントを参照してください。

カード情報のトークン化 | PAY.JP https://pay.jp/docs/cardtoken

### SDKの初期化

`android.app.Application` クラスを継承したアプリケーションクラスの `onCreate()` でSDKを初期化します。

このときpublicKeyにはPAY.JPで発行されたパブリックキーを設定してください。

```kotlin
class SampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        PayjpToken.init("pk_test_0383a1b8f91e8a6e3ea0e2a9")
    }
}
```

### トークンの作成

```kotlin
fun startCreateToken() {
  PayjpToken.getInstance().createToken("4242424242424242", "123", "02", "2020")
      .enqueue(object : Task.Callback<Token> {
          override fun onSuccess(data: Token) {
              // you can charge with `Token#id`
              Log.i("MainActivity", "token => $data")
          }

          override fun onError(throwable: Throwable) {
              Log.e("MainActivity", "failure creating token", throwable)
          }
      })
}
```

### 生成済みのトークン情報を取得する

```kotlin
fun getTokenById(tokenId: String) {
  PayjpToken.getInstance().getToken(textId)
      .enqueue(object : Task.Callback<Token> {
          override fun onSuccess(data: Token) {
              // you can charge with `Token#id`
              Log.i("MainActivity", "token => $data")
          }

          override fun onError(throwable: Throwable) {
              Log.e("MainActivity", "failure getting token", throwable)
          }
      })
}
```

より詳細な利用方法はサンプルコードを参照してください。

## サンプルコード

- `sample-kotlin`: Kotlinのサンプルコードです。
- `sample-java`: Javaのサンプルコードです。

## 動作環境

- Android Studio 3.0以上
- Android OS 4.0 (API Level 14) 以上

## インストール

TBD

## License

PAY.JP Android SDK is available under the MIT license. See the LICENSE file for more info.