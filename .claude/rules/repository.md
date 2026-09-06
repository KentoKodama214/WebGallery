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
- **禁止するimport**: `repository/impl/`の実装クラスから、自身に対応するインターフェース以外の`repository/`配下のインターフェース・実装への依存（`backend/src/test/java/com/web/gallery/ArchitectureTest.java`のArchUnitテストで機械的に検出される）

## インターフェースベース設計

- `repository/`にインターフェース、`repository/impl/`に`RepositoryImpl`実装が対になること
- 実装クラスに対応するインターフェースが存在しない、またはその逆のケースは違反

## メソッドシグネチャ

- 引数の型は、ドメインクラス（値オブジェクト）、Modelクラス、集約クラス（`aggregate/`）のみとする（可読性と安全性の担保のため）
- 返り値の型は、ドメインクラス（値オブジェクト）、Modelクラス、集約クラス（`aggregate/`）、Boolean、Integer（ただし、件数を返す時のみ）、voidのみとする
- 引数が4つ以上になるなら、別途専用のModelクラスを定義する

## 集約Repository

- 複数のテーブルにまたがる整合性のあるユースケース単位の操作（例: `PhotoAggregateRepository`）を提供するRepositoryは、他のRepositoryインターフェース・実装には依存せず、対象テーブルの`mapper/`を直接操作して実装すること（Repository同士の依存は`ArchitectureTest`のArchUnitテストで禁止されている）
- 単票Repository（例: `PhotoMstRepository`）と処理内容が重複する場合でも、Mapper呼び出しレベルでの重複は許容する（レイヤー依存ルールを優先する）
