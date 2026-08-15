package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.common.KbnEnglishName;

/**
 * KbnEnglishNameの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class KbnEnglishNameTypeHandler extends BaseTypeHandler<KbnEnglishName> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, KbnEnglishName parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.value());
	}

	@Override
	public KbnEnglishName getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String value = rs.getString(columnName);
		return value == null ? null : new KbnEnglishName(value);
	}

	@Override
	public KbnEnglishName getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String value = rs.getString(columnIndex);
		return value == null ? null : new KbnEnglishName(value);
	}

	@Override
	public KbnEnglishName getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String value = cs.getString(columnIndex);
		return value == null ? null : new KbnEnglishName(value);
	}
}
