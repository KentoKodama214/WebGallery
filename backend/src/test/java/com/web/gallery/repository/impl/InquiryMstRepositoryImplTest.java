package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.dto.InquiryDetailDto;
import com.web.gallery.dto.InquiryDto;
import com.web.gallery.entity.InquiryReplyMstCondition;
import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.mapper.InquiryMstMapper;
import com.web.gallery.mapper.InquiryReplyMstMapper;
import com.web.gallery.model.InquiryDetailModel;
import com.web.gallery.model.InquiryGetModel;
import com.web.gallery.model.InquiryPageModel;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class InquiryMstRepositoryImplTest {
  @InjectMocks private InquiryMstRepositoryImpl inquiryMstRepositoryImpl;

  @Mock private InquiryMstMapper inquiryMstMapper;
  @Mock private InquiryReplyMstMapper inquiryReplyMstMapper;

  private InquiryDto newInquiryDto(long inquiryNo) {
    InquiryDto dto = new InquiryDto();
    dto.setId(inquiryNo);
    dto.setAccountNo(1L);
    dto.setAccountId("testuser01");
    dto.setAccountName("テストユーザー");
    dto.setInquiryNo(inquiryNo);
    dto.setSubject("件名");
    dto.setStatusKbn(InquiryStatusEnum.UNREPLIED);
    dto.setIsReadByUser(true);
    dto.setCreatedAt(OffsetDateTime.now());
    return dto;
  }

  private InquiryDetailDto newInquiryDetailDto() {
    InquiryDetailDto dto = new InquiryDetailDto();
    dto.setId(1L);
    dto.setAccountNo(1L);
    dto.setAccountId("testuser01");
    dto.setAccountName("テストユーザー");
    dto.setInquiryNo(1L);
    dto.setSubject("件名");
    dto.setBody("本文");
    dto.setStatusKbn(InquiryStatusEnum.UNREPLIED);
    dto.setIsReadByUser(true);
    dto.setCreatedAt(OffsetDateTime.now());
    return dto;
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNewInquiryNo {
    @Test
    @Order(1)
    @DisplayName("正常系：登録済みの最大お問い合わせ番号の次の番号を返すこと")
    void getNewInquiryNo_existing() {
      doReturn(5L).when(inquiryMstMapper).getMaxInquiryNo(1L);

      InquiryNo actual = inquiryMstRepositoryImpl.getNewInquiryNo(new AccountNo(1L));

      assertEquals(6L, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：未登録の場合は1を返すこと")
    void getNewInquiryNo_none() {
      doReturn(null).when(inquiryMstMapper).getMaxInquiryNo(1L);

      InquiryNo actual = inquiryMstRepositoryImpl.getNewInquiryNo(new AccountNo(1L));

      assertEquals(1L, actual.value());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getInquiryList {
    @Test
    @Order(1)
    @DisplayName("正常系：取得件数が表示件数未満の場合、最後のページと判定されること")
    void getInquiryList_lastPage() {
      InquiryGetModel model =
          InquiryGetModel.builder().accountNo(new AccountNo(1L)).limit(10).offset(0).build();
      doReturn(List.of(newInquiryDto(1L))).when(inquiryMstMapper).selectList(any());

      InquiryPageModel actual = inquiryMstRepositoryImpl.getInquiryList(model);

      assertTrue(actual.getIsLast());
      assertEquals(1, actual.getInquiryModelList().size());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：取得件数が表示件数以上の場合、最後のページではないと判定され1件切り詰められること")
    void getInquiryList_notLastPage() {
      InquiryGetModel model =
          InquiryGetModel.builder().accountNo(new AccountNo(1L)).limit(2).offset(0).build();
      doReturn(List.of(newInquiryDto(1L), newInquiryDto(2L)))
          .when(inquiryMstMapper)
          .selectList(any());

      InquiryPageModel actual = inquiryMstRepositoryImpl.getInquiryList(model);

      assertFalse(actual.getIsLast());
      assertEquals(1, actual.getInquiryModelList().size());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getInquiryListForAdmin {
    @Test
    @Order(1)
    @DisplayName("正常系：取得件数が表示件数未満の場合、最後のページと判定されること")
    void getInquiryListForAdmin_lastPage() {
      InquiryGetModel model = InquiryGetModel.builder().limit(10).offset(0).build();
      doReturn(List.of(newInquiryDto(1L))).when(inquiryMstMapper).selectListForAdmin(any());

      InquiryPageModel actual = inquiryMstRepositoryImpl.getInquiryListForAdmin(model);

      assertTrue(actual.getIsLast());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getInquiryDetail {
    @Test
    @Order(1)
    @DisplayName("正常系：お問い合わせが存在する場合、詳細情報を返すこと")
    void getInquiryDetail_found() throws GalleryException {
      doReturn(newInquiryDetailDto()).when(inquiryMstMapper).selectDetail(any());
      doReturn(List.of()).when(inquiryReplyMstMapper).selectList(any());

      InquiryDetailModel actual =
          inquiryMstRepositoryImpl.getInquiryDetail(new AccountNo(1L), new InquiryNo(1L));

      assertNotNull(actual);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：お問い合わせが存在しない場合、GalleryExceptionをthrowすること")
    void getInquiryDetail_notFound() {
      doReturn(null).when(inquiryMstMapper).selectDetail(any());

      assertThrows(
          GalleryException.class,
          () -> inquiryMstRepositoryImpl.getInquiryDetail(new AccountNo(1L), new InquiryNo(1L)));

      verify(inquiryReplyMstMapper, never()).selectList(any(InquiryReplyMstCondition.class));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getInquiryDetailForAdmin {
    @Test
    @Order(1)
    @DisplayName("正常系：お問い合わせが存在する場合、詳細情報を返すこと")
    void getInquiryDetailForAdmin_found() throws GalleryException {
      doReturn(newInquiryDetailDto()).when(inquiryMstMapper).selectDetailForAdmin(any());
      doReturn(List.of()).when(inquiryReplyMstMapper).selectList(any());

      InquiryDetailModel actual =
          inquiryMstRepositoryImpl.getInquiryDetailForAdmin(new InquiryId(1L));

      assertNotNull(actual);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：お問い合わせが存在しない場合、GalleryExceptionをthrowすること")
    void getInquiryDetailForAdmin_notFound() {
      doReturn(null).when(inquiryMstMapper).selectDetailForAdmin(any());

      assertThrows(
          GalleryException.class,
          () -> inquiryMstRepositoryImpl.getInquiryDetailForAdmin(new InquiryId(1L)));
    }
  }
}
