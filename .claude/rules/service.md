---
paths:
  - backend/**/service/**
---

# Service層のアーキテクチャルール

## 命名規則

- インターフェース: `Service`サフィックス
- 実装クラス: `ServiceImpl`サフィックス

## Springアノテーション

- `service/impl/`の実装クラスには`@Service`アノテーションを付与すること
- `service/impl/`の実装クラスのpublicメソッドには、`@Transactional`アノテーションを付与すること

## レイヤー間依存関係

- **許可するimport**: `repository/`のインターフェース、`model/`、`aggregate/`、`constant/`、`enumeration/`、`exception/`、`policy/`、`domain/`、`config/`、`event/`
- **禁止するimport**: `controller/`、`mapper/`、`entity/`、`dto/`、`repository/impl/`への直接依存
- **禁止するimport**: `controller/request/`や`controller/response/`のDTO

## インターフェースベース設計

- `service/`にインターフェース、`service/impl/`に`ServiceImpl`実装が対になること
- 実装クラスに対応するインターフェースが存在しない、またはその逆のケースは違反

## メソッドシグネチャ

- 引数の型は、ドメインクラス（値オブジェクト）、Modelクラス、集約クラス（`aggregate/`）のみとする（可読性と安全性の担保のため）
- 返り値の型は、ドメインクラス（値オブジェクト）、Modelクラス、集約クラス（`aggregate/`）、Boolean、Integer（ただし、件数を返す時のみ）、voidのみとする
- 引数が4つ以上になるなら、別途専用のModelクラスを定義する
