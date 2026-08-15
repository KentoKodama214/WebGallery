package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.account.FreeMemo;

/**
 * FreeMemoの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class FreeMemoTypeHandler extends BaseTypeHandler<FreeMemo> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, FreeMemo parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.value());
	}

	@Override
	public FreeMemo getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String value = rs.getString(columnName);
		return new FreeMemo(value);
	}

	@Override
	public FreeMemo getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String value = rs.getString(columnIndex);
		return new FreeMemo(value);
	}

	@Override
	public FreeMemo getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String value = cs.getString(columnIndex);
		return new FreeMemo(value);
	}
}
