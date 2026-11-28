package com.web.gallery.infrastructure.persistence.mapper.inquiry;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.infrastructure.persistence.entity.inquiry.InquiryReplyMst;
import com.web.gallery.infrastructure.persistence.entity.inquiry.InquiryReplyMstCondition;
import java.time.OffsetDateTime;
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
public class InquiryReplyMstMapperTest {
  @Autowired private InquiryReplyMstMapper inquiryReplyMstMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/InquiryReplyMstMapperTest.sql")
  @Sql("/sql/common/ResetInquiryReplyMstIdSeq.sql")
  class insert {
    @Test
    @Order(1)
    @DisplayName("正常系：登録成功")
    void insert_success() {
      InquiryReplyMst insertInquiryReplyMst =
          InquiryReplyMst.builder()
              .inquiryId(1L)
              .replyNo(3L)
              .adminAccountNo(1L)
              .createdBy(1L)
              .body("新しい返信本文")
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actualCount = inquiryReplyMstMapper.insert(insertInquiryReplyMst);
      assertEquals(1, actualCount);

      List<InquiryReplyMst> actualData =
          jdbcTemplate.query(
              "SELECT * FROM common.inquiry_reply_mst WHERE inquiry_id = 1 AND reply_no = 3",
              (rs, rowNum) ->
                  InquiryReplyMst.builder()
                      .id(rs.getLong("id"))
                      .inquiryId(rs.getLong("inquiry_id"))
                      .replyNo(rs.getLong("reply_no"))
                      .adminAccountNo(rs.getLong("admin_account_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .body(rs.getString("body"))
                      .build());

      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getInquiryId());
      assertEquals(3L, actualData.getFirst().getReplyNo());
      assertEquals(1L, actualData.getFirst().getAdminAccountNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
      assertEquals("新しい返信本文", actualData.getFirst().getBody());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/InquiryReplyMstMapperTest.sql")
  class getMaxReplyNo {
    @Test
    @Order(1)
    @DisplayName("正常系：登録済みの返信がある場合、最大の返信番号を返すこと")
    void getMaxReplyNo_found() {
      Long actual = inquiryReplyMstMapper.getMaxReplyNo(1L);

      assertEquals(2L, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：登録済みの返信がない場合、nullを返すこと")
    void getMaxReplyNo_notFound() {
      Long actual = inquiryReplyMstMapper.getMaxReplyNo(2L);

      assertNull(actual);
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/InquiryReplyMstMapperTest.sql")
  class selectList {
    @Test
    @Order(1)
    @DisplayName("正常系：返信番号の昇順で一覧を取得できること")
    void selectList_orderedByReplyNo() {
      InquiryReplyMstCondition condition = InquiryReplyMstCondition.byInquiryId(1L);

      List<InquiryReplyMst> actual = inquiryReplyMstMapper.selectList(condition);

      assertEquals(2, actual.size());
      assertEquals(1L, actual.get(0).getReplyNo());
      assertEquals("返信本文1", actual.get(0).getBody());
      assertEquals(2L, actual.get(1).getReplyNo());
      assertEquals("返信本文2", actual.get(1).getBody());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：該当するお問い合わせIDの返信がない場合、空リストを返すこと")
    void selectList_notFound() {
      InquiryReplyMstCondition condition = InquiryReplyMstCondition.byInquiryId(2L);

      List<InquiryReplyMst> actual = inquiryReplyMstMapper.selectList(condition);

      assertEquals(0, actual.size());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/InquiryReplyMstMapperTest.sql")
  class exists {
    @Test
    @Order(1)
    @DisplayName("正常系：他ユーザーのお問い合わせへ投稿した返信がある場合、trueを返すこと")
    void exists_replyToOthersInquiry() {
      // アカウント1はアカウント3のお問い合わせ（お問い合わせID:3）へ返信している
      assertTrue(
          inquiryReplyMstMapper.exists(
              InquiryReplyMstCondition.byAdminAccountNoExcludingOwnInquiry(1L)));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：自分が起票したお問い合わせへの自己返信しかない場合、falseを返すこと")
    void exists_onlyReplyToOwnInquiry() {
      // アカウント4は自分のお問い合わせ（お問い合わせID:4）にしか返信していない。
      // 自スレッドの返信は退会時にスレッドごと削除されるため、退会をブロックする理由にならない
      assertFalse(
          inquiryReplyMstMapper.exists(
              InquiryReplyMstCondition.byAdminAccountNoExcludingOwnInquiry(4L)));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：返信を1件も投稿していない場合、falseを返すこと")
    void exists_noReply() {
      assertFalse(
          inquiryReplyMstMapper.exists(
              InquiryReplyMstCondition.byAdminAccountNoExcludingOwnInquiry(2L)));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：お問い合わせIDに該当する返信の有無を返すこと")
    void exists_byInquiryId() {
      assertTrue(inquiryReplyMstMapper.exists(InquiryReplyMstCondition.byInquiryId(1L)));
      assertFalse(inquiryReplyMstMapper.exists(InquiryReplyMstCondition.byInquiryId(2L)));
    }

    @Test
    @Order(5)
    @DisplayName("正常系：お問い合わせの登録者のアカウント番号に該当する返信の有無を返すこと")
    void exists_byInquiryAccountNo() {
      assertTrue(inquiryReplyMstMapper.exists(InquiryReplyMstCondition.byInquiryAccountNo(1L)));
      assertFalse(inquiryReplyMstMapper.exists(InquiryReplyMstCondition.byInquiryAccountNo(2L)));
    }

    @Test
    @Order(6)
    @DisplayName("抽出条件が空の場合はfalseを返すこと（全件を対象にしてしまうのを防ぐガード）")
    void exists_emptyCondition() {
      // 抽出条件はファクトリメソッド経由での生成を前提としているが、builder()から直接
      // 組み立てられた場合でも「1件でも行があればtrue」にならないこと
      assertFalse(inquiryReplyMstMapper.exists(InquiryReplyMstCondition.builder().build()));
    }

    @Test
    @Order(7)
    @DisplayName("除外条件だけが指定された場合もfalseを返すこと（除外条件は単独では抽出条件にならない）")
    void exists_onlyExcludingCondition() {
      assertFalse(
          inquiryReplyMstMapper.exists(
              InquiryReplyMstCondition.builder().excludingInquiryAccountNo(1L).build()));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/InquiryReplyMstMapperTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：お問い合わせの登録者のアカウント番号に紐づく返信を削除すること")
    void delete_byInquiryAccountNo() {
      // アカウント1が起票したお問い合わせ（お問い合わせID:1）に紐づく返信2件だけが消え、
      // 他ユーザーのスレッドの返信（返信3・4）は残ること
      assertEquals(
          2, inquiryReplyMstMapper.delete(InquiryReplyMstCondition.byInquiryAccountNo(1L)));
      assertEquals(
          0,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.inquiry_reply_mst WHERE inquiry_id = 1", Integer.class));
      assertEquals(
          2,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.inquiry_reply_mst", Integer.class));
    }

    @Test
    @Order(2)
    @DisplayName("抽出条件が空の場合は1件も削除しないこと（WHERE句なしの全件削除を防ぐガード）")
    void delete_emptyCondition_deletesNothing() {
      // 抽出条件はファクトリメソッド経由での生成を前提としているが、builder()から直接
      // 組み立てられた場合でも全件削除にならないこと
      assertEquals(0, inquiryReplyMstMapper.delete(InquiryReplyMstCondition.builder().build()));
      assertEquals(
          4,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.inquiry_reply_mst", Integer.class));
    }

    @Test
    @Order(3)
    @DisplayName("管理者のアカウント番号は削除条件にならないこと（返信を持つ管理者はアカウント削除自体をブロックする仕様のため）")
    void delete_byAdminAccountNo_isNotSupported() {
      assertEquals(
          0,
          inquiryReplyMstMapper.delete(
              InquiryReplyMstCondition.byAdminAccountNoExcludingOwnInquiry(1L)));
      assertEquals(
          4,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.inquiry_reply_mst", Integer.class));
    }
  }
}
