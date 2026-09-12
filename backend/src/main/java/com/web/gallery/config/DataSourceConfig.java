package com.web.gallery.config;

import com.zaxxer.hikari.HikariDataSource;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

  private final DataSourceProperties dataSourceProperties;

  private final DataSourceReplicaConfig dataSourceReplicaConfig;

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
            driverClassName);

    String replicaUrl = dataSourceReplicaConfig.getUrl();
    if (!StringUtils.hasText(replicaUrl)) {
      return primary;
    }

    DataSource replica =
        buildTargetDataSource(
            replicaUrl,
            dataSourceProperties.determineUsername(),
            dataSourceProperties.determinePassword(),
            driverClassName);

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
   * 指定の接続情報から {@link HikariDataSource} を構築する
   *
   * <p>単体テストでMockitoのspyにより差し替え可能とするため {@code protected} とする（実DB接続無しで {@link #dataSource()}
   * の分岐・配線ロジックのみ検証するため）。
   *
   * @param url JDBC接続URL
   * @param username 接続ユーザー名
   * @param password 接続パスワード
   * @param driverClassName JDBCドライバクラス名
   * @return 構築した {@link HikariDataSource}
   */
  protected DataSource buildTargetDataSource(
      String url, String username, String password, String driverClassName) {
    return DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .url(url)
        .username(username)
        .password(password)
        .driverClassName(driverClassName)
        .build();
  }
}
