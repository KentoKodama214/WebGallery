---
paths:
  - backend/src/test/**/integration/**
---

# 統合テストのアーキテクチャルール

## テスト種別

- `@SpringBootTest`と`@ActiveProfiles("test")`を使用
- `@Transactional`による自動ロールバック
- `@Sql("/sql/...")`アノテーションでテストフィクスチャデータを読み込み

## 命名規則

- クラス名に`IntegrationTest`サフィックスを付与すること

## 配置ルール

- 統合テストクラスは`integration/`サブディレクトリに配置すること
- `integration/`ディレクトリにあるのに`IntegrationTest`サフィックスがない、またはその逆のケースは違反
