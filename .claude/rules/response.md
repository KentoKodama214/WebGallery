---
paths:
  - backend/**/controller/response/**
---

# Responseクラスのアーキテクチャルール

## ファクトリメソッド

- `static from(Model)`: Model→Responseの変換に使用するファクトリメソッドを定義すること
- `static of(...)`: 固定値や少数のパラメータから直接生成する場合に使用するファクトリメソッドを定義すること
- 上記いずれかのファクトリメソッドが定義されていないResponseクラスは違反
