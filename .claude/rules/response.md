---
paths:
  - backend/**/controller/response/**
---

# Responseクラスのアーキテクチャルール

## 命名規則

- クラス名サフィックス: `Response`

## プロパティ
- ドメインクラス（値オブジェクト）、Modelクラス、Dtoクラス、Entityクラスは使用しない（依存性・責務の分離のため）

## ファクトリメソッド

- `static from(Model)`: Model→Responseの変換に使用するファクトリメソッドを定義すること
- `static of(...)`: 固定値や少数のパラメータから直接生成する場合に使用するファクトリメソッドを定義すること
- 上記いずれかのファクトリメソッドが定義されていないResponseクラスは違反
