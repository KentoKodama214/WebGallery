---
paths:
  - backend/**/controller/request/**
---

# Requestクラスのアーキテクチャルール

## バリデーションアノテーション

- リクエストクラスのプロパティにはバリデーションアノテーション（`@NotNull`、`@NotBlank`、`@Size`等）を付与すること
- バリデーションアノテーションが一つも存在しないRequestクラスは違反
