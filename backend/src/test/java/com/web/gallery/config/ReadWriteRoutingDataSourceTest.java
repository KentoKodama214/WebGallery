package com.web.gallery.config;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class ReadWriteRoutingDataSourceTest {

  private ReadWriteRoutingDataSource routingDataSource;

  @Mock private DataSource primaryDataSource;

  @Mock private DataSource replicaDataSource;

  @BeforeEach
  void setUp() {
    routingDataSource = new ReadWriteRoutingDataSource();
    routingDataSource.setTargetDataSources(
        Map.of(
            ReadWriteRoutingDataSource.Role.PRIMARY, primaryDataSource,
            ReadWriteRoutingDataSource.Role.REPLICA, replicaDataSource));
    routingDataSource.setDefaultTargetDataSource(primaryDataSource);
    routingDataSource.afterPropertiesSet();
  }

  @AfterEach
  void tearDown() {
    TransactionSynchronizationManager.clear();
  }

  @Test
  @DisplayName("読み取り専用トランザクション中はリードレプリカへルーティングする")
  void readOnlyTrue_routesToReplica() throws SQLException {
    Connection replicaConnection = mock(Connection.class);
    when(replicaDataSource.getConnection()).thenReturn(replicaConnection);
    TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);

    Connection actual = routingDataSource.getConnection();

    assertSame(replicaConnection, actual);
    verify(replicaDataSource).getConnection();
    verify(primaryDataSource, never()).getConnection();
  }

  @Test
  @DisplayName("書き込みトランザクション中はプライマリへルーティングする")
  void readOnlyFalse_routesToPrimary() throws SQLException {
    Connection primaryConnection = mock(Connection.class);
    when(primaryDataSource.getConnection()).thenReturn(primaryConnection);
    TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);

    Connection actual = routingDataSource.getConnection();

    assertSame(primaryConnection, actual);
    verify(primaryDataSource).getConnection();
    verify(replicaDataSource, never()).getConnection();
  }

  @Test
  @DisplayName("トランザクション外ではプライマリへフェイルセーフする")
  void noActiveTransaction_routesToPrimary() throws SQLException {
    Connection primaryConnection = mock(Connection.class);
    when(primaryDataSource.getConnection()).thenReturn(primaryConnection);

    Connection actual = routingDataSource.getConnection();

    assertSame(primaryConnection, actual);
    verify(primaryDataSource).getConnection();
    verify(replicaDataSource, never()).getConnection();
  }
}
