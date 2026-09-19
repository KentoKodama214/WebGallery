package com.web.gallery.type_handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.enumeration.InquiryStatusEnum;
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
class InquiryStatusEnumTypeHandlerTest {
  @InjectMocks private InquiryStatusEnumTypeHandler inquiryStatusEnumTypeHandler;

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
      inquiryStatusEnumTypeHandler.setNonNullParameter(
          preparedStatement, 1, InquiryStatusEnum.REPLIED, JdbcType.VARCHAR);

      verify(preparedStatement, times(1)).setString(1, InquiryStatusEnum.REPLIED.getDbValue());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNullableResultByColumnName {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値に一致するEnum値を返すこと")
    void getNullableResultByColumnName_matched() throws SQLException {
      doReturn(InquiryStatusEnum.WITHDRAWN.getDbValue()).when(resultSet).getString("status");

      InquiryStatusEnum actual =
          inquiryStatusEnumTypeHandler.getNullableResult(resultSet, "status");

      assertEquals(InquiryStatusEnum.WITHDRAWN, actual);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：DB値がnullの場合、nullを返すこと")
    void getNullableResultByColumnName_null() throws SQLException {
      doReturn(null).when(resultSet).getString("status");

      InquiryStatusEnum actual =
          inquiryStatusEnumTypeHandler.getNullableResult(resultSet, "status");

      assertNull(actual);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：DB保存値に一致するEnum値がない場合、nullを返すこと")
    void getNullableResultByColumnName_unmatched() throws SQLException {
      doReturn("unknown").when(resultSet).getString("status");

      InquiryStatusEnum actual =
          inquiryStatusEnumTypeHandler.getNullableResult(resultSet, "status");

      assertNull(actual);
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
      doReturn(InquiryStatusEnum.UNREPLIED.getDbValue()).when(resultSet).getString(1);

      InquiryStatusEnum actual = inquiryStatusEnumTypeHandler.getNullableResult(resultSet, 1);

      assertEquals(InquiryStatusEnum.UNREPLIED, actual);
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
      doReturn(InquiryStatusEnum.REPLIED.getDbValue()).when(callableStatement).getString(1);

      InquiryStatusEnum actual =
          inquiryStatusEnumTypeHandler.getNullableResult(callableStatement, 1);

      assertEquals(InquiryStatusEnum.REPLIED, actual);
    }
  }
}
