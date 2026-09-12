package com.web.gallery.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Country;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IpGeoLocation;
import com.web.gallery.domain.common.Referer;
import com.web.gallery.domain.common.Region;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.entity.PhotoViewLog;
import com.web.gallery.model.PhotoViewLogModel;
import com.web.gallery.repository.impl.PhotoViewLogRepositoryImpl;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class PhotoViewLogRepositoryImplIntegrationTest {
  @Autowired private PhotoViewLogRepositoryImpl photoViewLogRepositoryImpl;

  @Autowired private JdbcTemplate jdbcTemplate;

  /**
   * saveメソッドはREQUIRES_NEWで独立した別コネクションのトランザクションとして書き込むため、
   * フィクスチャ（{@code @Sql}）で投入したaccount・photo_mst行が本テストのトランザクション内で未コミットのままだと、
   * 外部キー制約の検証がその行のコミットを待ち続けて自己デッドロックする。 そのため、フィクスチャ投入後にここで一度物理コミットしてから新しいテスト用トランザクションを開始する
   */
  @BeforeEach
  void commitFixtures() {
    TestTransaction.flagForCommit();
    TestTransaction.end();
    TestTransaction.start();
  }

  /**
   * commitFixturesで物理コミットしたフィクスチャ・テスト結果が他のテストクラスへ残留しないよう、 テスト終了後に明示的にTRUNCATE（CASCADE）して物理コミットする
   */
  @AfterEach
  void cleanUp() {
    TestTransaction.end();
    TestTransaction.start();
    jdbcTemplate.execute(
        """
					TRUNCATE TABLE
						photo.photo_favorite,
						photo.photo_tag_mst,
						photo.photo_mst,
						common.refresh_token,
						common.location_mst,
						common.account,
						common.kbn_mst
					CASCADE
					""");
    TestTransaction.flagForCommit();
    TestTransaction.end();
  }

  private List<PhotoViewLog> getByPhotoAccountNo(Long photoAccountNo) {
    return jdbcTemplate.query(
        "SELECT * FROM photo.photo_view_log WHERE photo_account_no = ? ORDER BY"
            + " photo_view_log_no",
        (rs, rowNum) ->
            PhotoViewLog.builder()
                .photoViewLogNo(rs.getLong("photo_view_log_no"))
                .photoAccountNo(rs.getLong("photo_account_no"))
                .photoNo(rs.getLong("photo_no"))
                .referer(rs.getString("referer"))
                .ipAddress(rs.getString("ip_address"))
                .country(rs.getString("country"))
                .region(rs.getString("region"))
                .createdBy(rs.getLong("created_by"))
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .build(),
        photoAccountNo);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/PhotoViewLogRepositoryImplIntegrationTest.sql")
  class save {
    @Test
    @Order(1)
    @DisplayName("正常系：写真詳細閲覧ログを保存する")
    void save_success() {
      PhotoViewLogModel model =
          PhotoViewLogModel.builder()
              .photoAccountNo(new AccountNo(1L))
              .photoNo(new PhotoNo(1L))
              .referer(new Referer("https://example.com/photo_list"))
              .ipAddress(new IpAddress("203.0.113.1"))
              .geoLocation(new IpGeoLocation(new Country("JP"), new Region("Osaka")))
              .build();

      photoViewLogRepositoryImpl.save(model);

      List<PhotoViewLog> actual = getByPhotoAccountNo(1L);
      assertEquals(1, actual.size());
      assertEquals(1L, actual.getFirst().getPhotoNo());
      assertEquals("https://example.com/photo_list", actual.getFirst().getReferer());
      assertEquals("203.0.113.1", actual.getFirst().getIpAddress());
      assertEquals("JP", actual.getFirst().getCountry());
      assertEquals("Osaka", actual.getFirst().getRegion());
      // created_byはphoto_account_noと同一
      assertEquals(1L, actual.getFirst().getCreatedBy());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：国・地域・リファラが未解決の場合、空文字で保存する（NOT NULL制約のため）")
    void save_success_with_empty_values() {
      PhotoViewLogModel model =
          PhotoViewLogModel.builder()
              .photoAccountNo(new AccountNo(1L))
              .photoNo(new PhotoNo(1L))
              .referer(new Referer(""))
              .ipAddress(new IpAddress("203.0.113.1"))
              .geoLocation(IpGeoLocation.empty())
              .build();

      photoViewLogRepositoryImpl.save(model);

      List<PhotoViewLog> actual = getByPhotoAccountNo(1L);
      assertEquals(1, actual.size());
      assertEquals("", actual.getFirst().getReferer());
      assertEquals("", actual.getFirst().getCountry());
      assertEquals("", actual.getFirst().getRegion());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/PhotoViewLogRepositoryImplIntegrationTest.sql")
  class deleteByPhotoAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：写真アカウント番号に該当する閲覧ログを削除する")
    void deleteByPhotoAccountNo_success() {
      // フィクスチャでphoto_account_no=2の閲覧ログが1件存在する
      assertEquals(1, getByPhotoAccountNo(2L).size());

      photoViewLogRepositoryImpl.deleteByPhotoAccountNo(new AccountNo(2L));

      assertTrue(getByPhotoAccountNo(2L).isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：該当する閲覧ログが存在しない場合もエラーにならない")
    void deleteByPhotoAccountNo_no_log() {
      assertDoesNotThrow(
          () -> photoViewLogRepositoryImpl.deleteByPhotoAccountNo(new AccountNo(999L)));
    }
  }
}
