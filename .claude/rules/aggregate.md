---
paths:
  - backend/**/aggregate/**
---

# 集約（Aggregate）クラスのアーキテクチャルール

## 目的

複数のRepository（テーブル）にまたがる整合性・ライフサイクルを1つのオブジェクトに集約し、Service層でのオーケストレーションを削減する。書き込みユースケース（登録・更新・削除）の整合性管理にのみ責務を持ち、読み取り専用のクエリはこの層を経由しない。

## クラス設計

- 集約ルートクラスは通常のPOJOとし、コンストラクタは`private`とする
- インスタンス化は静的ファクトリメソッド（`forRegist`, `forUpdate`, `forDelete`, `reconstruct`等）経由のみ
- Setterは公開しない。状態変更は業務的な意味を持つメソッド名を通じてのみ行う
- 内部で保持する`model/`のModelクラス・`domain/`の値オブジェクト自体は不変のまま扱い、「変更」は既存インスタンスの書き換えではなく新しいインスタンスへの差し替えで表現する

## レイヤー間依存関係

- **許可するimport**: `model/`、`domain/`、`constant/`、`enumeration/`、`exception/`
- **禁止するimport**: `controller/`、`mapper/`、`entity/`、`dto/`、`repository/impl/`への直接依存

## 命名規則

- 集約ルートクラス名: 対象概念そのもの（例: `Photo`）。サフィックスは付与しない
