---
paths:
  - backend/**/repository/**
---

# Repository層のアーキテクチャルール

## 命名規則

- インターフェース: `Repository`サフィックス
- 実装クラス: `RepositoryImpl`サフィックス

## Springアノテーション

- `repository/impl/`の実装クラスには`@Repository`アノテーションを付与すること

## レイヤー間依存関係

- **許可するimport**: `mapper/`、`entity/`、`dto/`、`model/`、`aggregate/`、`constant/`
- **禁止するimport**: `controller/`、`service/`への直接依存
- **禁止するimport**: `controller/request/`や`controller/response/`のDTO

## インターフェースベース設計

- `repository/`にインターフェース、`repository/impl/`に`RepositoryImpl`実装が対になること
- 実装クラスに対応するインターフェースが存在しない、またはその逆のケースは違反

## メソッドシグネチャ

- 引数の型は、ドメインクラス（値オブジェクト）、Modelクラス、集約クラス（`aggregate/`）のみとする（可読性と安全性の担保のため）
- 返り値の型は、ドメインクラス（値オブジェクト）、Modelクラス、集約クラス（`aggregate/`）、Boolean、Integer（ただし、件数を返す時のみ）、voidのみとする
- 引数が4つ以上になるなら、別途専用のModelクラスを定義する

## 集約Repository

- 複数のテーブルにまたがる整合性のあるユースケース単位の操作（例: `PhotoAggregateRepository`）を提供するRepositoryは、既存の単票Repositoryインターフェース（例: `PhotoMstRepository`、`PhotoTagMstRepository`）をコンストラクタインジェクションで合成して実装し、Mapper・Entityへの直接依存は避けること（永続化ロジックの重複防止のため）
