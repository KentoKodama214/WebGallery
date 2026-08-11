package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.account.ResidentPrefectureKbnCode;

/**
 * ResidentPrefectureKbnCodeの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class ResidentPrefectureKbnCodeTypeHandler extends BaseTypeHandler<ResidentPrefectureKbnCode> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ResidentPrefectureKbnCode parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.value());
	}

	@Override
	public ResidentPrefectureKbnCode getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String value = rs.getString(columnName);
		return value == null ? null : new ResidentPrefectureKbnCode(value);
	}

	@Override
	public ResidentPrefectureKbnCode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String value = rs.getString(columnIndex);
		return value == null ? null : new ResidentPrefectureKbnCode(value);
	}

	@Override
	public ResidentPrefectureKbnCode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String value = cs.getString(columnIndex);
		return value == null ? null : new ResidentPrefectureKbnCode(value);
	}
}
