package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.photo.PhotoEnglishTitle;

/**
 * PhotoEnglishTitleの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class PhotoEnglishTitleTypeHandler extends BaseTypeHandler<PhotoEnglishTitle> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, PhotoEnglishTitle parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.value());
	}

	@Override
	public PhotoEnglishTitle getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String value = rs.getString(columnName);
		return value == null ? null : new PhotoEnglishTitle(value);
	}

	@Override
	public PhotoEnglishTitle getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String value = rs.getString(columnIndex);
		return value == null ? null : new PhotoEnglishTitle(value);
	}

	@Override
	public PhotoEnglishTitle getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String value = cs.getString(columnIndex);
		return value == null ? null : new PhotoEnglishTitle(value);
	}
}
