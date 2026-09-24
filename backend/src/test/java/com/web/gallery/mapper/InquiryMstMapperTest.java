package com.web.gallery.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.dto.InquiryDetailDto;
import com.web.gallery.dto.InquiryDto;
import com.web.gallery.entity.inquiry.InquiryMst;
import com.web.gallery.entity.inquiry.InquiryMstCondition;
import com.web.gallery.entity.inquiry.InquiryMstUpdateTarget;
import com.web.gallery.enumeration.InquiryStatusEnum;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
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
public class InquiryMstMapperTest {
  @Autowired private InquiryMstMapper inquiryMstMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/InquiryMstMapperTest.sql")
  class insert {
    @Test
    @Order(1)
    @DisplayName("正常系：登録成功")
    void insert_success() {
      InquiryMst insertInquiryMst =
          InquiryMst.builder()
              .accountNo(1L)
              .inquiryNo(3L)
              .createdBy(1L)
              .updatedBy(1L)
              .subject("新規の件名")
              .body("新規の本文")
              .statusKbn(InquiryStatusEnum.UNREPLIED)
              .isReadByUser(true)
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actualCount = inquiryMstMapper.insert(insertInquiryMst);
      assertEquals(1, actualCount);

      List<InquiryMst> actualData =
          jdbcTemplate.query(
              "SELECT * FROM common.inquiry_mst WHERE id = 4",
              (rs, rowNum) ->
                  InquiryMst.builder()
                      .id(rs.getLong("id"))
                      .accountNo(rs.getLong("account_no"))
                      .inquiryNo(rs.getLong("inquiry_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .updatedBy(rs.getLong("updated_by"))
                      .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                      .subject(rs.getString("subject"))
                      .body(rs.getString("body"))
                      .statusKbn(InquiryStatusEnum.getOrDefault(rs.getString("status_kbn")))
                      .isReadByUser(rs.getBoolean("is_read_by_user"))
                      .build());

      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(3L, actualData.getFirst().getInquiryNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
      assertEquals(1L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertEquals("新規の件名", actualData.getFirst().getSubject());
      assertEquals("新規の本文", actualData.getFirst().getBody());
      assertEquals(InquiryStatusEnum.UNREPLIED, actualData.getFirst().getStatusKbn());
      assertTrue(actualData.getFirst().getIsReadByUser());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/InquiryMstMapperTest.sql")
  class update {
    private List<InquiryMst> getInquiryMstList(String condition) {
      return jdbcTemplate.query(
          "SELECT * FROM common.inquiry_mst WHERE " + condition,
          (rs, rowNum) ->
              InquiryMst.builder()
                  .id(rs.getLong("id"))
                  .accountNo(rs.getLong("account_no"))
                  .inquiryNo(rs.getLong("inquiry_no"))
                  .createdBy(rs.getLong("created_by"))
                  .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                  .updatedBy(rs.getLong("updated_by"))
                  .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                  .subject(rs.getString("subject"))
                  .body(rs.getString("body"))
                  .statusKbn(InquiryStatusEnum.getOrDefault(rs.getString("status_kbn")))
                  .isReadByUser(rs.getBoolean("is_read_by_user"))
                  .build());
    }

    @Test
    @Order(1)
    @DisplayName("正常系：IDでのupdate")
    void update_by_id() {
      InquiryMstCondition condition = InquiryMstCondition.builder().id(1L).build();
      InquiryMstUpdateTarget target =
          InquiryMstUpdateTarget.builder().updatedBy(9L).isReadByUser(false).build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = inquiryMstMapper.update(condition, target);
      assertEquals(1, actual);

      List<InquiryMst> actualData = getInquiryMstList("id=1");
      assertEquals(1, actualData.size());
      assertEquals(9L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsReadByUser());
      // statusKbnは更新対象外のため元の値を維持する
      assertEquals(InquiryStatusEnum.UNREPLIED, actualData.getFirst().getStatusKbn());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウント番号でのupdate")
    void update_by_accountNo() {
      InquiryMstCondition condition = InquiryMstCondition.builder().accountNo(2L).build();
      InquiryMstUpdateTarget target = InquiryMstUpdateTarget.builder().updatedBy(9L).build();

      Integer actual = inquiryMstMapper.update(condition, target);
      assertEquals(1, actual);

      List<InquiryMst> actualData = getInquiryMstList("account_no=2");
      assertEquals(1, actualData.size());
      assertEquals(9L, actualData.getFirst().getUpdatedBy());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：お問い合わせ番号でのupdate")
    void update_by_inquiryNo() {
      InquiryMstCondition condition = InquiryMstCondition.builder().inquiryNo(2L).build();
      InquiryMstUpdateTarget target = InquiryMstUpdateTarget.builder().updatedBy(9L).build();

      Integer actual = inquiryMstMapper.update(condition, target);
      assertEquals(1, actual);

      List<InquiryMst> actualData = getInquiryMstList("inquiry_no=2");
      assertEquals(1, actualData.size());
      assertEquals(2L, actualData.getFirst().getId());
      assertEquals(9L, actualData.getFirst().getUpdatedBy());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：ステータス区分でのupdate")
    void update_by_statusKbn() {
      InquiryMstCondition condition =
          InquiryMstCondition.builder().statusKbn(InquiryStatusEnum.REPLIED).build();
      InquiryMstUpdateTarget target = InquiryMstUpdateTarget.builder().updatedBy(9L).build();

      Integer actual = inquiryMstMapper.update(condition, target);
      assertEquals(1, actual);

      List<InquiryMst> actualData = getInquiryMstList("status_kbn='replied'");
      assertEquals(1, actualData.size());
      assertEquals(2L, actualData.getFirst().getId());
      assertEquals(9L, actualData.getFirst().getUpdatedBy());
    }

    @Test
    @Order(5)
    @DisplayName("正常系：複数の条件でupdateする場合")
    void update_some_conditions() {
      InquiryMstCondition condition =
          InquiryMstCondition.builder().accountNo(1L).inquiryNo(1L).build();
      InquiryMstUpdateTarget target =
          InquiryMstUpdateTarget.builder()
              .updatedBy(9L)
              .statusKbn(InquiryStatusEnum.REPLIED)
              .isReadByUser(false)
              .build();

      Integer actual = inquiryMstMapper.update(condition, target);
      assertEquals(1, actual);

      List<InquiryMst> actualData = getInquiryMstList("account_no=1 AND inquiry_no=1");
      assertEquals(1, actualData.size());
      assertEquals(9L, actualData.getFirst().getUpdatedBy());
      assertEquals(InquiryStatusEnum.REPLIED, actualData.getFirst().getStatusKbn());
      assertFalse(actualData.getFirst().getIsReadByUser());

      // 条件に該当しない他レコードは更新されない
      List<InquiryMst> otherData = getInquiryMstList("id<>1");
      assertTrue(otherData.stream().noneMatch(d -> d.getUpdatedBy() == 9L));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/InquiryMstMapperTest.sql")
  class getMaxInquiryNo {
    @Test
    @Order(1)
    @DisplayName("正常系：登録済みのお問い合わせがある場合、最大のお問い合わせ番号を返すこと")
    void getMaxInquiryNo_found() {
      Long actual = inquiryMstMapper.getMaxInquiryNo(1L);

      assertEquals(2L, actual);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：登録済みのお問い合わせがない場合、nullを返すこと")
    void getMaxInquiryNo_not_found() {
      Long actual = inquiryMstMapper.getMaxInquiryNo(99L);

      assertNull(actual);
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/InquiryMstMapperTest.sql")
  class selectList {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号でのselectで2件以上の場合、お問い合わせ番号の降順で返すこと")
    void selectList_by_accountNo() {
      InquiryMstCondition condition =
          InquiryMstCondition.builder().accountNo(1L).limit(10).offset(0).build();

      List<InquiryDto> actual = inquiryMstMapper.selectList(condition);

      assertEquals(2, actual.size());
      assertEquals(2L, actual.get(0).getInquiryNo());
      assertEquals(1L, actual.get(1).getInquiryNo());
      // ユーザー向け一覧ではアカウント情報は結合しない
      assertNull(actual.get(0).getAccountId());
      assertNull(actual.get(0).getAccountName());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：お問い合わせ番号でのselectで1件の場合")
    void selectList_by_inquiryNo() {
      InquiryMstCondition condition =
          InquiryMstCondition.builder().inquiryNo(1L).limit(10).offset(0).build();

      List<InquiryDto> actual = inquiryMstMapper.selectList(condition);

      assertEquals(2, actual.size());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：ステータス区分でのselectで1件の場合")
    void selectList_by_statusKbn() {
      InquiryMstCondition condition =
          InquiryMstCondition.builder()
              .statusKbn(InquiryStatusEnum.REPLIED)
              .limit(10)
              .offset(0)
              .build();

      List<InquiryDto> actual = inquiryMstMapper.selectList(condition);

      assertEquals(1, actual.size());
      assertEquals(2L, actual.get(0).getInquiryNo());
      assertEquals(InquiryStatusEnum.REPLIED, actual.get(0).getStatusKbn());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：selectで0件の場合")
    void selectList_not_found() {
      InquiryMstCondition condition =
          InquiryMstCondition.builder().accountNo(99L).limit(10).offset(0).build();

      List<InquiryDto> actual = inquiryMstMapper.selectList(condition);

      assertEquals(0, actual.size());
    }

    @Test
    @Order(5)
    @DisplayName("正常系：複数の条件でselectする場合")
    void selectList_some_conditions() {
      InquiryMstCondition condition =
          InquiryMstCondition.builder().accountNo(1L).inquiryNo(1L).limit(10).offset(0).build();

      List<InquiryDto> actual = inquiryMstMapper.selectList(condition);

      assertEquals(1, actual.size());
      assertEquals(1L, actual.get(0).getInquiryNo());
    }

    @Test
    @Order(6)
    @DisplayName("正常系：limit・offsetが適用されること")
    void selectList_applies_limit_and_offset() {
      InquiryMstCondition condition =
          InquiryMstCondition.builder().accountNo(1L).limit(1).offset(1).build();

      List<InquiryDto> actual = inquiryMstMapper.selectList(condition);

      assertEquals(1, actual.size());
      // inquiry_no DESC の2件目（1件目はinquiry_no=2）
      assertEquals(1L, actual.get(0).getInquiryNo());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/InquiryMstMapperTest.sql")
  class selectListForAdmin {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント情報を結合し、作成日時の降順で返すこと")
    void selectListForAdmin_ordersByCreatedAtDesc_withAccountInfo() {
      InquiryMstCondition condition = InquiryMstCondition.builder().limit(10).offset(0).build();

      List<InquiryDto> actual = inquiryMstMapper.selectListForAdmin(condition);

      assertEquals(3, actual.size());
      assertEquals(3L, actual.get(0).getId());
      assertEquals(2L, actual.get(1).getId());
      assertEquals(1L, actual.get(2).getId());
      assertEquals("bbbbbbbb", actual.get(0).getAccountId());
      assertEquals("BBBBBBBB", actual.get(0).getAccountName());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ステータス区分でのselectで2件の場合")
    void selectListForAdmin_by_statusKbn() {
      InquiryMstCondition condition =
          InquiryMstCondition.builder()
              .statusKbn(InquiryStatusEnum.UNREPLIED)
              .limit(10)
              .offset(0)
              .build();

      List<InquiryDto> actual = inquiryMstMapper.selectListForAdmin(condition);

      assertEquals(2, actual.size());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：selectで0件の場合")
    void selectListForAdmin_not_found() {
      InquiryMstCondition condition =
          InquiryMstCondition.builder().accountNo(99L).limit(10).offset(0).build();

      List<InquiryDto> actual = inquiryMstMapper.selectListForAdmin(condition);

      assertEquals(0, actual.size());
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/InquiryMstMapperTest.sql")
  class selectDetail {
    @Test
    @Order(1)
    @DisplayName("正常系：条件に該当するお問い合わせ詳細を取得できること")
    void selectDetail_success() {
      InquiryMstCondition condition =
          InquiryMstCondition.builder().accountNo(1L).inquiryNo(1L).build();

      InquiryDetailDto actual = inquiryMstMapper.selectDetail(condition);

      assertNotNull(actual);
      assertEquals(1L, actual.getId());
      assertEquals(1L, actual.getAccountNo());
      assertEquals(1L, actual.getInquiryNo());
      assertEquals("件名1", actual.getSubject());
      assertEquals("本文1", actual.getBody());
      assertEquals(InquiryStatusEnum.UNREPLIED, actual.getStatusKbn());
      assertTrue(actual.getIsReadByUser());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getCreatedAt());
      // ユーザー向け詳細ではアカウント情報は結合しない
      assertNull(actual.getAccountId());
      assertNull(actual.getAccountName());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：条件に該当するお問い合わせが存在しない場合、nullを返すこと")
    void selectDetail_not_found() {
      InquiryMstCondition condition =
          InquiryMstCondition.builder().accountNo(1L).inquiryNo(99L).build();

      InquiryDetailDto actual = inquiryMstMapper.selectDetail(condition);

      assertNull(actual);
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/InquiryMstMapperTest.sql")
  class selectDetailForAdmin {
    @Test
    @Order(1)
    @DisplayName("正常系：条件に該当するお問い合わせ詳細を、アカウント情報を結合して取得できること")
    void selectDetailForAdmin_success() {
      InquiryMstCondition condition = InquiryMstCondition.builder().id(1L).build();

      InquiryDetailDto actual = inquiryMstMapper.selectDetailForAdmin(condition);

      assertNotNull(actual);
      assertEquals(1L, actual.getId());
      assertEquals("aaaaaaaa", actual.getAccountId());
      assertEquals("AAAAAAAA", actual.getAccountName());
      assertEquals("件名1", actual.getSubject());
      assertEquals("本文1", actual.getBody());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：条件に該当するお問い合わせが存在しない場合、nullを返すこと")
    void selectDetailForAdmin_not_found() {
      InquiryMstCondition condition = InquiryMstCondition.builder().id(99L).build();

      InquiryDetailDto actual = inquiryMstMapper.selectDetailForAdmin(condition);

      assertNull(actual);
    }
  }
}
