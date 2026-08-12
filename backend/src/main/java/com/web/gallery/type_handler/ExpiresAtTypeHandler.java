package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.common.ExpiresAt;

/**
 * ExpiresAtの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class ExpiresAtTypeHandler extends BaseTypeHandler<ExpiresAt> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ExpiresAt parameter, JdbcType jdbcType) throws SQLException {
		ps.setObject(i, parameter.value());
	}

	@Override
	public ExpiresAt getNullableResult(ResultSet rs, String columnName) throws SQLException {
		OffsetDateTime value = rs.getObject(columnName, OffsetDateTime.class);
		return value == null ? null : new ExpiresAt(value);
	}

	@Override
	public ExpiresAt getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		OffsetDateTime value = rs.getObject(columnIndex, OffsetDateTime.class);
		return value == null ? null : new ExpiresAt(value);
	}

	@Override
	public ExpiresAt getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		OffsetDateTime value = cs.getObject(columnIndex, OffsetDateTime.class);
		return value == null ? null : new ExpiresAt(value);
	}
}
