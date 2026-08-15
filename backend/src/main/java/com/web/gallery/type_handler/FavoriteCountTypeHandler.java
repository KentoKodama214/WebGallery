package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.photo.FavoriteCount;

/**
 * FavoriteCountの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class FavoriteCountTypeHandler extends BaseTypeHandler<FavoriteCount> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, FavoriteCount parameter, JdbcType jdbcType) throws SQLException {
		ps.setInt(i, parameter.value());
	}

	@Override
	public FavoriteCount getNullableResult(ResultSet rs, String columnName) throws SQLException {
		int value = rs.getInt(columnName);
		return rs.wasNull() ? null : new FavoriteCount(value);
	}

	@Override
	public FavoriteCount getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		int value = rs.getInt(columnIndex);
		return rs.wasNull() ? null : new FavoriteCount(value);
	}

	@Override
	public FavoriteCount getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		int value = cs.getInt(columnIndex);
		return cs.wasNull() ? null : new FavoriteCount(value);
	}
}
