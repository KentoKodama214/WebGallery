package com.web.gallery.infrastructure.persistence.mapper.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.enumeration.DirectionEnum;
import com.web.gallery.infrastructure.persistence.dto.photo.PortfolioPhotoDto;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioPhoto;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioPhotoCondition;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PortfolioPhotoMapperTest {
  @Autowired private PortfolioPhotoMapper portfolioPhotoMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioPhotoMapperTest.sql")
  class insertBulk {
    @Test
    @Order(1)
    @DisplayName("正常系：複数件を一括登録し、写真設定はjsonb型で保存されること")
    void insertBulk_success() {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      Integer actualCount =
          portfolioPhotoMapper.insertBulk(
              List.of(
                  PortfolioPhoto.builder()
                      .accountNo(1L)
                      .portfolioNo(2L)
                      .photoNo(2L)
                      .createdBy(1L)
                      .displayOrder(2)
                      .photoSettings(Map.of("theme", "テーマ21", "subTheme", "サブ"))
                      .build()));

      assertEquals(1, actualCount);
      Map<String, Object> actual =
          jdbcTemplate.queryForMap("SELECT * FROM photo.portfolio_photo WHERE id = 6");
      assertEquals(1L, actual.get("account_no"));
      assertEquals(2L, actual.get("portfolio_no"));
      assertEquals(2L, actual.get("photo_no"));
      assertEquals(1L, actual.get("created_by"));
      assertEquals(2, actual.get("display_order"));
      assertEquals(
          transactionNow,
          jdbcTemplate.queryForObject(
              "SELECT created_at FROM photo.portfolio_photo WHERE id = 6", OffsetDateTime.class));
      assertTrue(
          jdbcTemplate.queryForObject(
              "SELECT photo_settings = '{\"theme\": \"テーマ21\", \"subTheme\": \"サブ\"}'::jsonb"
                  + " FROM photo.portfolio_photo WHERE id = 6",
              Boolean.class));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioPhotoMapperTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・ポートフォリオ番号に該当する写真だけを削除すること")
    void delete_byAccountAndPortfolio() {
      Integer actualCount =
          portfolioPhotoMapper.delete(PortfolioPhotoCondition.byAccountAndPortfolio(1L, 1L));

      assertEquals(2, actualCount);
      assertEquals(
          3,
          jdbcTemplate.queryForObject("SELECT COUNT(*) FROM photo.portfolio_photo", Integer.class));
      // 他アカウントの同じポートフォリオ番号の写真は削除されない
      assertEquals(
          1,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_photo WHERE account_no = 2", Integer.class));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウント番号に該当する写真をすべて削除すること")
    void delete_byAccountNo() {
      Integer actualCount = portfolioPhotoMapper.delete(PortfolioPhotoCondition.byAccountNo(1L));

      assertEquals(4, actualCount);
      assertEquals(
          1,
          jdbcTemplate.queryForObject("SELECT COUNT(*) FROM photo.portfolio_photo", Integer.class));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：写真番号の条件で絞り込んで削除すること")
    void delete_byPhotoNo() {
      Integer actualCount =
          portfolioPhotoMapper.delete(
              PortfolioPhotoCondition.builder().accountNo(1L).photoNo(2L).build());

      assertEquals(1, actualCount);
    }

    @Test
    @Order(4)
    @DisplayName("異常系：抽出条件が1つも指定されていない場合、全件削除せず0件となること")
    void delete_noCondition() {
      Integer actualCount = portfolioPhotoMapper.delete(PortfolioPhotoCondition.builder().build());

      assertEquals(0, actualCount);
      assertEquals(
          5,
          jdbcTemplate.queryForObject("SELECT COUNT(*) FROM photo.portfolio_photo", Integer.class));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioPhotoMapperTest.sql")
  class selectList {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・ポートフォリオ番号で絞り込み、写真マスタの表示用の情報と結合して表示順に取得すること")
    void selectList_byAccountAndPortfolio() {
      List<PortfolioPhotoDto> actual =
          portfolioPhotoMapper.selectList(PortfolioPhotoCondition.byAccountAndPortfolio(1L, 1L));

      assertEquals(2, actual.size());
      PortfolioPhotoDto first = actual.get(0);
      assertEquals(1L, first.getAccountNo());
      assertEquals(1L, first.getPortfolioNo());
      assertEquals(2L, first.getPhotoNo());
      assertEquals(1, first.getDisplayOrder());
      assertEquals(Map.of("caption", "キャプション12"), first.getPhotoSettings());
      assertEquals("タイトル12", first.getPhotoJapaneseTitle());
      assertEquals("1/2-bbb.jpg", first.getImageFilePath());
      assertEquals(DirectionEnum.SQUARE, first.getDirectionKbn());
      PortfolioPhotoDto second = actual.get(1);
      assertEquals(1L, second.getPhotoNo());
      assertEquals(2, second.getDisplayOrder());
      assertEquals(Map.of(), second.getPhotoSettings());
      assertEquals(DirectionEnum.HORIZONTAL, second.getDirectionKbn());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウント番号の条件で絞り込むこと（他アカウントの写真と結合しない）")
    void selectList_byAccountNo() {
      List<PortfolioPhotoDto> actual =
          portfolioPhotoMapper.selectList(PortfolioPhotoCondition.byAccountNo(2L));

      assertEquals(1, actual.size());
      assertEquals("タイトル21", actual.get(0).getPhotoJapaneseTitle());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：写真番号の条件で絞り込むこと")
    void selectList_byPhotoNo() {
      List<PortfolioPhotoDto> actual =
          portfolioPhotoMapper.selectList(
              PortfolioPhotoCondition.builder().accountNo(1L).photoNo(1L).build());

      assertEquals(
          List.of(1L, 2L, 3L), actual.stream().map(PortfolioPhotoDto::getPortfolioNo).toList());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：該当する写真がない場合、0件となること")
    void selectList_none() {
      assertTrue(
          portfolioPhotoMapper
              .selectList(PortfolioPhotoCondition.byAccountAndPortfolio(3L, 1L))
              .isEmpty());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioPhotoMapperTest.sql")
  class countAvailablePhotos {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントが所有する未削除の写真だけを数えること（論理削除済みの写真3は数えない）")
    void countAvailablePhotos_excludesDeleted() {
      assertEquals(2, portfolioPhotoMapper.countAvailablePhotos(1L, List.of(1L, 2L, 3L)));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：他アカウントの写真番号は、所有者のアカウント番号でなければ数えないこと")
    void countAvailablePhotos_otherAccount() {
      // account_no=3は写真を持たないため、写真番号1を指定しても0件
      assertEquals(0, portfolioPhotoMapper.countAvailablePhotos(3L, List.of(1L)));
      assertEquals(1, portfolioPhotoMapper.countAvailablePhotos(2L, List.of(1L)));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：存在しない写真番号は数えないこと")
    void countAvailablePhotos_notExists() {
      assertEquals(1, portfolioPhotoMapper.countAvailablePhotos(1L, List.of(1L, 99L)));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioPhotoMapperTest.sql")
  class selectAvailablePhotos {
    /**
     * 検索結果の写真番号を取得する
     *
     * @param titlePattern タイトルのLIKEパターン
     * @param locationPattern 撮影場所の名前のLIKEパターン
     * @param tagPattern タグ名のLIKEパターン
     * @return 写真番号のリスト（検索結果の順）
     */
    private List<Long> photoNos(String titlePattern, String locationPattern, String tagPattern) {
      return portfolioPhotoMapper
          .selectAvailablePhotos(1L, titlePattern, locationPattern, tagPattern, 10, 0)
          .stream()
          .map(PortfolioPhotoDto::getPhotoNo)
          .toList();
    }

    @Test
    @Order(1)
    @DisplayName("正常系：条件が未指定の場合、本人の未削除の写真を撮影日時の新しい順に、表示に必要な項目とあわせて取得すること")
    void selectAvailablePhotos_noCondition() {
      List<PortfolioPhotoDto> actual =
          portfolioPhotoMapper.selectAvailablePhotos(1L, null, null, null, 10, 0);

      assertEquals(List.of(2L, 1L), actual.stream().map(PortfolioPhotoDto::getPhotoNo).toList());
      PortfolioPhotoDto first = actual.get(0);
      assertEquals("タイトル12", first.getPhotoJapaneseTitle());
      assertEquals("1/2-bbb.jpg", first.getImageFilePath());
      assertEquals(DirectionEnum.SQUARE, first.getDirectionKbn());
    }

    @Test
    @Order(2)
    @DisplayName("タイトルの条件で、日本語・英語のいずれかに大文字小文字を区別せず部分一致する写真に絞り込むこと")
    void selectAvailablePhotos_byTitle() {
      assertEquals(List.of(2L, 1L), photoNos("%タイトル1%", null, null));
      assertEquals(List.of(1L), photoNos("%TITLE11%", null, null));
    }

    @Test
    @Order(3)
    @DisplayName("撮影場所の条件で、表示名・管理名のいずれかに部分一致し、論理削除されていない撮影場所の写真に絞り込むこと")
    void selectAvailablePhotos_byLocation() {
      assertEquals(List.of(2L), photoNos(null, "%京都%", null));
      assertEquals(List.of(2L), photoNos(null, "%金閣寺\\_管理%", null));
      // 写真1の撮影場所（大阪城）は論理削除済み
      assertEquals(List.of(), photoNos(null, "%大阪%", null));
    }

    @Test
    @Order(4)
    @DisplayName("タグの条件で、日本語・英語のいずれかに部分一致するタグを持つ、本人の未削除の写真に絞り込むこと")
    void selectAvailablePhotos_byTag() {
      // 同じタグを持つ論理削除済みの写真3・他アカウントの写真は含めない
      assertEquals(List.of(1L), photoNos(null, null, "%紅葉%"));
      assertEquals(List.of(1L), photoNos(null, null, "%AUTUMN%"));
    }

    @Test
    @Order(5)
    @DisplayName("複数の条件を指定した場合、すべてに一致する写真に絞り込むこと（AND条件）")
    void selectAvailablePhotos_and() {
      assertEquals(List.of(), photoNos("%タイトル12%", null, "%紅葉%"));
      assertEquals(List.of(1L), photoNos("%タイトル11%", null, "%紅葉%"));
    }

    @Test
    @Order(6)
    @DisplayName("エスケープしたワイルドカード文字は、文字そのものとして一致させること")
    void selectAvailablePhotos_escapedWildcard() {
      assertEquals(List.of(), photoNos("%\\%%", null, null));
    }

    @Test
    @Order(7)
    @DisplayName("取得件数・取得開始位置を指定した場合、その範囲だけを取得すること")
    void selectAvailablePhotos_paging() {
      List<PortfolioPhotoDto> actual =
          portfolioPhotoMapper.selectAvailablePhotos(1L, null, null, null, 1, 1);

      assertEquals(List.of(1L), actual.stream().map(PortfolioPhotoDto::getPhotoNo).toList());
    }
  }
}
