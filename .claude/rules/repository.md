---
paths:
  - backend/**/repository/**
---

# Repository層のアーキテクチャルール

## レイヤー間依存関係

- **許可するimport**: `mapper/`、`entity/`、`dto/`、`model/`、`constant/`
- **禁止するimport**: `controller/`、`service/`への直接依存
- **禁止するimport**: `controller/request/`や`controller/response/`のDTO

## インターフェースベース設計

- `repository/`にインターフェース、`repository/impl/`に`RepositoryImpl`実装が対になること
- 実装クラスに対応するインターフェースが存在しない、またはその逆のケースは違反

## 命名規則

- インターフェース: `Repository`サフィックス
- 実装クラス: `RepositoryImpl`サフィックス

## Springアノテーション

- `repository/impl/`の実装クラスには`@Repository`アノテーションを付与すること
