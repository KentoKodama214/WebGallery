---
paths:
  - backend/**/domain/service/**
---

# Policyクラス（ドメインサービス）のアーキテクチャルール

複数のプロパティやEntityにまたがらない、単一のビジネスルール（ポリシー）を判定するドメインサービスを配置する。`domain/service/`に配置するが、クラス名は既存の`Policy`サフィックスを維持する（オニオンアーキテクチャの`application/service`（ユースケースのService）と紛らわしいが、パッケージが異なるため区別できる）。

## 命名規則

- クラス名サフィックス: `Policy`

## Springアノテーション

- `@Component`アノテーションを付与すること

## レイヤー間依存関係

- **許可するimport**: `domain/`配下全体（値オブジェクト、`enumeration/`、`constant/`、`exception/`）のみ
- **禁止するimport**: `application/`・`infrastructure/`・`presentation/`への依存（domainは最内層のためオニオンアーキテクチャ上外側の層を一切知らない）
- 特に`infrastructure/config/`（`application.ymlのプロパティを保持するConfigクラス）への直接依存は禁止。設定値が必要な場合は、Service層（`application/service/impl/`）が`application/config/`のポート経由でConfig値を読み取り、`domain/model/`の値オブジェクト（例: `PhotoUpperLimits`、`MaxFileSizeMb`）にラップした上でPolicyのメソッド引数として渡すこと

## メソッドシグネチャ

- 永続化されたデータの取得は行わず、呼び出し元（Service層）から渡された値のみで判定を行う（DBアクセスを行わない）
- 引数の型は、ドメインクラス（値オブジェクト）、Enumのみとする
- 返り値の型は、ドメインクラス（値オブジェクト）、Boolean、Integer（件数を返す時のみ）のみとする

## 検証

本ファイルのルールは`backend/src/test/java/com/web/gallery/architecture/PolicyArchitectureTest.java`および`backend/src/test/java/com/web/gallery/architecture/OnionArchitectureTest.java`のArchUnitテストで機械的に検証される。
