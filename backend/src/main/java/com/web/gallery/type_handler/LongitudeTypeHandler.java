package com.web.gallery.type_handler;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.common.Longitude;

/**
 * Longitudeの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class LongitudeTypeHandler extends BaseTypeHandler<Longitude> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Longitude parameter, JdbcType jdbcType) throws SQLException {
		ps.setBigDecimal(i, parameter.value());
	}

	@Override
	public Longitude getNullableResult(ResultSet rs, String columnName) throws SQLException {
		BigDecimal value = rs.getBigDecimal(columnName);
		return value == null ? null : new Longitude(value);
	}

	@Override
	public Longitude getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		BigDecimal value = rs.getBigDecimal(columnIndex);
		return value == null ? null : new Longitude(value);
	}

	@Override
	public Longitude getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		BigDecimal value = cs.getBigDecimal(columnIndex);
		return value == null ? null : new Longitude(value);
	}
}
