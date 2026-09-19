package com.web.gallery.enumeration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class InquiryStatusEnumTest {

  @Test
  @DisplayName("正常系：DB保存値に一致する場合、対応するEnum値を返すこと")
  void getOrDefault_matchedByDbValue() {
    assertEquals(InquiryStatusEnum.REPLIED, InquiryStatusEnum.getOrDefault("replied"));
  }

  @Test
  @DisplayName("正常系：Enum名に一致する場合、対応するEnum値を返すこと")
  void getOrDefault_matchedByName() {
    assertEquals(InquiryStatusEnum.WITHDRAWN, InquiryStatusEnum.getOrDefault("WITHDRAWN"));
  }

  @Test
  @DisplayName("異常系：nullを指定した場合、UNREPLIEDを返すこと")
  void getOrDefault_null() {
    assertEquals(InquiryStatusEnum.UNREPLIED, InquiryStatusEnum.getOrDefault(null));
  }

  @Test
  @DisplayName("異常系：該当するEnum値がない場合、UNREPLIEDを返すこと")
  void getOrDefault_unmatched() {
    assertEquals(InquiryStatusEnum.UNREPLIED, InquiryStatusEnum.getOrDefault("unknown"));
  }
}
