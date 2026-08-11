package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.common.IsDeleted;

/**
 * IsDeletedの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class IsDeletedTypeHandler extends BaseTypeHandler<IsDeleted> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, IsDeleted parameter, JdbcType jdbcType) throws SQLException {
		ps.setBoolean(i, parameter.getValue());
	}

	@Override
	public IsDeleted getNullableResult(ResultSet rs, String columnName) throws SQLException {
		boolean value = rs.getBoolean(columnName);
		return rs.wasNull() ? null : new IsDeleted(value);
	}

	@Override
	public IsDeleted getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		boolean value = rs.getBoolean(columnIndex);
		return rs.wasNull() ? null : new IsDeleted(value);
	}

	@Override
	public IsDeleted getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		boolean value = cs.getBoolean(columnIndex);
		return cs.wasNull() ? null : new IsDeleted(value);
	}
}
