package com.web.gallery.infrastructure.persistence.type_handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
class JsonbTypeHandlerTest {
  @InjectMocks private JsonbTypeHandler jsonbTypeHandler;

  @Mock private PreparedStatement preparedStatement;
  @Mock private ResultSet resultSet;
  @Mock private CallableStatement callableStatement;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class setNonNullParameter {
    @Test
    @Order(1)
    @DisplayName("正常系：マップをJSON文字列に変換し、jsonb型として解釈されるようTypes.OTHERで設定されること")
    void setNonNullParameter_success() throws SQLException {
      Map<String, Object> parameter = new LinkedHashMap<>();
      parameter.put("rows", List.of(Map.of("pattern", "TWO_EQUAL")));
      parameter.put("caption", "金閣寺");

      jsonbTypeHandler.setNonNullParameter(preparedStatement, 1, parameter, JdbcType.OTHER);

      verify(preparedStatement, times(1))
          .setObject(
              eq(1),
              eq("{\"rows\":[{\"pattern\":\"TWO_EQUAL\"}],\"caption\":\"金閣寺\"}"),
              eq(Types.OTHER));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：空のマップの場合、空のJSONオブジェクトが設定されること")
    void setNonNullParameter_empty() throws SQLException {
      jsonbTypeHandler.setNonNullParameter(preparedStatement, 1, Map.of(), JdbcType.OTHER);

      verify(preparedStatement, times(1)).setObject(1, "{}", Types.OTHER);
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNullableResultByColumnName {
    @Test
    @Order(1)
    @DisplayName("正常系：JSON文字列を、入れ子を含めてマップに変換すること")
    void getNullableResultByColumnName_success() throws SQLException {
      doReturn("{\"rows\": [{\"pattern\": \"TWO_EQUAL\"}], \"count\": 2}")
          .when(resultSet)
          .getString("template_settings");

      Map<String, Object> actual =
          jsonbTypeHandler.getNullableResult(resultSet, "template_settings");

      assertEquals(Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL")), "count", 2), actual);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：DB保存値がnullの場合、nullを返すこと")
    void getNullableResultByColumnName_null() throws SQLException {
      doReturn(null).when(resultSet).getString("template_settings");

      assertNull(jsonbTypeHandler.getNullableResult(resultSet, "template_settings"));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：JSONオブジェクトとして解釈できない場合、SQLExceptionをスローすること")
    void getNullableResultByColumnName_invalidJson() throws SQLException {
      doReturn("[1, 2]").when(resultSet).getString("template_settings");

      assertThrows(
          SQLException.class,
          () -> jsonbTypeHandler.getNullableResult(resultSet, "template_settings"));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNullableResultByColumnIndex {
    @Test
    @Order(1)
    @DisplayName("正常系：JSON文字列をマップに変換すること")
    void getNullableResultByColumnIndex_success() throws SQLException {
      doReturn("{\"caption\": \"金閣寺\"}").when(resultSet).getString(1);

      assertEquals(Map.of("caption", "金閣寺"), jsonbTypeHandler.getNullableResult(resultSet, 1));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNullableResultByCallableStatement {
    @Test
    @Order(1)
    @DisplayName("正常系：JSON文字列をマップに変換すること")
    void getNullableResultByCallableStatement_success() throws SQLException {
      doReturn("{\"caption\": \"金閣寺\"}").when(callableStatement).getString(1);

      assertEquals(
          Map.of("caption", "金閣寺"), jsonbTypeHandler.getNullableResult(callableStatement, 1));
    }
  }
}
