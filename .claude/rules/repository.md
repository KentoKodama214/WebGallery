---
paths:
  - backend/**/repository/**
---

# Repository層のアーキテクチャルール

インターフェースは`application/repository/`（ユースケースが要求するポート）、実装は`infrastructure/persistence/repository/`（永続化の技術詳細）に配置する。命名は現状の`Repository`サフィックス・パッケージ名を踏襲し、`port`等への改名は行わない。

例外：`SchedulerLockRepository`（+実装、Mapper、Enum）は他のどのapplication/domain層からも参照されない完全に自己完結したクラスタのため、`application/repository/`・`infrastructure/persistence/repository/`ではなく`infrastructure/scheduler/lock/`に集約する（詳細は`scheduler.md`）。本ファイルのルールはこの例外には適用されない。

## 命名規則

- インターフェース: `Repository`サフィックス
- 実装クラス: `RepositoryImpl`サフィックス

## Springアノテーション

- `infrastructure/persistence/repository/`の実装クラスには`@Repository`アノテーションを付与すること

## レイヤー間依存関係

- **許可するimport（インターフェース、`application/repository/`）**: `application/model/`、`application/aggregate/`、`domain/`配下全体（値オブジェクト、`enumeration/`、`constant/`）
- **許可するimport（実装、`infrastructure/persistence/repository/`）**: 上記に加え`infrastructure/persistence/mapper/`、`infrastructure/persistence/entity/`、`infrastructure/persistence/dto/`
- **禁止するimport**: `presentation/controller/`、`application/service/`への直接依存
- **禁止するimport**: `presentation/request/`や`presentation/response/`のDTO
- **禁止するimport**: `infrastructure/persistence/repository/`の実装クラスから、自身に対応するインターフェース以外の`application/repository/`配下のインターフェース・`infrastructure/persistence/repository/`配下の実装への依存（`backend/src/test/java/com/web/gallery/ArchitectureTest.java`のArchUnitテストで機械的に検出される）

## インターフェースベース設計

- `application/repository/`にインターフェース、`infrastructure/persistence/repository/`に`RepositoryImpl`実装が対になること
- 実装クラスに対応するインターフェースが存在しない、またはその逆のケースは違反

## メソッドシグネチャ

- 引数の型は、ドメインクラス（値オブジェクト）、Modelクラス、集約クラス（`application/aggregate/`）、Enum（`domain/enumeration/`）のみとする（可読性と安全性の担保のため）
- 返り値の型は、ドメインクラス（値オブジェクト）、Modelクラス、集約クラス（`application/aggregate/`）、Boolean、Integer（ただし、件数を返す時のみ）、voidのみとする
- 引数が4つ以上になるなら、別途専用のModelクラスを定義する

## Dto/Entity → Modelの変換

- Repository実装がMapperから受け取った`Entity`/`Dto`をインターフェースの返り値の型（Model等）へ変換するロジックは、Repository実装クラス内のprivateメソッド（例: `toXxxModel(XxxDto dto)`）として実装する。Request→Modelの変換（`presentation/converter/`のConverterクラス）とは異なり、Dto/Entity→Model変換専用のクラスは設けない

## 集約Repository

- 複数のテーブルにまたがる整合性のあるユースケース単位の操作（例: `PhotoAggregateRepository`）を提供するRepositoryは、他のRepositoryインターフェース・実装には依存せず、対象テーブルの`infrastructure/persistence/mapper/`を直接操作して実装すること（Repository同士の依存は`ArchitectureTest`のArchUnitテストで禁止されている）
- 単票Repository（例: `PhotoMstRepository`）と処理内容が重複する場合でも、Mapper呼び出しレベルでの重複は許容する（レイヤー依存ルールを優先する）

## 検証

本ファイルのその他のルール（命名規則、インターフェース-実装の1対1対応、`@Repository`付与、追加の禁止import、メソッドシグネチャ）は`backend/src/test/java/com/web/gallery/architecture/RepositoryArchitectureTest.java`のArchUnitテストで機械的に検証される。
