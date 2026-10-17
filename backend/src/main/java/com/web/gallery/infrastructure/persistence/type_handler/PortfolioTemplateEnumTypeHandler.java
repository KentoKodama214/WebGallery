package com.web.gallery.infrastructure.persistence.type_handler;

import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/** ポートフォリオのテンプレートのJavaとPostgresのEnum型を仲介する型変換クラス */
public class PortfolioTemplateEnumTypeHandler extends BaseTypeHandler<PortfolioTemplateEnum> {
  // Java -> DB (WHERE句やINSERTで使用)
  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, PortfolioTemplateEnum parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setString(i, parameter.getDbValue());
  }

  // DB -> Java (SELECTの結果取得で使用)
  @Override
  public PortfolioTemplateEnum getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return fromDbValue(rs.getString(columnName));
  }

  @Override
  public PortfolioTemplateEnum getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    return fromDbValue(rs.getString(columnIndex));
  }

  @Override
  public PortfolioTemplateEnum getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return fromDbValue(cs.getString(columnIndex));
  }

  private PortfolioTemplateEnum fromDbValue(String dbValue) {
    if (dbValue == null) return null;
    for (PortfolioTemplateEnum value : PortfolioTemplateEnum.values()) {
      if (value.getDbValue().equals(dbValue)) return value;
    }
    return null;
  }
}
