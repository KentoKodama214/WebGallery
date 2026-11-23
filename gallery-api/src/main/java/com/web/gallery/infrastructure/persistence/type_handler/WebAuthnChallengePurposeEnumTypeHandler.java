package com.web.gallery.infrastructure.persistence.type_handler;

import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/** WebAuthnチャレンジの用途のJavaとPostgresのEnum型を仲介する型変換クラス */
public class WebAuthnChallengePurposeEnumTypeHandler
    extends BaseTypeHandler<WebAuthnChallengePurposeEnum> {
  // Java -> DB (WHERE句やINSERTで使用)
  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, WebAuthnChallengePurposeEnum parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setString(i, parameter.getDbValue());
  }

  // DB -> Java (SELECTの結果取得で使用)
  @Override
  public WebAuthnChallengePurposeEnum getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return fromDbValue(rs.getString(columnName));
  }

  @Override
  public WebAuthnChallengePurposeEnum getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    return fromDbValue(rs.getString(columnIndex));
  }

  @Override
  public WebAuthnChallengePurposeEnum getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return fromDbValue(cs.getString(columnIndex));
  }

  private WebAuthnChallengePurposeEnum fromDbValue(String dbValue) {
    if (dbValue == null) return null;
    for (WebAuthnChallengePurposeEnum value : WebAuthnChallengePurposeEnum.values()) {
      if (value.getDbValue().equals(dbValue)) return value;
    }
    return null;
  }
}
