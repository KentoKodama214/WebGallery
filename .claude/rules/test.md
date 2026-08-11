---
paths:
  - backend/src/test/**
---

# テストクラスのアーキテクチャルール

## テスト種別

- **ユニットテスト**: `@ExtendWith(MockitoExtension.class)`でモック化した依存関係を使用
- **統合テスト**: `@SpringBootTest`と`@ActiveProfiles("test")`を使用
  - `@Transactional`による自動ロールバック
  - `@Sql("/sql/...")`アノテーションでテストフィクスチャデータを読み込み

## 命名規則

- **ユニットテスト**: クラス名に`Test`サフィックスを付与すること
- **統合テスト**: クラス名に`IntegrationTest`サフィックスを付与すること

## 配置ルール

- 統合テストクラスは`integration/`サブディレクトリに配置すること
- `integration/`ディレクトリにあるのに`IntegrationTest`サフィックスがない、またはその逆のケースは違反
