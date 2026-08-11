package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.common.CreatedAt;

/**
 * CreatedAtの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class CreatedAtTypeHandler extends BaseTypeHandler<CreatedAt> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, CreatedAt parameter, JdbcType jdbcType) throws SQLException {
		ps.setObject(i, parameter.getValue());
	}

	@Override
	public CreatedAt getNullableResult(ResultSet rs, String columnName) throws SQLException {
		OffsetDateTime value = rs.getObject(columnName, OffsetDateTime.class);
		return value == null ? null : new CreatedAt(value);
	}

	@Override
	public CreatedAt getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		OffsetDateTime value = rs.getObject(columnIndex, OffsetDateTime.class);
		return value == null ? null : new CreatedAt(value);
	}

	@Override
	public CreatedAt getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		OffsetDateTime value = cs.getObject(columnIndex, OffsetDateTime.class);
		return value == null ? null : new CreatedAt(value);
	}
}
