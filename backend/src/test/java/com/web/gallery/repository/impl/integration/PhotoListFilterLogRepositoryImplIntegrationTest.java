package com.web.gallery.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Country;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IpGeoLocation;
import com.web.gallery.domain.common.Referer;
import com.web.gallery.domain.common.Region;
import com.web.gallery.domain.photo.IsFavoriteOnly;
import com.web.gallery.entity.PhotoListFilterLog;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import com.web.gallery.model.PhotoListFilterLogModel;
import com.web.gallery.repository.impl.PhotoListFilterLogRepositoryImpl;
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
public class PhotoListFilterLogRepositoryImplIntegrationTest {
  @Autowired private PhotoListFilterLogRepositoryImpl photoListFilterLogRepositoryImpl;

  @Autowired private JdbcTemplate jdbcTemplate;

  /**
   * saveメソッドはREQUIRES_NEWで独立した別コネクションのトランザクションとして書き込むため、
   * フィクスチャ（{@code @Sql}）で投入したaccount行が本テストのトランザクション内で未コミットのままだと、
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

  private List<PhotoListFilterLog> getByPhotoAccountNo(Long photoAccountNo) {
    return jdbcTemplate.query(
        "SELECT * FROM photo.photo_list_filter_log WHERE photo_account_no = ? ORDER BY"
            + " photo_list_filter_log_no",
        (rs, rowNum) ->
            PhotoListFilterLog.builder()
                .photoListFilterLogNo(rs.getLong("photo_list_filter_log_no"))
                .photoAccountNo(rs.getLong("photo_account_no"))
                .accountNo(rs.getLong("account_no"))
                .directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
                .isFavorite(rs.getBoolean("is_favorite"))
                .tagList(rs.getString("tag_list"))
                .sortBy(SortPhotoEnum.getOrDefault(rs.getString("sort_by")))
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
  @Sql("/sql/repository/PhotoListFilterLogRepositoryImplIntegrationTest.sql")
  class save {
    @Test
    @Order(1)
    @DisplayName("正常系：絞り込み・並び替えログを保存する（区分値のenumキャストを含む）")
    void save_success() {
      PhotoListFilterLogModel model =
          PhotoListFilterLogModel.builder()
              .photoAccountNo(new AccountNo(1L))
              .accountNo(new AccountNo(2L))
              .directionKbn(DirectionEnum.HORIZONTAL)
              .isFavoriteOnly(new IsFavoriteOnly(false))
              .tagList("山,川")
              .sortBy(SortPhotoEnum.SEASON)
              .referer(new Referer("https://example.com/photo_list"))
              .ipAddress(new IpAddress("203.0.113.1"))
              .geoLocation(new IpGeoLocation(new Country("JP"), new Region("Tokyo")))
              .build();

      photoListFilterLogRepositoryImpl.save(model);

      List<PhotoListFilterLog> actual = getByPhotoAccountNo(1L);
      assertEquals(1, actual.size());
      assertEquals(DirectionEnum.HORIZONTAL, actual.getFirst().getDirectionKbn());
      assertFalse(actual.getFirst().getIsFavorite());
      assertEquals("山,川", actual.getFirst().getTagList());
      assertEquals(SortPhotoEnum.SEASON, actual.getFirst().getSortBy());
      assertEquals("https://example.com/photo_list", actual.getFirst().getReferer());
      assertEquals("203.0.113.1", actual.getFirst().getIpAddress());
      assertEquals("JP", actual.getFirst().getCountry());
      assertEquals("Tokyo", actual.getFirst().getRegion());
      // ログイン中の閲覧者のアカウント番号
      assertEquals(2L, actual.getFirst().getAccountNo());
      // created_byはphoto_account_noと同一
      assertEquals(1L, actual.getFirst().getCreatedBy());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：国・地域・リファラが未解決の場合、空文字で保存する（NOT NULL制約のため）")
    void save_success_with_empty_values() {
      PhotoListFilterLogModel model =
          PhotoListFilterLogModel.builder()
              .photoAccountNo(new AccountNo(1L))
              .directionKbn(DirectionEnum.NONE)
              .isFavoriteOnly(new IsFavoriteOnly(false))
              .tagList("")
              .sortBy(SortPhotoEnum.PHOTO_AT)
              .referer(new Referer(""))
              .ipAddress(new IpAddress("203.0.113.1"))
              .geoLocation(IpGeoLocation.empty())
              .build();

      photoListFilterLogRepositoryImpl.save(model);

      List<PhotoListFilterLog> actual = getByPhotoAccountNo(1L);
      assertEquals(1, actual.size());
      assertEquals(DirectionEnum.NONE, actual.getFirst().getDirectionKbn());
      assertEquals(SortPhotoEnum.PHOTO_AT, actual.getFirst().getSortBy());
      assertEquals("", actual.getFirst().getReferer());
      assertEquals("", actual.getFirst().getCountry());
      assertEquals("", actual.getFirst().getRegion());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：未ログイン（accountNoがnull）の場合、account_noを0で保存する（NOT NULL制約のため）")
    void save_success_when_not_logged_in() {
      PhotoListFilterLogModel model =
          PhotoListFilterLogModel.builder()
              .photoAccountNo(new AccountNo(1L))
              .accountNo(null)
              .directionKbn(DirectionEnum.NONE)
              .isFavoriteOnly(new IsFavoriteOnly(false))
              .tagList("")
              .sortBy(SortPhotoEnum.PHOTO_AT)
              .referer(new Referer(""))
              .ipAddress(new IpAddress("203.0.113.1"))
              .geoLocation(IpGeoLocation.empty())
              .build();

      photoListFilterLogRepositoryImpl.save(model);

      List<PhotoListFilterLog> actual = getByPhotoAccountNo(1L);
      assertEquals(1, actual.size());
      assertEquals(0L, actual.getFirst().getAccountNo());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/PhotoListFilterLogRepositoryImplIntegrationTest.sql")
  class deleteByPhotoAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：写真アカウント番号に該当する絞り込みログを削除する")
    void deleteByPhotoAccountNo_success() {
      // フィクスチャでphoto_account_no=2の絞り込みログが1件存在する
      assertEquals(1, getByPhotoAccountNo(2L).size());

      photoListFilterLogRepositoryImpl.deleteByPhotoAccountNo(new AccountNo(2L));

      assertTrue(getByPhotoAccountNo(2L).isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：該当する絞り込みログが存在しない場合もエラーにならない")
    void deleteByPhotoAccountNo_no_log() {
      assertDoesNotThrow(
          () -> photoListFilterLogRepositoryImpl.deleteByPhotoAccountNo(new AccountNo(999L)));
    }
  }
}
