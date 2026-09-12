package com.web.gallery.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 現在のトランザクションが読み取り専用かどうかで、プライマリ／リードレプリカのDataSourceを振り分けるルーティング用DataSource
 *
 * <p>本クラスは単独で {@code DataSource} Beanとして公開してはならない。{@link
 * org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy} で必ずラップして {@link
 * DataSourceConfig} 経由で公開すること。理由：{@code DataSourceTransactionManager} は実コネクションの取得（{@code
 * doBegin}）をトランザクション同期情報（読み取り専用フラグ）の確定より先に行うため、 本クラスを直接使うと {@link #determineCurrentLookupKey()}
 * 実行時点で読み取り専用フラグがまだ確定しておらず、 常にプライマリに固定されてしまう。{@code LazyConnectionDataSourceProxy}
 * で実接続の取得を最初のSQL発行直前まで 遅延させることで、フラグ確定後にルーティングできるようにする。
 *
 * <p><strong>レプリケーション遅延の注意：</strong>{@code @Transactional(readOnly = true)}
 * が付与されたメソッドはリードレプリカへルーティングされうる。リードレプリカへの反映は非同期であるため、 直前の書き込みトランザクションの結果を同一フロー内で即座に読み戻す必要がある処理には
 * {@code readOnly = true} を付与しないこと（プライマリに固定される）。
 */
public class ReadWriteRoutingDataSource extends AbstractRoutingDataSource {

  /** ルーティング先を表す識別子 */
  public enum Role {
    /** 書き込み・強一貫性読み取り用のプライマリDataSource */
    PRIMARY,
    /** 読み取り専用トランザクション用のリードレプリカDataSource */
    REPLICA
  }

  @Override
  protected Object determineCurrentLookupKey() {
    return TransactionSynchronizationManager.isCurrentTransactionReadOnly()
        ? Role.REPLICA
        : Role.PRIMARY;
  }
}
