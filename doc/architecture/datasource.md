# データソース構成（プライマリ／リードレプリカ）

## 概要

AWS RDSのリードレプリカを導入するかどうかに関わらず同じコードで動作するよう、DataSource層をあらかじめ両対応にしている。振り分けはService層の`@Transactional(readOnly = true)`を自動検知して行うため、リードレプリカを導入する際もService層のコード変更は不要。

## 構成

`config/DataSourceConfig`が公開する`DataSource` Beanは常にちょうど1つ。

- リードレプリカ未設定（`app.datasource.replica.url`未設定）: `spring.datasource.*`から構築した`HikariDataSource`をそのまま公開する。既存環境（ローカル・development・test・現状のprod）と完全に同一の挙動
- リードレプリカ設定あり: `config/ReadWriteRoutingDataSource`（`AbstractRoutingDataSource`のサブクラス）でプライマリ・リードレプリカの`HikariDataSource`をまとめ、`LazyConnectionDataSourceProxy`でラップして公開する

```
Service (@Transactional(readOnly = true) 有無)
  -> DataSourceConfig#dataSource()
    -> LazyConnectionDataSourceProxy（リードレプリカ設定時のみ）
      -> ReadWriteRoutingDataSource
        -> primary: HikariDataSource（spring.datasource.*）
        -> replica: HikariDataSource（app.datasource.replica.url）
```

DataSource Beanを常に1つに保っているのは、`DataSourceTransactionManagerAutoConfiguration`や`MybatisAutoConfiguration`が複数の`DataSource` Beanが存在すると解決に失敗するため。

## `LazyConnectionDataSourceProxy`が必須な理由

`DataSourceTransactionManager`は実コネクションの取得（`doBegin`）を、トランザクション同期情報（読み取り専用フラグ）の確定より先に行う。そのため`ReadWriteRoutingDataSource`を素で`DataSource` Beanとして公開すると、`determineCurrentLookupKey()`実行時点でまだ読み取り専用フラグが確定しておらず、常にプライマリに固定されてしまう。`LazyConnectionDataSourceProxy`で実接続の取得を最初のSQL発行直前まで遅延させることで、フラグ確定後に正しくルーティングできるようにしている。

## リードレプリカの有効化手順

インフラ側で環境変数`APP_DATASOURCE_REPLICA_URL`（`jdbc:postgresql://...`形式）を設定するのみ。`application-prod.yml`にキーを追加する必要はなく、追加してはならない。

`${APP_DATASOURCE_REPLICA_URL:}`のように空文字デフォルトをYAMLへ書くと、未設定環境でも`app.datasource.replica.url`というプロパティキー自体は存在してしまい、意図せずリードレプリカ用DataSourceが生成されてしまう。この非対称性（環境変数が実際に設定された場合のみプロパティが存在する）を意図的に利用しているため、YAML側にキーを追加しないこと。

username/passwordはプライマリと共用する（RDSリードレプリカは通常マスターと同一の認証情報で接続できるため）。

本番プロファイルでは`config/ProdConfigValidationRunner`が起動時にリードレプリカURLの妥当性（PostgreSQL接続URL形式であること、プライマリと同一URLでないこと）を検証し、不正な場合は起動を失敗させる。

## レプリケーション遅延の運用ルール

リードレプリカへの反映は非同期のため、書き込み直後に自分自身が書いた内容を同一フロー内で即座に読み戻す必要がある処理には`@Transactional(readOnly = true)`を付与しないこと（プライマリに固定される）。
