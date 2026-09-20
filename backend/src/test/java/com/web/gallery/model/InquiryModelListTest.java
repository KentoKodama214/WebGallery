package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.dto.InquiryDto;
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
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class InquiryModelListTest {

  private InquiryDto dto(long id) {
    InquiryDto dto = new InquiryDto();
    dto.setId(id);
    dto.setAccountNo(1L);
    dto.setInquiryNo(id);
    dto.setSubject("件名" + id);
    dto.setStatusKbn(InquiryStatusEnum.UNREPLIED);
    dto.setIsReadByUser(true);
    dto.setCreatedAt(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)));
    return dto;
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：InquiryDtoのリストがInquiryModelのリストに変換されること")
    void from_allFieldsSet() {
      InquiryModelList actual = InquiryModelList.from(List.of(dto(1L), dto(2L)));

      assertEquals(2, actual.size());
      assertEquals(1L, actual.get(0).getInquiryId().value());
      assertEquals(2L, actual.get(1).getInquiryId().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：空リストの場合、空のInquiryModelListに変換されること")
    void from_emptyList() {
      InquiryModelList actual = InquiryModelList.from(List.of());

      assertTrue(actual.isEmpty());
    }
  }
}
