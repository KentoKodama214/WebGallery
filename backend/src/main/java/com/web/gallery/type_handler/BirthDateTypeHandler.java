package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.account.BirthDate;

/**
 * BirthDateの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class BirthDateTypeHandler extends BaseTypeHandler<BirthDate> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, BirthDate parameter, JdbcType jdbcType) throws SQLException {
		ps.setDate(i, Date.valueOf(parameter.getValue()));
	}

	@Override
	public BirthDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Date date = rs.getDate(columnName);
		return date == null ? null : new BirthDate(date.toLocalDate());
	}

	@Override
	public BirthDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		Date date = rs.getDate(columnIndex);
		return date == null ? null : new BirthDate(date.toLocalDate());
	}

	@Override
	public BirthDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		Date date = cs.getDate(columnIndex);
		return date == null ? null : new BirthDate(date.toLocalDate());
	}
}
