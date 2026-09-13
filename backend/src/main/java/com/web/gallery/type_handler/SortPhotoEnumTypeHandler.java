package com.web.gallery.type_handler;

import com.web.gallery.enumeration.SortPhotoEnum;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/** 写真一覧の並び順のJavaとPostgresのEnum型を仲介する型変換クラス */
public class SortPhotoEnumTypeHandler extends BaseTypeHandler<SortPhotoEnum> {
  // Java -> DB (WHERE句やINSERTで使用)
  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, SortPhotoEnum parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, toDbValue(parameter));
  }

  // DB -> Java (SELECTの結果取得で使用)
  @Override
  public SortPhotoEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return fromDbValue(rs.getString(columnName));
  }

  @Override
  public SortPhotoEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return fromDbValue(rs.getString(columnIndex));
  }

  @Override
  public SortPhotoEnum getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return fromDbValue(cs.getString(columnIndex));
  }

  private String toDbValue(SortPhotoEnum sortPhoto) {
    return switch (sortPhoto) {
      case PHOTO_AT -> "photo_at";
      case FAVORITE -> "favorite";
      case SEASON -> "season";
    };
  }

  private SortPhotoEnum fromDbValue(String dbValue) {
    if (dbValue == null) return null;
    return switch (dbValue) {
      case "photo_at" -> SortPhotoEnum.PHOTO_AT;
      case "favorite" -> SortPhotoEnum.FAVORITE;
      case "season" -> SortPhotoEnum.SEASON;
      default -> null;
    };
  }
}
