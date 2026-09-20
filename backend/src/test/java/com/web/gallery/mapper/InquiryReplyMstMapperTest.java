package com.web.gallery.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.entity.InquiryReplyMst;
import com.web.gallery.entity.InquiryReplyMstCondition;
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
}
