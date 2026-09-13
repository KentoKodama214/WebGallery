package com.web.gallery.config;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.util.StringUtils;

/**
 * アプリケーションが利用する {@link DataSource} のBean定義クラス
 *
 * <p>{@link DataSourceReplicaConfig} でリードレプリカの接続URLが設定されていない場合は、{@code spring.datasource.*}
 * から構築したプライマリ用の {@link HikariDataSource} をそのまま公開する（既存環境と完全に同一の挙動）。 設定されている場合のみ、プライマリ・リードレプリカ双方を
 * {@link ReadWriteRoutingDataSource} でまとめ、{@link LazyConnectionDataSourceProxy} でラップして公開する。
 *
 * <p>{@code DataSource} Beanは常にちょうど1つになるよう構成する。{@code
 * DataSourceTransactionManagerAutoConfiguration} や {@code MybatisAutoConfiguration} は複数の {@code
 * DataSource} Beanが存在すると解決に失敗するため、プライマリ・リードレプリカを個別のBeanとして公開しない。
 *
 * <p>本クラスは {@code DataSource} Beanを独自に構築するため、Spring Boot標準の {@code spring.datasource.hikari.*}
 * 自動バインド（{@code DataSourceConfiguration.Hikari} が提供する仕組み）は適用されない。そのため {@link
 * #buildTargetDataSource} 内で {@link Binder} を使い、同じプロパティキーを {@link HikariDataSource}
 * インスタンスへ手動でバインドしている。プライマリ・リードレプリカは基本的に同じ {@code spring.datasource.hikari.*} 設定を共有するが、{@link
 * #applyReplicaPoolSizeOverrides} により {@link DataSourceReplicaConfig}
 * でリードレプリカのプールサイズ（最大接続数・最小アイドル接続数）のみ個別に上書きできる。
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

  /** プライマリ用コネクションプールの識別名（ログ・メトリクスでリードレプリカと区別するために使用） */
  private static final String PRIMARY_POOL_NAME = "primary";

  /** リードレプリカ用コネクションプールの識別名（ログ・メトリクスでプライマリと区別するために使用） */
  private static final String REPLICA_POOL_NAME = "replica";

  private final DataSourceProperties dataSourceProperties;

  private final DataSourceReplicaConfig dataSourceReplicaConfig;

  private final Environment environment;

  private final MeterRegistry meterRegistry;

  /**
   * アプリケーションが利用するDataSourceを構築する
   *
   * @return リードレプリカ未設定時はプライマリの {@link HikariDataSource}、設定時は {@link LazyConnectionDataSourceProxy}
   *     でラップしたルーティング用DataSource
   */
  @Bean
  public DataSource dataSource() {
    String driverClassName = dataSourceProperties.determineDriverClassName();
    DataSource primary =
        buildTargetDataSource(
            dataSourceProperties.determineUrl(),
            dataSourceProperties.determineUsername(),
            dataSourceProperties.determinePassword(),
            driverClassName,
            PRIMARY_POOL_NAME);

    String replicaUrl = dataSourceReplicaConfig.getUrl();
    if (!StringUtils.hasText(replicaUrl)) {
      return primary;
    }

    DataSource replica =
        buildTargetDataSource(
            replicaUrl,
            dataSourceProperties.determineUsername(),
            dataSourceProperties.determinePassword(),
            driverClassName,
            REPLICA_POOL_NAME);
    applyReplicaPoolSizeOverrides(replica);

    ReadWriteRoutingDataSource routingDataSource = new ReadWriteRoutingDataSource();
    routingDataSource.setTargetDataSources(
        Map.of(
            ReadWriteRoutingDataSource.Role.PRIMARY, primary,
            ReadWriteRoutingDataSource.Role.REPLICA, replica));
    routingDataSource.setDefaultTargetDataSource(primary);
    routingDataSource.afterPropertiesSet();

    return new LazyConnectionDataSourceProxy(routingDataSource);
  }

  /**
   * リードレプリカ用DataSourceのプールサイズを {@link DataSourceReplicaConfig} の値で上書きする
   *
   * <p>{@code app.datasource.replica.maximum-pool-size} / {@code
   * app.datasource.replica.minimum-idle} が設定されている場合のみ上書きし、未設定時は {@link #buildTargetDataSource}
   * で適用済みの {@code spring.datasource.hikari.*}（プライマリと同じ設定）をそのまま使う。
   *
   * @param replica リードレプリカ用DataSource（{@link HikariDataSource} 以外は何もしない）
   */
  private void applyReplicaPoolSizeOverrides(DataSource replica) {
    if (!(replica instanceof HikariDataSource hikariReplica)) {
      return;
    }

    Integer maximumPoolSizeOverride = dataSourceReplicaConfig.getMaximumPoolSize();
    if (maximumPoolSizeOverride != null) {
      hikariReplica.setMaximumPoolSize(maximumPoolSizeOverride);
    }

    Integer minimumIdleOverride = dataSourceReplicaConfig.getMinimumIdle();
    if (minimumIdleOverride != null) {
      hikariReplica.setMinimumIdle(minimumIdleOverride);
    }
  }

  /**
   * 指定の接続情報から {@link HikariDataSource} を構築する
   *
   * <p>単体テストでMockitoのspyにより差し替え可能とするため {@code protected} とする（実DB接続無しで {@link #dataSource()}
   * の分岐・配線ロジックのみ検証するため）。
   *
   * <p>{@code spring.datasource.hikari.*} のプロパティ（コネクション寿命・キープアライブ・リーク検知等）は自動バインドされないため、{@link
   * Binder} で明示的にバインドする。プールが未起動（{@code getConnection()} 未実行）の間は実DB接続は発生しない。
   *
   * <p>{@link MeterRegistry} を明示的に紐付けることで、{@code AbstractRoutingDataSource} 経由でも（Spring
   * Bootの自動メトリクス紐付けに依存せず）確実に{@code hikaricp.connections.*}系メトリクスが{@code pool}タグ（{@code
   * poolName}）付きで公開される。
   *
   * @param url JDBC接続URL
   * @param username 接続ユーザー名
   * @param password 接続パスワード
   * @param driverClassName JDBCドライバクラス名
   * @param poolName コネクションプールの識別名（ログ・メトリクスでプライマリ／リードレプリカを区別するために設定）
   * @return 構築した {@link HikariDataSource}
   */
  protected DataSource buildTargetDataSource(
      String url, String username, String password, String driverClassName, String poolName) {
    HikariDataSource dataSource =
        DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .url(url)
            .username(username)
            .password(password)
            .driverClassName(driverClassName)
            .build();
    dataSource.setPoolName(poolName);
    Binder.get(environment).bind("spring.datasource.hikari", Bindable.ofInstance(dataSource));
    dataSource.setMetricRegistry(meterRegistry);
    return dataSource;
  }
}
