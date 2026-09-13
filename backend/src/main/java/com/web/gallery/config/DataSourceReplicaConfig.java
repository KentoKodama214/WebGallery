package com.web.gallery.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * リードレプリカ（AWS RDS）の接続URL・コネクションプール設定を保持するConfigクラス
 *
 * <p>このプロパティキー（{@code app.datasource.replica.url}）は、いずれの {@code application-*.yml} にも書かないこと。{@code
 * ${APP_DATASOURCE_REPLICA_URL:}} のように空文字デフォルトをYAMLへ書くと、
 * 未設定環境でもキー自体は存在してしまい、意図せずリードレプリカ用のDataSourceが生成されてしまう。 有効化する場合は、インフラ側の環境変数 {@code
 * APP_DATASOURCE_REPLICA_URL} のみを設定すること（Spring Bootのリラクゼーションバインディングにより {@code
 * app.datasource.replica.url} として認識される）。
 *
 * <p>{@link #maximumPoolSize} / {@link #minimumIdle} は {@code url} と異なりこのクラス単独では機能を有効化しないため（{@link
 * DataSourceConfig} は {@code url} 未設定時はリードレプリカを構築しない）、YAMLへのキー追加は問題ない。ただし現状はいずれの {@code
 * application-*.yml} にも書かず、環境変数（{@code APP_DATASOURCE_REPLICA_MAXIMUM_POOL_SIZE} / {@code
 * APP_DATASOURCE_REPLICA_MINIMUM_IDLE}）のみで上書きする運用とし、{@code url} と同じ設定方法に揃えている。未設定時は {@code
 * spring.datasource.hikari.*} の値（プライマリと同じ設定）がそのまま使われる。
 *
 * <p>ユーザー名・パスワードはプライマリと共用するため、このクラスでは保持しない（RDSリードレプリカは 通常マスターと同一の認証情報で接続できるため）。
 */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "app.datasource.replica")
public class DataSourceReplicaConfig {
  /** リードレプリカのJDBC接続URL。未設定の場合、リードレプリカ機能は無効化され、常にプライマリへ接続する */
  private final String url;

  /** リードレプリカ用コネクションプールの最大接続数。未設定時は {@code spring.datasource.hikari.maximum-pool-size} を使用する */
  private final Integer maximumPoolSize;

  /** リードレプリカ用コネクションプールの最小アイドル接続数。未設定時は {@code spring.datasource.hikari.minimum-idle} を使用する */
  private final Integer minimumIdle;
}
