package com.web.gallery.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.aggregate.Inquiry;
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
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.InquiryNotFoundException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.inquiry.InquiryDetailModel;
import com.web.gallery.model.inquiry.InquiryReplyModelList;
import com.web.gallery.repository.impl.InquiryAggregateRepositoryImpl;
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

/**
 * {@link InquiryAggregateRepositoryImpl}の結合テスト
 *
 * <p>regist・addReplyの失敗分岐は、実DBを介して再現できない（到達不能な防御的分岐）。regist（{@code
 * inquiryMstMapper.insert}）は単純なINSERTがPK重複時は例外をthrowするのみで0件影響にはならない。addReply（{@code
 * inquiryReplyMstMapper.insert}・{@code inquiryMstMapper.update}）は、返信の登録先である{@code
 * inquiry_reply_mst.inquiry_id}が{@code inquiry_mst.id}へのFK制約を持つため、INSERTが成功する時点で当該IDの行は必ず存在し、
 * 後段のUPDATE（同一IDでの条件一致）は常に成功する。そのため本クラスではmarkReadByUser・withdrawの失敗分岐のみを検証する
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class InquiryAggregateRepositoryImplIntegrationTest {
  @Autowired private InquiryAggregateRepositoryImpl inquiryAggregateRepositoryImpl;

  @Autowired private JdbcTemplate jdbcTemplate;

  private InquiryDetailModel.InquiryDetailModelBuilder baseDetail(
      Long accountNo, Long inquiryId, Long inquiryNo) {
    return InquiryDetailModel.builder()
        .accountNo(new AccountNo(accountNo))
        .inquiryId(inquiryId != null ? new InquiryId(inquiryId) : null)
        .inquiryNo(inquiryNo != null ? new InquiryNo(inquiryNo) : null)
        .subject(new InquirySubject("件名"))
        .body(new InquiryBody("本文"))
        .statusKbn(InquiryStatusEnum.UNREPLIED)
        .isReadByUser(true);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/InquiryAggregateRepositoryImplIntegrationTest.sql")
  class regist {
    @Test
    @Order(1)
    @DisplayName("正常系：お問い合わせが新規登録されること")
    void regist_success() throws GalleryException {
      InquiryDetailModel requestDetail =
          InquiryDetailModel.builder()
              .accountNo(new AccountNo(2L))
              .subject(new InquirySubject("新規のお問い合わせ"))
              .body(new InquiryBody("新規のお問い合わせ本文です。"))
              .build();
      Inquiry inquiry = Inquiry.forRegist(requestDetail, new InquiryNo(1L));

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      inquiryAggregateRepositoryImpl.regist(inquiry);

      List<InquiryMst> actualData =
          jdbcTemplate.query(
              "SELECT * FROM common.inquiry_mst WHERE account_no=2 AND inquiry_no=1",
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
      assertEquals(2L, actualData.getFirst().getAccountNo());
      assertEquals(1L, actualData.getFirst().getInquiryNo());
      assertEquals(2L, actualData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
      assertEquals(2L, actualData.getFirst().getUpdatedBy());
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
  @Sql("/sql/repository/InquiryAggregateRepositoryImplIntegrationTest.sql")
  class addReply {
    @Test
    @Order(1)
    @DisplayName("正常系：返信が追加され、お問い合わせ本体のステータス・既読フラグが更新されること")
    void addReply_success() throws GalleryException {
      InquiryDetailModel detail = baseDetail(1L, 1L, 1L).build();
      Inquiry inquiry = Inquiry.reconstruct(detail, InquiryReplyModelList.empty());
      inquiry.addReply(new AccountNo(1L), new ReplyBody("ご返信ありがとうございます。"), new ReplyNo(1L));

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      inquiryAggregateRepositoryImpl.addReply(inquiry);

      List<InquiryReplyMst> actualReplyData =
          jdbcTemplate.query(
              "SELECT * FROM common.inquiry_reply_mst WHERE inquiry_id=1",
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
      assertEquals(1L, actualReplyData.getFirst().getInquiryId());
      assertEquals(1L, actualReplyData.getFirst().getReplyNo());
      assertEquals(1L, actualReplyData.getFirst().getAdminAccountNo());
      assertEquals(1L, actualReplyData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualReplyData.getFirst().getCreatedAt());
      assertEquals("ご返信ありがとうございます。", actualReplyData.getFirst().getBody());

      String statusKbn =
          jdbcTemplate.queryForObject(
              "SELECT status_kbn FROM common.inquiry_mst WHERE id=1", String.class);
      assertEquals("replied", statusKbn);

      Boolean isReadByUser =
          jdbcTemplate.queryForObject(
              "SELECT is_read_by_user FROM common.inquiry_mst WHERE id=1", Boolean.class);
      assertFalse(isReadByUser);
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/InquiryAggregateRepositoryImplIntegrationTest.sql")
  class markReadByUser {
    @Test
    @Order(1)
    @DisplayName("正常系：ユーザー既読フラグが更新されること")
    void markReadByUser_success() throws GalleryException {
      InquiryDetailModel detail = baseDetail(1L, 2L, 2L).isReadByUser(false).build();
      Inquiry inquiry = Inquiry.reconstruct(detail, InquiryReplyModelList.empty());
      inquiry.markReadByUser();

      inquiryAggregateRepositoryImpl.markReadByUser(inquiry);

      Boolean isReadByUser =
          jdbcTemplate.queryForObject(
              "SELECT is_read_by_user FROM common.inquiry_mst WHERE id=2", Boolean.class);
      assertTrue(isReadByUser);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：対象のお問い合わせが存在しない場合、InquiryNotFoundExceptionをthrowする")
    void markReadByUser_InquiryNotFoundException() {
      InquiryDetailModel detail = baseDetail(1L, null, 999L).isReadByUser(false).build();
      Inquiry inquiry = Inquiry.reconstruct(detail, InquiryReplyModelList.empty());
      inquiry.markReadByUser();

      assertThrows(
          InquiryNotFoundException.class,
          () -> inquiryAggregateRepositoryImpl.markReadByUser(inquiry));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/InquiryAggregateRepositoryImplIntegrationTest.sql")
  class withdraw {
    @Test
    @Order(1)
    @DisplayName("正常系：ステータスが取り下げに更新されること")
    void withdraw_success() throws GalleryException {
      InquiryDetailModel detail = baseDetail(1L, 3L, 3L).build();
      Inquiry inquiry = Inquiry.reconstruct(detail, InquiryReplyModelList.empty());
      inquiry.withdraw();

      inquiryAggregateRepositoryImpl.withdraw(inquiry);

      String statusKbn =
          jdbcTemplate.queryForObject(
              "SELECT status_kbn FROM common.inquiry_mst WHERE id=3", String.class);
      assertEquals("withdrawn", statusKbn);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：対象のお問い合わせが存在しない場合、UpdateFailureExceptionをthrowする")
    void withdraw_UpdateFailureException() {
      InquiryDetailModel detail = baseDetail(1L, null, 999L).build();
      Inquiry inquiry = Inquiry.reconstruct(detail, InquiryReplyModelList.empty());
      inquiry.withdraw();

      assertThrows(
          UpdateFailureException.class, () -> inquiryAggregateRepositoryImpl.withdraw(inquiry));
    }
  }
}
