---
paths:
  - backend/src/test/**
  - "!backend/src/test/**/integration/**"
---

# ユニットテストのアーキテクチャルール

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ExtendWith(MockitoExtension.class)`でモック化した依存関係を使用
