---
paths:
  - backend/**/service/**
---

# Service層のアーキテクチャルール

## レイヤー間依存関係

- **許可するimport**: `repository/`のインターフェース、`model/`、`constant/`、`enumuration/`、`exception/`
- **禁止するimport**: `controller/`、`mapper/`、`entity/`、`dto/`、`repository/impl/`への直接依存
- **禁止するimport**: `controller/request/`や`controller/response/`のDTO

## インターフェースベース設計

- `service/`にインターフェース、`service/impl/`に`ServiceImpl`実装が対になること
- 実装クラスに対応するインターフェースが存在しない、またはその逆のケースは違反

## 命名規則

- インターフェース: `Service`サフィックス
- 実装クラス: `ServiceImpl`サフィックス

## Springアノテーション

- `service/impl/`の実装クラスには`@Service`アノテーションを付与すること
