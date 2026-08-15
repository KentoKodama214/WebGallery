package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.common.UpdatedBy;

/**
 * UpdatedByの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class UpdatedByTypeHandler extends BaseTypeHandler<UpdatedBy> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, UpdatedBy parameter, JdbcType jdbcType) throws SQLException {
		ps.setLong(i, parameter.value());
	}

	@Override
	public UpdatedBy getNullableResult(ResultSet rs, String columnName) throws SQLException {
		long value = rs.getLong(columnName);
		return rs.wasNull() ? null : new UpdatedBy(value);
	}

	@Override
	public UpdatedBy getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		long value = rs.getLong(columnIndex);
		return rs.wasNull() ? null : new UpdatedBy(value);
	}

	@Override
	public UpdatedBy getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		long value = cs.getLong(columnIndex);
		return cs.wasNull() ? null : new UpdatedBy(value);
	}
}
