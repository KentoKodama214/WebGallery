---
paths:
  - backend/**/scheduler/**
---

# Schedulerクラスのアーキテクチャルール

`@Scheduled` による定期実行タスク（期限切れリフレッシュトークンの削除等）を配置する。
定期実行の有効化は `config/SchedulingConfig`（`@EnableScheduling`）で行う。

## 命名規則

- クラス名サフィックス: `Scheduler`
- 定期実行メソッドは処理内容を表す動詞で始める（例: `purgeExpiredRefreshTokens`）

## Springアノテーション

- `@Component` を付与すること
- コンストラクタインジェクションとし、`@RequiredArgsConstructor` を使用すること
- 定期実行メソッドに `@Scheduled` を付与すること
  - 実行時刻は `cron` 式で指定し、`zone = Consts.ZONE_ID_ASIA_TOKYO` を必ず指定する（JST固定・環境依存を排除）
  - `fixedRate` / `fixedDelay` は原則使用しない
- インスタンス個別にスケジュールを無効化できるよう、`@ConditionalOnProperty` を付与すること
  - `prefix = "app.scheduler"`、`name = "<機能名>-enabled"`、`matchIfMissing = true`（未設定時は有効）

## 責務

- ビジネスロジック・バリデーション・永続化処理を持たない
- Service層のメソッドを呼び出すだけの薄い層とする（可能ならメソッド参照で委譲する）
- 処理件数の集計・通知などの副次的処理は Service層・`event/` 側に置く

## レイヤー間依存関係

- **許可するimport**: `service/` のインターフェース、`helper/`（`SchedulerLock`）、`config/`、`enumeration/`、`constant/`、`domain/`
- **禁止するimport**: `controller/`、`repository/`、`mapper/`、`entity/`、`dto/`、`aggregate/`、`service/impl/` への直接依存
- **禁止するimport**: `controller/request/` や `controller/response/` のDTO
- 他の `Scheduler` クラスへの依存

## 多重起動防止（複数インスタンス構成）

AWS等で複数インスタンスを稼働させると、全インスタンスが同時刻に同じ定期実行メソッドを起動する。
そのため、定期実行メソッドの処理は **必ず `SchedulerLock#runIfLocked` でラップする**。

- 処理が冪等であってもラップする（ログ出力の一元化・実行インスタンスの一意化のため）
- `SchedulerLock`（`helper/`）は PostgreSQL のトランザクションレベルのアドバイザリーロック
  （`pg_try_advisory_xact_lock`）を用いて、ロックを取得できたインスタンスでのみ `task` を実行する
- ロックの取得と `task` の実行は同一トランザクションで行われ、ロックはトランザクション終了時に自動解放される
- ロックを取得できなかったインスタンスは待機せずスキップする

```java
@Scheduled(cron = "0 0 4 * * *", zone = Consts.ZONE_ID_ASIA_TOKYO)
public void purgeExpiredRefreshTokens() {
  schedulerLock.runIfLocked(
      SchedulerLockName.REFRESH_TOKEN_CLEANUP, authService::purgeExpiredRefreshTokens);
}
```

### ロック名（`enumeration/SchedulerLockName`）

- 定期実行処理ごとに要素を1つ追加する
- `lockKey`（`bigint`）はインスタンス間・再起動をまたいで安定させる必要があるため、
  一度割り当てた値は変更しない。既存の値と重複しない番号を採番する
- `displayName` はログ出力用の日本語名称

### ログ出力

- 開始・完了・スキップ・異常終了のライフサイクルログは `SchedulerLock` が
  `[定期実行] {displayName} 開始` 形式で一元的に出力する
- `Scheduler` クラス側および `task` では、これらのライフサイクルログを出力しない
  （業務固有の情報（処理件数など）が必要な場合のみ、呼び出す Service層で出力する）

## テスト

- 単体テスト（`@ExtendWith(MockitoExtension.class)`）を `scheduler/` パッケージに配置する
- `SchedulerLock` をモックし、`SchedulerLockName` の正しい要素とともに対象の Service層メソッドへ
  委譲していることを検証する
