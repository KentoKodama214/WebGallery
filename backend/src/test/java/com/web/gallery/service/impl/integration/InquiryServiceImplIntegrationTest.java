package com.web.gallery.service.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryBody;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.InquirySubject;
import com.web.gallery.domain.inquiry.ReplyBody;
import com.web.gallery.domain.inquiry.ReplyNo;
import com.web.gallery.entity.inquiry.InquiryMst;
import com.web.gallery.entity.inquiry.InquiryReplyMst;
import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.InquiryNotFoundException;
import com.web.gallery.model.inquiry.InquiryDetailModel;
import com.web.gallery.model.inquiry.InquiryListGetModel;
import com.web.gallery.model.inquiry.InquiryPageModel;
import com.web.gallery.service.impl.InquiryServiceImpl;
import java.time.OffsetDateTime;
import java.util.List;
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
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class InquiryServiceImplIntegrationTest {
  @Autowired private InquiryServiceImpl inquiryServiceImpl;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/service/InquiryServiceImplIntegrationTest.sql")
  class registInquiry {
    @Test
    @Order(1)
    @DisplayName("正常系：お問い合わせが新規登録され、お問い合わせ番号が採番されること")
    void registInquiry_success() throws GalleryException {
      InquiryDetailModel requestDetail =
          InquiryDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .subject(new InquirySubject("新規のお問い合わせ"))
              .body(new InquiryBody("新規のお問い合わせ本文です。"))
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      InquiryNo inquiryNo = inquiryServiceImpl.registInquiry(requestDetail);

      assertEquals(8L, inquiryNo.value());
      List<InquiryMst> actualData =
          jdbcTemplate.query(
              "SELECT * FROM common.inquiry_mst WHERE account_no=1 AND inquiry_no=8",
              (rs, rowNum) ->
                  InquiryMst.builder()
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
      assertEquals(8L, actualData.getFirst().getInquiryNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
      assertEquals(1L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertEquals("新規のお問い合わせ", actualData.getFirst().getSubject());
      assertEquals("新規のお問い合わせ本文です。", actualData.getFirst().getBody());
      assertEquals(InquiryStatusEnum.UNREPLIED, actualData.getFirst().getStatusKbn());
      assertTrue(actualData.getFirst().getIsReadByUser());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/service/InquiryServiceImplIntegrationTest.sql")
  class getInquiryList {
    @Test
    @Order(1)
    @DisplayName("正常系：自分のお問い合わせ一覧を取得できること（1ページ目、5件に切り詰められ最後のページでないこと）")
    void getInquiryList_success() {
      InquiryListGetModel model =
          InquiryListGetModel.builder().accountNo(new AccountNo(1L)).pageNo(1).build();

      InquiryPageModel actual = inquiryServiceImpl.getInquiryList(model);

      assertFalse(actual.getIsLast());
      assertEquals(5, actual.getInquiryModelList().size());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：自分のお問い合わせ一覧を取得できること（2ページ目、残り2件で最後のページと判定されること）")
    void getInquiryList_secondPage_isLast() {
      InquiryListGetModel model =
          InquiryListGetModel.builder().accountNo(new AccountNo(1L)).pageNo(2).build();

      InquiryPageModel actual = inquiryServiceImpl.getInquiryList(model);

      assertTrue(actual.getIsLast());
      assertEquals(2, actual.getInquiryModelList().size());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/service/InquiryServiceImplIntegrationTest.sql")
  class getInquiryDetail {
    @Test
    @Order(1)
    @DisplayName("正常系：既読済みのお問い合わせは既読化処理を行わずそのまま返すこと")
    void getInquiryDetail_alreadyRead() throws GalleryException {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      InquiryDetailModel actual =
          inquiryServiceImpl.getInquiryDetail(new AccountNo(1L), new InquiryNo(1L));

      assertTrue(actual.getIsReadByUser());

      // 既読化のUPDATEが発生していない（updated_atがフィクスチャ投入時のまま）ことを確認する
      OffsetDateTime updatedAt =
          jdbcTemplate.queryForObject(
              "SELECT updated_at FROM common.inquiry_mst WHERE id=1", OffsetDateTime.class);
      assertTrue(updatedAt.isBefore(transactionNow));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：未読のお問い合わせは取得と同時に既読化されること")
    void getInquiryDetail_markReadByUser() throws GalleryException {
      InquiryDetailModel actual =
          inquiryServiceImpl.getInquiryDetail(new AccountNo(1L), new InquiryNo(2L));

      assertTrue(actual.getIsReadByUser());

      Boolean isReadByUser =
          jdbcTemplate.queryForObject(
              "SELECT is_read_by_user FROM common.inquiry_mst WHERE id=2", Boolean.class);
      assertTrue(isReadByUser);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：お問い合わせが存在しない場合、InquiryNotFoundExceptionをthrowする")
    void getInquiryDetail_InquiryNotFoundException() {
      assertThrows(
          InquiryNotFoundException.class,
          () -> inquiryServiceImpl.getInquiryDetail(new AccountNo(1L), new InquiryNo(999L)));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/service/InquiryServiceImplIntegrationTest.sql")
  class getInquiryListForAdmin {
    @Test
    @Order(1)
    @DisplayName("正常系：全アカウントのお問い合わせ一覧を取得できること（1ページ目、5件に切り詰められ最後のページでないこと）")
    void getInquiryListForAdmin_success() {
      InquiryListGetModel model = InquiryListGetModel.builder().pageNo(1).build();

      InquiryPageModel actual = inquiryServiceImpl.getInquiryListForAdmin(model);

      assertFalse(actual.getIsLast());
      assertEquals(5, actual.getInquiryModelList().size());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：全アカウントのお問い合わせ一覧を取得できること（2ページ目、残り2件で最後のページと判定されること）")
    void getInquiryListForAdmin_secondPage_isLast() {
      InquiryListGetModel model = InquiryListGetModel.builder().pageNo(2).build();

      InquiryPageModel actual = inquiryServiceImpl.getInquiryListForAdmin(model);

      assertTrue(actual.getIsLast());
      assertEquals(2, actual.getInquiryModelList().size());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/service/InquiryServiceImplIntegrationTest.sql")
  class getInquiryDetailForAdmin {
    @Test
    @Order(1)
    @DisplayName("正常系：管理者用にお問い合わせ詳細を取得できること")
    void getInquiryDetailForAdmin_success() throws GalleryException {
      InquiryDetailModel actual = inquiryServiceImpl.getInquiryDetailForAdmin(new InquiryId(1L));

      assertEquals("既読の問い合わせ", actual.getSubject().value());
      assertEquals(1, actual.getReplyModelList().toList().size());
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/service/InquiryServiceImplIntegrationTest.sql")
  class replyToInquiry {
    @Test
    @Order(1)
    @DisplayName("正常系：返信が登録され、ステータスが回答済みに更新されること")
    void replyToInquiry_success() throws GalleryException {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      ReplyNo replyNo =
          inquiryServiceImpl.replyToInquiry(
              new InquiryId(3L), new AccountNo(2L), new ReplyBody("返信本文です。"));

      assertEquals(1L, replyNo.value());
      String statusKbn =
          jdbcTemplate.queryForObject(
              "SELECT status_kbn FROM common.inquiry_mst WHERE id=3", String.class);
      assertEquals("replied", statusKbn);

      List<InquiryReplyMst> actualReplyData =
          jdbcTemplate.query(
              "SELECT * FROM common.inquiry_reply_mst WHERE inquiry_id=3",
              (rs, rowNum) ->
                  InquiryReplyMst.builder()
                      .inquiryId(rs.getLong("inquiry_id"))
                      .replyNo(rs.getLong("reply_no"))
                      .adminAccountNo(rs.getLong("admin_account_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .body(rs.getString("body"))
                      .build());
      assertEquals(1, actualReplyData.size());
      assertEquals(3L, actualReplyData.getFirst().getInquiryId());
      assertEquals(1L, actualReplyData.getFirst().getReplyNo());
      assertEquals(2L, actualReplyData.getFirst().getAdminAccountNo());
      assertEquals(2L, actualReplyData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualReplyData.getFirst().getCreatedAt());
      assertEquals("返信本文です。", actualReplyData.getFirst().getBody());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：取り下げ済みのお問い合わせに返信しようとした場合、BadRequestExceptionをthrowする")
    void replyToInquiry_withdrawn() {
      assertThrows(
          BadRequestException.class,
          () ->
              inquiryServiceImpl.replyToInquiry(
                  new InquiryId(4L), new AccountNo(2L), new ReplyBody("返信本文です。")));
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/service/InquiryServiceImplIntegrationTest.sql")
  class withdrawInquiry {
    @Test
    @Order(1)
    @DisplayName("正常系：お問い合わせが取り下げられること")
    void withdrawInquiry_success() throws GalleryException {
      inquiryServiceImpl.withdrawInquiry(new AccountNo(1L), new InquiryNo(3L));

      String statusKbn =
          jdbcTemplate.queryForObject(
              "SELECT status_kbn FROM common.inquiry_mst WHERE id=3", String.class);
      assertEquals("withdrawn", statusKbn);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：お問い合わせが存在しない場合、InquiryNotFoundExceptionをthrowする")
    void withdrawInquiry_InquiryNotFoundException() {
      assertThrows(
          InquiryNotFoundException.class,
          () -> inquiryServiceImpl.withdrawInquiry(new AccountNo(1L), new InquiryNo(999L)));
    }
  }
}
