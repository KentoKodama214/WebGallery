---
paths:
  - backend/**/application/service/**
---

# Service層のアーキテクチャルール

## 命名規則

- インターフェース: `Service`サフィックス
- 実装クラス: `ServiceImpl`サフィックス

## Springアノテーション

- `application/service/impl/`の実装クラスには`@Service`アノテーションを付与すること
- `application/service/impl/`の実装クラスのpublicメソッドには、`@Transactional`アノテーションを付与すること
- `@Transactional(readOnly = true)`はリードレプリカへルーティングされうる（`infrastructure/config/DataSourceConfig`）。直前の書き込みトランザクションの結果を同一フロー内で即座に読み戻す必要がある処理には`readOnly = true`を付与しないこと（詳細: `doc/architecture/datasource.md`）

## レイヤー間依存関係

- **許可するimport**: `application/repository/`のインターフェース、`application/model/`、`application/aggregate/`、`application/helper/`（JwtTokenProvider・GeoIpResolver・ReauthenticationThrottle等のポート）、`application/config/`（Configポート）、`domain/`配下全体（値オブジェクト、`domain/service/`のPolicy、`domain/enumeration/`、`domain/constant/`、`domain/exception/`、`domain/event/`）
- **禁止するimport**: `presentation/controller/`、`infrastructure/persistence/mapper・entity・dto`、`infrastructure/persistence/repository/`（repositoryの実装）への直接依存
- **禁止するimport**: `infrastructure/config/`の具象Configクラスへの直接依存。設定値は必ず`application/config/`のポート型で受け取ること（実装は`infrastructure/config/`の`*ConfigImpl`がDIで注入される）
- **禁止するimport**: `presentation/request/`や`presentation/response/`のDTO

## インターフェースベース設計

- `application/service/`にインターフェース、`application/service/impl/`に`ServiceImpl`実装が対になること
- 実装クラスに対応するインターフェースが存在しない、またはその逆のケースは違反

## メソッドシグネチャ

- 引数の型は、ドメインクラス（値オブジェクト）、Modelクラス、集約クラス（`application/aggregate/`）のみとする（可読性と安全性の担保のため）
- 返り値の型は、ドメインクラス（値オブジェクト）、Modelクラス、集約クラス（`application/aggregate/`）、Boolean、Integer（ただし、件数を返す時のみ）、voidのみとする
- 引数が4つ以上になるなら、別途専用のModelクラスを定義する

## 検証

本ファイルのルールは`backend/src/test/java/com/web/gallery/architecture/ServiceArchitectureTest.java`のArchUnitテストで機械的に検証される。メソッドシグネチャの検証は`Service`インターフェースのメソッドを対象とし、`UserDetailsService`等の外部インターフェースの実装やSpringの`@EventListener`ハンドラなど、`ServiceImpl`が独自interfaceの契約外に持つメソッドは対象としない。
