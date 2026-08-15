package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.photo.PhotoJapaneseTitle;

/**
 * PhotoJapaneseTitleの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class PhotoJapaneseTitleTypeHandler extends BaseTypeHandler<PhotoJapaneseTitle> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, PhotoJapaneseTitle parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.value());
	}

	@Override
	public PhotoJapaneseTitle getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String value = rs.getString(columnName);
		return value == null ? null : new PhotoJapaneseTitle(value);
	}

	@Override
	public PhotoJapaneseTitle getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String value = rs.getString(columnIndex);
		return value == null ? null : new PhotoJapaneseTitle(value);
	}

	@Override
	public PhotoJapaneseTitle getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String value = cs.getString(columnIndex);
		return value == null ? null : new PhotoJapaneseTitle(value);
	}
}
