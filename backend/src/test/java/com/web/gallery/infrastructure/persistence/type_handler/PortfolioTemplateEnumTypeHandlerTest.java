package com.web.gallery.infrastructure.persistence.type_handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
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
class PortfolioTemplateEnumTypeHandlerTest {
  @InjectMocks private PortfolioTemplateEnumTypeHandler portfolioTemplateEnumTypeHandler;

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
      portfolioTemplateEnumTypeHandler.setNonNullParameter(
          preparedStatement, 1, PortfolioTemplateEnum.GRID, JdbcType.VARCHAR);

      verify(preparedStatement, times(1)).setString(1, PortfolioTemplateEnum.GRID.getDbValue());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNullableResultByColumnName {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値がFEATUREに対応する場合、FEATUREを返すこと")
    void getNullableResultByColumnName_feature() throws SQLException {
      doReturn(PortfolioTemplateEnum.FEATURE.getDbValue())
          .when(resultSet)
          .getString("template_kbn");

      assertEquals(
          PortfolioTemplateEnum.FEATURE,
          portfolioTemplateEnumTypeHandler.getNullableResult(resultSet, "template_kbn"));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：DB保存値がGRIDに対応する場合、GRIDを返すこと")
    void getNullableResultByColumnName_grid() throws SQLException {
      doReturn(PortfolioTemplateEnum.GRID.getDbValue()).when(resultSet).getString("template_kbn");

      assertEquals(
          PortfolioTemplateEnum.GRID,
          portfolioTemplateEnumTypeHandler.getNullableResult(resultSet, "template_kbn"));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：DB保存値がnullの場合、nullを返すこと")
    void getNullableResultByColumnName_null() throws SQLException {
      doReturn(null).when(resultSet).getString("template_kbn");

      assertNull(portfolioTemplateEnumTypeHandler.getNullableResult(resultSet, "template_kbn"));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：DB保存値がどのEnum値にも一致しない場合、nullを返すこと")
    void getNullableResultByColumnName_unmatched() throws SQLException {
      doReturn("unknown").when(resultSet).getString("template_kbn");

      assertNull(portfolioTemplateEnumTypeHandler.getNullableResult(resultSet, "template_kbn"));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNullableResultByColumnIndex {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値に対応するEnum値を返すこと")
    void getNullableResultByColumnIndex_success() throws SQLException {
      doReturn(PortfolioTemplateEnum.GRID.getDbValue()).when(resultSet).getString(1);

      assertEquals(
          PortfolioTemplateEnum.GRID,
          portfolioTemplateEnumTypeHandler.getNullableResult(resultSet, 1));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNullableResultByCallableStatement {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値に対応するEnum値を返すこと")
    void getNullableResultByCallableStatement_success() throws SQLException {
      doReturn(PortfolioTemplateEnum.GRID.getDbValue()).when(callableStatement).getString(1);

      assertEquals(
          PortfolioTemplateEnum.GRID,
          portfolioTemplateEnumTypeHandler.getNullableResult(callableStatement, 1));
    }
  }
}
