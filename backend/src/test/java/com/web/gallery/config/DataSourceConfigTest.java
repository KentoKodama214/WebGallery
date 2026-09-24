package com.web.gallery.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class DataSourceConfigTest {

  private static DataSourceProperties dataSourceProperties() {
    DataSourceProperties properties = new DataSourceProperties();
    properties.setUrl("jdbc:postgresql://primary:5432/web_gallery");
    properties.setUsername("app");
    properties.setPassword("secret");
    return properties;
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class dataSource {
    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("リードレプリカ未設定の場合")
    class replicaNotConfigured {

      @Test
      @Order(1)
      @DisplayName("プライマリのDataSourceをそのまま返す")
      void nullUrl_returnsPrimaryDirectly() {
        DataSourceConfig config =
            spy(
                new DataSourceConfig(
                    dataSourceProperties(),
                    new DataSourceReplicaConfig(null, null, null),
                    new MockEnvironment(),
                    new SimpleMeterRegistry()));
        DataSource mockPrimary = mock(DataSource.class);
        doReturn(mockPrimary)
            .when(config)
            .buildTargetDataSource(anyString(), anyString(), anyString(), anyString(), anyString());

        DataSource actual = config.dataSource();

        assertSame(mockPrimary, actual);
        verify(config, times(1))
            .buildTargetDataSource(anyString(), anyString(), anyString(), anyString(), anyString());
      }

      @Test
      @Order(2)
      @DisplayName("空文字でもプライマリのDataSourceをそのまま返す")
      void blankUrl_returnsPrimaryDirectly() {
        DataSourceConfig config =
            spy(
                new DataSourceConfig(
                    dataSourceProperties(),
                    new DataSourceReplicaConfig("", null, null),
                    new MockEnvironment(),
                    new SimpleMeterRegistry()));
        DataSource mockPrimary = mock(DataSource.class);
        doReturn(mockPrimary)
            .when(config)
            .buildTargetDataSource(anyString(), anyString(), anyString(), anyString(), anyString());

        DataSource actual = config.dataSource();

        assertSame(mockPrimary, actual);
      }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("リードレプリカ設定ありの場合")
    class replicaConfigured {

      @Test
      @Order(1)
      @DisplayName("LazyConnectionDataSourceProxyでラップしたルーティング用DataSourceを返す")
      void configuredUrl_wrapsWithLazyProxyAndRouting() {
        DataSourceProperties properties = dataSourceProperties();
        DataSourceReplicaConfig replicaConfig =
            new DataSourceReplicaConfig("jdbc:postgresql://replica:5432/web_gallery", null, null);
        DataSourceConfig config =
            spy(
                new DataSourceConfig(
                    properties, replicaConfig, new MockEnvironment(), new SimpleMeterRegistry()));
        DataSource mockPrimary = mock(DataSource.class);
        DataSource mockReplica = mock(DataSource.class);
        doReturn(mockPrimary)
            .when(config)
            .buildTargetDataSource(
                properties.determineUrl(),
                properties.determineUsername(),
                properties.determinePassword(),
                properties.determineDriverClassName(),
                "primary");
        doReturn(mockReplica)
            .when(config)
            .buildTargetDataSource(
                replicaConfig.getUrl(),
                properties.determineUsername(),
                properties.determinePassword(),
                properties.determineDriverClassName(),
                "replica");

        DataSource actual = config.dataSource();

        assertInstanceOf(LazyConnectionDataSourceProxy.class, actual);
        DataSource wrapped = ((LazyConnectionDataSourceProxy) actual).getTargetDataSource();
        assertInstanceOf(ReadWriteRoutingDataSource.class, wrapped);
        ReadWriteRoutingDataSource routingDataSource = (ReadWriteRoutingDataSource) wrapped;
        assertSame(mockPrimary, routingDataSource.getResolvedDefaultDataSource());
        assertSame(
            mockPrimary,
            routingDataSource
                .getResolvedDataSources()
                .get(ReadWriteRoutingDataSource.Role.PRIMARY));
        assertSame(
            mockReplica,
            routingDataSource
                .getResolvedDataSources()
                .get(ReadWriteRoutingDataSource.Role.REPLICA));
      }
    }

    @Nested
    @Order(3)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("リードレプリカのプールサイズ上書きの場合")
    class replicaPoolSizeOverride {

      private static HikariDataSource replicaOf(DataSource dataSource) {
        ReadWriteRoutingDataSource routingDataSource =
            (ReadWriteRoutingDataSource)
                ((LazyConnectionDataSourceProxy) dataSource).getTargetDataSource();
        return (HikariDataSource)
            routingDataSource.getResolvedDataSources().get(ReadWriteRoutingDataSource.Role.REPLICA);
      }

      @Test
      @Order(1)
      @DisplayName("app.datasource.replica.*が設定されている場合はプライマリと異なるプールサイズになる")
      void overridesAppliedWhenConfigured() {
        DataSourceReplicaConfig replicaConfig =
            new DataSourceReplicaConfig("jdbc:postgresql://replica:5432/web_gallery", 5, 2);
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.datasource.hikari.maximum-pool-size", "20");
        environment.setProperty("spring.datasource.hikari.minimum-idle", "10");
        DataSourceConfig config =
            new DataSourceConfig(
                dataSourceProperties(), replicaConfig, environment, new SimpleMeterRegistry());

        HikariDataSource replica = replicaOf(config.dataSource());

        assertEquals(5, replica.getMaximumPoolSize());
        assertEquals(2, replica.getMinimumIdle());
      }

      @Test
      @Order(2)
      @DisplayName("app.datasource.replica.*が未設定の場合はプライマリと同じプールサイズになる")
      void fallsBackToPrimarySettingsWhenNotConfigured() {
        DataSourceReplicaConfig replicaConfig =
            new DataSourceReplicaConfig("jdbc:postgresql://replica:5432/web_gallery", null, null);
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.datasource.hikari.maximum-pool-size", "20");
        environment.setProperty("spring.datasource.hikari.minimum-idle", "10");
        DataSourceConfig config =
            new DataSourceConfig(
                dataSourceProperties(), replicaConfig, environment, new SimpleMeterRegistry());

        HikariDataSource replica = replicaOf(config.dataSource());

        assertEquals(20, replica.getMaximumPoolSize());
        assertEquals(10, replica.getMinimumIdle());
      }
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class buildTargetDataSource {

    @Test
    @Order(1)
    @DisplayName("プール名とspring.datasource.hikari.*のプロパティがHikariDataSourceへ反映される")
    void appliesPoolNameAndHikariProperties() {
      MockEnvironment environment = new MockEnvironment();
      environment.setProperty("spring.datasource.hikari.max-lifetime", "123456");
      environment.setProperty("spring.datasource.hikari.keepalive-time", "60000");
      environment.setProperty("spring.datasource.hikari.leak-detection-threshold", "60000");
      DataSourceConfig config =
          new DataSourceConfig(
              dataSourceProperties(),
              new DataSourceReplicaConfig(null, null, null),
              environment,
              new SimpleMeterRegistry());

      DataSource actual =
          config.buildTargetDataSource(
              "jdbc:postgresql://primary:5432/web_gallery",
              "app",
              "secret",
              "org.postgresql.Driver",
              "primary");

      HikariDataSource hikariDataSource = assertInstanceOf(HikariDataSource.class, actual);
      assertEquals("primary", hikariDataSource.getPoolName());
      assertEquals(123456L, hikariDataSource.getMaxLifetime());
      assertEquals(60000L, hikariDataSource.getKeepaliveTime());
      assertEquals(60000L, hikariDataSource.getLeakDetectionThreshold());
    }

    @Test
    @Order(2)
    @DisplayName("指定したMeterRegistryがHikariDataSourceに紐付けられる")
    void appliesMeterRegistry() {
      SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
      DataSourceConfig config =
          new DataSourceConfig(
              dataSourceProperties(),
              new DataSourceReplicaConfig(null, null, null),
              new MockEnvironment(),
              meterRegistry);

      DataSource actual =
          config.buildTargetDataSource(
              "jdbc:postgresql://primary:5432/web_gallery",
              "app",
              "secret",
              "org.postgresql.Driver",
              "primary");

      HikariDataSource hikariDataSource = assertInstanceOf(HikariDataSource.class, actual);
      assertSame(meterRegistry, hikariDataSource.getMetricRegistry());
    }
  }
}
