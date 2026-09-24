package com.web.gallery.helper;

import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ValidationErrorLoggerTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class logFieldErrors {
    @Test
    @Order(1)
    @DisplayName("正常系：機微フィールドでない場合、入力値をそのままログ出力すること")
    void logFieldErrors_nonSensitiveField() {
      Logger log = mock(Logger.class);
      BindingResult bindingResult = mock(BindingResult.class);
      FieldError fieldError = mock(FieldError.class);
      doReturn("email").when(fieldError).getField();
      doReturn("invalid@").when(fieldError).getRejectedValue();
      doReturn("must be valid").when(fieldError).getDefaultMessage();
      doReturn(List.of(fieldError)).when(bindingResult).getFieldErrors();

      ValidationErrorLogger.logFieldErrors(log, bindingResult);

      verify(log, times(1)).info(anyString(), eq("email"), eq("invalid@"), eq("must be valid"));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：機微フィールドの場合、入力値をマスクしてログ出力すること")
    void logFieldErrors_sensitiveField() {
      Logger log = mock(Logger.class);
      BindingResult bindingResult = mock(BindingResult.class);
      FieldError fieldError = mock(FieldError.class);
      doReturn("password").when(fieldError).getField();
      doReturn("too short").when(fieldError).getDefaultMessage();
      doReturn(List.of(fieldError)).when(bindingResult).getFieldErrors();

      ValidationErrorLogger.logFieldErrors(log, bindingResult);

      verify(log, times(1)).info(anyString(), eq("password"), eq("***"), eq("too short"));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：ネストしたパスの末尾セグメントが機微フィールド名の場合、マスクしてログ出力すること")
    void logFieldErrors_nestedSensitiveField() {
      Logger log = mock(Logger.class);
      BindingResult bindingResult = mock(BindingResult.class);
      FieldError fieldError = mock(FieldError.class);
      doReturn("account.newPassword").when(fieldError).getField();
      doReturn("too short").when(fieldError).getDefaultMessage();
      doReturn(List.of(fieldError)).when(bindingResult).getFieldErrors();

      ValidationErrorLogger.logFieldErrors(log, bindingResult);

      verify(log, times(1))
          .info(anyString(), eq("account.newPassword"), eq("***"), eq("too short"));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：フィールド名がnullの場合、機微フィールドとして扱わずそのままログ出力すること")
    void logFieldErrors_nullFieldName() {
      Logger log = mock(Logger.class);
      BindingResult bindingResult = mock(BindingResult.class);
      FieldError fieldError = mock(FieldError.class);
      doReturn(null).when(fieldError).getField();
      doReturn("someValue").when(fieldError).getRejectedValue();
      doReturn("invalid").when(fieldError).getDefaultMessage();
      doReturn(List.of(fieldError)).when(bindingResult).getFieldErrors();

      ValidationErrorLogger.logFieldErrors(log, bindingResult);

      verify(log, times(1)).info(anyString(), isNull(), eq("someValue"), eq("invalid"));
    }
  }
}
