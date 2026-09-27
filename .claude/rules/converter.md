---
paths:
  - backend/**/presentation/converter/**
---

# Converterクラスのアーキテクチャルール

## 背景

オニオンアーキテクチャでは`application/model/`のModelクラス（application層）が`presentation/request/`のRequestクラス（presentation層）を直接参照すると、内側の層が外側の層を知ってしまう依存性逆転になる。これを避けるため、Request→Modelの変換ロジックはModelクラス自身の静的ファクトリメソッド（`from(Request)`）ではなく、presentation層に置く専用のConverterクラスに切り出す。

ファクトリパターン自体（`static`なファクトリメソッド経由でのModel生成）は維持しつつ、変換ロジックの置き場所だけをControllerからConverterへ分離する。Controllerクラス自身に変換ロジックを持たせないこと。

Dto/Entity → Modelの変換（`infrastructure/persistence/`側）は本パターンの対象外。対応するRepository実装クラス内のprivateメソッドとして実装する（詳細は`repository.md`）。Converterクラスを設けるのはRequest→Modelの変換のみ。

## 命名規則

- クラス名サフィックス: `Converter`
- 機能単位（`AccountConverter`、`InquiryConverter`、`PhotoConverter`等）に分割する。フィールド単位ではなくRequestクラス群の機能に対応させる
- メソッド名は`toXxxModel`（例: `toAccountModelForUpdate`、`toPhotoDetailModelForRegist`）とする

## 配置・Springアノテーション

- `presentation/converter/`に配置する
- `@Component`を付与し、ControllerへコンストラクタインジェクションでDIする

## メソッドシグネチャ

- 引数は、対応する`presentation/request/`のRequestクラス、およびセッション由来の値（`AccountNo`等のドメインクラスやプリミティブ型）とする
- 返り値は`application/model/`のModelクラスとする
- 複数のRequestサブタイプ（登録用・更新用等）に対応するオーバーロードを設けてよい
- Request内のタグ登録リクエスト等、ネストした変換が必要な場合は同一Converterクラス内のprivateメソッドに切り出す

## レイヤー間依存関係

- **許可するimport**: `application/model/`、`presentation/request/`、`domain/`配下全体（値オブジェクト、`enumeration/`、`constant/`）
- **禁止するimport**: `infrastructure/persistence/mapper・entity・dto・repository`、`application/service/impl/`への直接依存

## テスト

- `unit-test-model.md`と同様、Mockitoは使用せず対象Converterクラスを直接インスタンス化してテストする
- 全項目を設定した場合にそのまま値が転写されること（`toXxxModel_allFieldsSet`）と、任意項目を未設定にした場合にnull・デフォルト値になること（`toXxxModel_optionalFieldsNull`）の双方を検証すること
- 空文字が`null`に変換される等、ドメイン固有の暗黙変換ルールがある場合は専用のテストケースを追加すること
- テストクラスは`presentation/converter/`配下に、対象Converterと同名+`Test`で配置する

## 検証

本ファイルのルールは`backend/src/test/java/com/web/gallery/architecture/ConverterArchitectureTest.java`のArchUnitテストで機械的に検証される。
