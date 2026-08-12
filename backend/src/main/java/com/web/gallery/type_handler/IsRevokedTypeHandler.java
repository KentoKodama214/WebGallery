package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.common.IsRevoked;

/**
 * IsRevokedの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class IsRevokedTypeHandler extends BaseTypeHandler<IsRevoked> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, IsRevoked parameter, JdbcType jdbcType) throws SQLException {
		ps.setBoolean(i, parameter.value());
	}

	@Override
	public IsRevoked getNullableResult(ResultSet rs, String columnName) throws SQLException {
		boolean value = rs.getBoolean(columnName);
		return rs.wasNull() ? null : new IsRevoked(value);
	}

	@Override
	public IsRevoked getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		boolean value = rs.getBoolean(columnIndex);
		return rs.wasNull() ? null : new IsRevoked(value);
	}

	@Override
	public IsRevoked getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		boolean value = cs.getBoolean(columnIndex);
		return cs.wasNull() ? null : new IsRevoked(value);
	}
}
