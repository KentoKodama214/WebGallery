package com.web.gallery.infrastructure.persistence.type_handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.JdbcType;
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
class WebAuthnChallengePurposeEnumTypeHandlerTest {
  @InjectMocks
  private WebAuthnChallengePurposeEnumTypeHandler webAuthnChallengePurposeEnumTypeHandler;

  @Mock private PreparedStatement preparedStatement;
  @Mock private ResultSet resultSet;
  @Mock private CallableStatement callableStatement;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class setNonNullParameter {
    @Test
    @Order(1)
    @DisplayName("正常系：Enum値に対応するDB保存値がPreparedStatementに設定されること")
    void setNonNullParameter_success() throws SQLException {
      webAuthnChallengePurposeEnumTypeHandler.setNonNullParameter(
          preparedStatement, 1, WebAuthnChallengePurposeEnum.AUTHENTICATION, JdbcType.VARCHAR);

      verify(preparedStatement, times(1))
          .setString(1, WebAuthnChallengePurposeEnum.AUTHENTICATION.getDbValue());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNullableResultByColumnName {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値がREGISTRATIONに対応する場合、REGISTRATIONを返すこと")
    void getNullableResultByColumnName_registration() throws SQLException {
      doReturn(WebAuthnChallengePurposeEnum.REGISTRATION.getDbValue())
          .when(resultSet)
          .getString("purpose_kbn");

      WebAuthnChallengePurposeEnum actual =
          webAuthnChallengePurposeEnumTypeHandler.getNullableResult(resultSet, "purpose_kbn");

      assertEquals(WebAuthnChallengePurposeEnum.REGISTRATION, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：DB保存値がAUTHENTICATIONに対応する場合、AUTHENTICATIONを返すこと")
    void getNullableResultByColumnName_authentication() throws SQLException {
      doReturn(WebAuthnChallengePurposeEnum.AUTHENTICATION.getDbValue())
          .when(resultSet)
          .getString("purpose_kbn");

      WebAuthnChallengePurposeEnum actual =
          webAuthnChallengePurposeEnumTypeHandler.getNullableResult(resultSet, "purpose_kbn");

      assertEquals(WebAuthnChallengePurposeEnum.AUTHENTICATION, actual);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：DB値がnullの場合、nullを返すこと")
    void getNullableResultByColumnName_null() throws SQLException {
      doReturn(null).when(resultSet).getString("purpose_kbn");

      assertNull(
          webAuthnChallengePurposeEnumTypeHandler.getNullableResult(resultSet, "purpose_kbn"));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：DB保存値に一致するEnum値がない場合、nullを返すこと")
    void getNullableResultByColumnName_unmatched() throws SQLException {
      doReturn("unknown").when(resultSet).getString("purpose_kbn");

      assertNull(
          webAuthnChallengePurposeEnumTypeHandler.getNullableResult(resultSet, "purpose_kbn"));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNullableResultByColumnIndex {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値に一致するEnum値を返すこと")
    void getNullableResultByColumnIndex_matched() throws SQLException {
      doReturn(WebAuthnChallengePurposeEnum.REGISTRATION.getDbValue()).when(resultSet).getString(1);

      assertEquals(
          WebAuthnChallengePurposeEnum.REGISTRATION,
          webAuthnChallengePurposeEnumTypeHandler.getNullableResult(resultSet, 1));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNullableResultByCallableStatement {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値に一致するEnum値を返すこと")
    void getNullableResultByCallableStatement_matched() throws SQLException {
      doReturn(WebAuthnChallengePurposeEnum.AUTHENTICATION.getDbValue())
          .when(callableStatement)
          .getString(1);

      assertEquals(
          WebAuthnChallengePurposeEnum.AUTHENTICATION,
          webAuthnChallengePurposeEnumTypeHandler.getNullableResult(callableStatement, 1));
    }
  }
}
