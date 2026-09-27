---
paths:
  - backend/**/application/aggregate/**
---

# 集約（Aggregate）クラスのアーキテクチャルール

集約ルートは、書き込みユースケースの整合性管理という点でユースケース固有の関心事であり、`domain/`ではなく`application/aggregate/`に配置する（`domain`は値オブジェクトとドメインサービス＝Policyのみを置く最内層）。

## 目的

複数のRepository（テーブル）にまたがる整合性・ライフサイクルを1つのオブジェクトに集約し、Service層でのオーケストレーションを削減する。書き込みユースケース（登録・更新・削除）の整合性管理にのみ責務を持ち、読み取り専用のクエリはこの層を経由しない。

## クラス設計

- 集約ルートクラスは通常のPOJOとし、コンストラクタは`private`とする
- インスタンス化は静的ファクトリメソッド（`forRegist`, `forUpdate`, `forDelete`, `reconstruct`等）経由のみ
- Setterは公開しない。状態変更は業務的な意味を持つメソッド名を通じてのみ行う
- 内部で保持する`model/`のModelクラス・`domain/`の値オブジェクト自体は不変のまま扱い、「変更」は既存インスタンスの書き換えではなく新しいインスタンスへの差し替えで表現する

## レイヤー間依存関係

- **許可するimport**: `application/model/`、`domain/`配下全体（値オブジェクト、`enumeration/`、`constant/`、`exception/`）
- **禁止するimport**: `presentation/`配下全体（Controller・Request・Response・Converter）、`infrastructure/persistence/mapper・entity・dto`、`infrastructure/persistence/repository/`への直接依存

## 命名規則

- 集約ルートクラス名: 対象概念そのもの（例: `Photo`）。サフィックスは付与しない

## 検証

コンストラクタのprivate化、publicなsetterの非公開、mapper・entity・dto・repository.implへの依存禁止は`backend/src/test/java/com/web/gallery/architecture/AggregateArchitectureTest.java`のArchUnitテストで機械的に検証される。presentation配下全体への依存禁止は、application層全体を対象とする`backend/src/test/java/com/web/gallery/architecture/OnionArchitectureTest.java`（`applicationShouldNotDependOnOuterLayers`）で検証される。
