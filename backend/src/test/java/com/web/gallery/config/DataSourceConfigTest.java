package com.web.gallery.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

class DataSourceConfigTest {

  private static DataSourceProperties dataSourceProperties() {
    DataSourceProperties properties = new DataSourceProperties();
    properties.setUrl("jdbc:postgresql://primary:5432/web_gallery");
    properties.setUsername("app");
    properties.setPassword("secret");
    return properties;
  }

  @Nested
  @DisplayName("リードレプリカ未設定の場合")
  class ReplicaNotConfigured {

    @Test
    @DisplayName("プライマリのDataSourceをそのまま返す")
    void nullUrl_returnsPrimaryDirectly() {
      DataSourceConfig config =
          spy(new DataSourceConfig(dataSourceProperties(), new DataSourceReplicaConfig(null)));
      DataSource mockPrimary = mock(DataSource.class);
      doReturn(mockPrimary)
          .when(config)
          .buildTargetDataSource(anyString(), anyString(), anyString(), anyString());

      DataSource actual = config.dataSource();

      assertSame(mockPrimary, actual);
      verify(config, times(1))
          .buildTargetDataSource(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("空文字でもプライマリのDataSourceをそのまま返す")
    void blankUrl_returnsPrimaryDirectly() {
      DataSourceConfig config =
          spy(new DataSourceConfig(dataSourceProperties(), new DataSourceReplicaConfig("")));
      DataSource mockPrimary = mock(DataSource.class);
      doReturn(mockPrimary)
          .when(config)
          .buildTargetDataSource(anyString(), anyString(), anyString(), anyString());

      DataSource actual = config.dataSource();

      assertSame(mockPrimary, actual);
    }
  }

  @Nested
  @DisplayName("リードレプリカ設定ありの場合")
  class ReplicaConfigured {

    @Test
    @DisplayName("LazyConnectionDataSourceProxyでラップしたルーティング用DataSourceを返す")
    void configuredUrl_wrapsWithLazyProxyAndRouting() {
      DataSourceProperties properties = dataSourceProperties();
      DataSourceReplicaConfig replicaConfig =
          new DataSourceReplicaConfig("jdbc:postgresql://replica:5432/web_gallery");
      DataSourceConfig config = spy(new DataSourceConfig(properties, replicaConfig));
      DataSource mockPrimary = mock(DataSource.class);
      DataSource mockReplica = mock(DataSource.class);
      doReturn(mockPrimary)
          .when(config)
          .buildTargetDataSource(
              properties.determineUrl(),
              properties.determineUsername(),
              properties.determinePassword(),
              properties.determineDriverClassName());
      doReturn(mockReplica)
          .when(config)
          .buildTargetDataSource(
              replicaConfig.getUrl(),
              properties.determineUsername(),
              properties.determinePassword(),
              properties.determineDriverClassName());

      DataSource actual = config.dataSource();

      assertInstanceOf(LazyConnectionDataSourceProxy.class, actual);
      DataSource wrapped = ((LazyConnectionDataSourceProxy) actual).getTargetDataSource();
      assertInstanceOf(ReadWriteRoutingDataSource.class, wrapped);
      ReadWriteRoutingDataSource routingDataSource = (ReadWriteRoutingDataSource) wrapped;
      assertSame(mockPrimary, routingDataSource.getResolvedDefaultDataSource());
      assertSame(
          mockPrimary,
          routingDataSource.getResolvedDataSources().get(ReadWriteRoutingDataSource.Role.PRIMARY));
      assertSame(
          mockReplica,
          routingDataSource.getResolvedDataSources().get(ReadWriteRoutingDataSource.Role.REPLICA));
    }
  }
}
