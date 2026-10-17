package com.web.gallery.infrastructure.persistence.type_handler;

import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/** ポートフォリオの公開状態のJavaとPostgresのEnum型を仲介する型変換クラス */
public class PortfolioStatusEnumTypeHandler extends BaseTypeHandler<PortfolioStatusEnum> {
  // Java -> DB (WHERE句やINSERTで使用)
  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, PortfolioStatusEnum parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setString(i, parameter.getDbValue());
  }

  // DB -> Java (SELECTの結果取得で使用)
  @Override
  public PortfolioStatusEnum getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return fromDbValue(rs.getString(columnName));
  }

  @Override
  public PortfolioStatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return fromDbValue(rs.getString(columnIndex));
  }

  @Override
  public PortfolioStatusEnum getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return fromDbValue(cs.getString(columnIndex));
  }

  private PortfolioStatusEnum fromDbValue(String dbValue) {
    if (dbValue == null) return null;
    for (PortfolioStatusEnum value : PortfolioStatusEnum.values()) {
      if (value.getDbValue().equals(dbValue)) return value;
    }
    return null;
  }
}
