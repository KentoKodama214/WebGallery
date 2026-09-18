package com.web.gallery.type_handler;

import com.web.gallery.enumeration.InquiryStatusEnum;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/** お問い合わせステータスのJavaとPostgresのEnum型を仲介する型変換クラス */
public class InquiryStatusEnumTypeHandler extends BaseTypeHandler<InquiryStatusEnum> {
  // Java -> DB (WHERE句やINSERTで使用)
  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, InquiryStatusEnum parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setString(i, parameter.getDbValue());
  }

  // DB -> Java (SELECTの結果取得で使用)
  @Override
  public InquiryStatusEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return fromDbValue(rs.getString(columnName));
  }

  @Override
  public InquiryStatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return fromDbValue(rs.getString(columnIndex));
  }

  @Override
  public InquiryStatusEnum getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return fromDbValue(cs.getString(columnIndex));
  }

  private InquiryStatusEnum fromDbValue(String dbValue) {
    if (dbValue == null) return null;
    for (InquiryStatusEnum status : InquiryStatusEnum.values()) {
      if (status.getDbValue().equals(dbValue)) return status;
    }
    return null;
  }
}
