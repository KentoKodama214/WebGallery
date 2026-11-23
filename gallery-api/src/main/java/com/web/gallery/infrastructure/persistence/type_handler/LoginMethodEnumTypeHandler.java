package com.web.gallery.infrastructure.persistence.type_handler;

import com.web.gallery.domain.enumeration.LoginMethodEnum;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/** ログイン方式のJavaとPostgresのEnum型を仲介する型変換クラス */
public class LoginMethodEnumTypeHandler extends BaseTypeHandler<LoginMethodEnum> {
  // Java -> DB (WHERE句やINSERTで使用)
  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, LoginMethodEnum parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setString(i, parameter.getDbValue());
  }

  // DB -> Java (SELECTの結果取得で使用)
  @Override
  public LoginMethodEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return fromDbValue(rs.getString(columnName));
  }

  @Override
  public LoginMethodEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return fromDbValue(rs.getString(columnIndex));
  }

  @Override
  public LoginMethodEnum getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return fromDbValue(cs.getString(columnIndex));
  }

  private LoginMethodEnum fromDbValue(String dbValue) {
    if (dbValue == null) return null;
    for (LoginMethodEnum value : LoginMethodEnum.values()) {
      if (value.getDbValue().equals(dbValue)) return value;
    }
    return null;
  }
}
