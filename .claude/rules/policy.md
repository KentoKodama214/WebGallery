---
paths:
  - backend/**/policy/**
---

# Policyクラス（ドメインサービス）のアーキテクチャルール

複数のプロパティやEntityにまたがらない、単一のビジネスルール（ポリシー）を判定するドメインサービスを配置する。

## 命名規則

- クラス名サフィックス: `Policy`

## Springアノテーション

- `@Component`アノテーションを付与すること

## レイヤー間依存関係

- **許可するimport**: `config/`、`domain/`、`enumeration/`、`constant/`
- **禁止するimport**: `controller/`、`repository/`、`mapper/`、`entity/`、`dto/`、`service/`への依存
- **禁止するimport**: `controller/request/`や`controller/response/`のDTO

## メソッドシグネチャ

- 永続化されたデータの取得は行わず、呼び出し元（Service層）から渡された値のみで判定を行う（DBアクセスを行わない）
- 引数の型は、ドメインクラス（値オブジェクト）、Enumのみとする
- 返り値の型は、ドメインクラス（値オブジェクト）、Boolean、Integer（件数を返す時のみ）のみとする
