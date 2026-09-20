package com.web.gallery.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class GeoIpReaderConfigTest {

  @Mock private GeoIpConfig geoIpConfig;

  @TempDir private Path tempDir;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class geoIpDatabaseReader {
    @Test
    @Order(1)
    @DisplayName("異常系：データベースパスが未設定（空文字）の場合、nullを返すこと")
    void geoIpDatabaseReader_blankPath() {
      doReturn("").when(geoIpConfig).getDatabasePath();

      GeoIpReaderConfig geoIpReaderConfig = new GeoIpReaderConfig(geoIpConfig);

      assertNull(geoIpReaderConfig.geoIpDatabaseReader());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：データベースパスがnullの場合、nullを返すこと")
    void geoIpDatabaseReader_nullPath() {
      doReturn(null).when(geoIpConfig).getDatabasePath();

      GeoIpReaderConfig geoIpReaderConfig = new GeoIpReaderConfig(geoIpConfig);

      assertNull(geoIpReaderConfig.geoIpDatabaseReader());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：データベースファイルが存在しない場合、nullを返すこと")
    void geoIpDatabaseReader_fileNotFound() {
      doReturn(tempDir.resolve("not-exist.mmdb").toString()).when(geoIpConfig).getDatabasePath();

      GeoIpReaderConfig geoIpReaderConfig = new GeoIpReaderConfig(geoIpConfig);

      assertNull(geoIpReaderConfig.geoIpDatabaseReader());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：データベースファイルが破損している場合、nullを返すこと")
    void geoIpDatabaseReader_corruptedFile() throws IOException {
      Path corrupted = tempDir.resolve("corrupted.mmdb");
      Files.writeString(corrupted, "not a valid mmdb file");
      doReturn(corrupted.toString()).when(geoIpConfig).getDatabasePath();

      GeoIpReaderConfig geoIpReaderConfig = new GeoIpReaderConfig(geoIpConfig);

      assertNull(geoIpReaderConfig.geoIpDatabaseReader());
    }
  }
}
