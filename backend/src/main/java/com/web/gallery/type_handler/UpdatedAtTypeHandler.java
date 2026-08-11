package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.common.UpdatedAt;

/**
 * UpdatedAtの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class UpdatedAtTypeHandler extends BaseTypeHandler<UpdatedAt> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, UpdatedAt parameter, JdbcType jdbcType) throws SQLException {
		ps.setObject(i, parameter.value());
	}

	@Override
	public UpdatedAt getNullableResult(ResultSet rs, String columnName) throws SQLException {
		OffsetDateTime value = rs.getObject(columnName, OffsetDateTime.class);
		return value == null ? null : new UpdatedAt(value);
	}

	@Override
	public UpdatedAt getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		OffsetDateTime value = rs.getObject(columnIndex, OffsetDateTime.class);
		return value == null ? null : new UpdatedAt(value);
	}

	@Override
	public UpdatedAt getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		OffsetDateTime value = cs.getObject(columnIndex, OffsetDateTime.class);
		return value == null ? null : new UpdatedAt(value);
	}
}
