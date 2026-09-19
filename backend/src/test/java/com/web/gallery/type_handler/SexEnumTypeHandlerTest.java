package com.web.gallery.type_handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.enumeration.SexEnum;
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
class SexEnumTypeHandlerTest {
  @InjectMocks private SexEnumTypeHandler sexEnumTypeHandler;

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
      sexEnumTypeHandler.setNonNullParameter(preparedStatement, 1, SexEnum.WOMAN, JdbcType.VARCHAR);

      verify(preparedStatement, times(1)).setString(1, SexEnum.WOMAN.getDbValue());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNullableResultByColumnName {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値がNONEに対応する場合、NONEを返すこと")
    void getNullableResultByColumnName_none() throws SQLException {
      doReturn(SexEnum.NONE.getDbValue()).when(resultSet).getString("sex");

      SexEnum actual = sexEnumTypeHandler.getNullableResult(resultSet, "sex");

      assertEquals(SexEnum.NONE, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：DB保存値がMANに対応する場合、MANを返すこと")
    void getNullableResultByColumnName_man() throws SQLException {
      doReturn(SexEnum.MAN.getDbValue()).when(resultSet).getString("sex");

      SexEnum actual = sexEnumTypeHandler.getNullableResult(resultSet, "sex");

      assertEquals(SexEnum.MAN, actual);
    }

    @Test
    @Order(3)
    @DisplayName("正常系：DB保存値がWOMANに対応する場合、WOMANを返すこと")
    void getNullableResultByColumnName_woman() throws SQLException {
      doReturn(SexEnum.WOMAN.getDbValue()).when(resultSet).getString("sex");

      SexEnum actual = sexEnumTypeHandler.getNullableResult(resultSet, "sex");

      assertEquals(SexEnum.WOMAN, actual);
    }

    @Test
    @Order(4)
    @DisplayName("異常系：DB値がnullの場合、nullを返すこと")
    void getNullableResultByColumnName_null() throws SQLException {
      doReturn(null).when(resultSet).getString("sex");

      SexEnum actual = sexEnumTypeHandler.getNullableResult(resultSet, "sex");

      assertNull(actual);
    }

    @Test
    @Order(5)
    @DisplayName("異常系：DB保存値に一致するEnum値がない場合、nullを返すこと")
    void getNullableResultByColumnName_unmatched() throws SQLException {
      doReturn("unknown").when(resultSet).getString("sex");

      SexEnum actual = sexEnumTypeHandler.getNullableResult(resultSet, "sex");

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
      doReturn(SexEnum.NONE.getDbValue()).when(resultSet).getString(1);

      SexEnum actual = sexEnumTypeHandler.getNullableResult(resultSet, 1);

      assertEquals(SexEnum.NONE, actual);
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
      doReturn(SexEnum.WOMAN.getDbValue()).when(callableStatement).getString(1);

      SexEnum actual = sexEnumTypeHandler.getNullableResult(callableStatement, 1);

      assertEquals(SexEnum.WOMAN, actual);
    }
  }
}
