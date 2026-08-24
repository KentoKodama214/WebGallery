---
paths:
  - backend/**/event/**
---

# Eventクラスのアーキテクチャルール

写真登録・削除などのドメインイベントと、それをハンドリングするリスナーを配置する。Service層のビジネスロジックから、通知やログ集計等の副次的な処理を疎結合にする。

## 命名規則

- イベントクラスサフィックス: `Event`
- リスナークラスサフィックス: `Listener`

## イベントクラス

- `record Xxx(型 xxx, ...)`として実装する
- プロパティは、ドメインクラス（値オブジェクト）のみとする

## リスナークラス

- `@Component`アノテーションを付与すること
- ハンドリングするメソッドには`@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`を付与し、トランザクションのコミット後にのみ実行されるようにすること

## レイヤー間依存関係

- **許可するimport**: `domain/`
- **禁止するimport**: `controller/`、`repository/`、`mapper/`、`entity/`、`dto/`、`service/`への依存
