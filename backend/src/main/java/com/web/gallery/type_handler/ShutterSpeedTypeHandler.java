package com.web.gallery.type_handler;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.photo.ShutterSpeed;

/**
 * ShutterSpeedの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class ShutterSpeedTypeHandler extends BaseTypeHandler<ShutterSpeed> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ShutterSpeed parameter, JdbcType jdbcType) throws SQLException {
		ps.setBigDecimal(i, parameter.value());
	}

	@Override
	public ShutterSpeed getNullableResult(ResultSet rs, String columnName) throws SQLException {
		BigDecimal value = rs.getBigDecimal(columnName);
		return value == null ? null : new ShutterSpeed(value);
	}

	@Override
	public ShutterSpeed getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		BigDecimal value = rs.getBigDecimal(columnIndex);
		return value == null ? null : new ShutterSpeed(value);
	}

	@Override
	public ShutterSpeed getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		BigDecimal value = cs.getBigDecimal(columnIndex);
		return value == null ? null : new ShutterSpeed(value);
	}
}
